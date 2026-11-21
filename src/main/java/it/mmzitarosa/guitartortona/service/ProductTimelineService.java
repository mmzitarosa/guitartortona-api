package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.product.ProductTimelineDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductTimelineDTO.Entry;
import it.mmzitarosa.guitartortona.dto.product.ProductTimelineDTO.FieldChange;
import it.mmzitarosa.guitartortona.entity.AuditLogEntity;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.repository.AuditLogRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.repository.SaleRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.mmzitarosa.guitartortona.dto.product.ProductTimelineDTO.*;

/**
 * Costruisce la storia di un prodotto unendo acquisti, vendite e registro di audit. Gli acquisti mostrano i valori
 * originali della riga di fattura; le modifiche successive sono voci a parte, una per giorno, e i valori sovrascritti
 * sono indicati in {@code superseded}.
 */
@Service @RequiredArgsConstructor
public class ProductTimelineService {

	// Campi del prodotto nello stesso ordine in cui compaiono nel form
	private static final List<String> PRODUCT_FIELDS = List.of("code", "internalCode", "category", "brand", "condition", "description", "price", "reorderPoint", "notes");
	private static final String PRODUCT_PRICE = "price";
	private static final String QUANTITY = "quantity";
	private static final List<String> COST_FIELDS = List.of("purchasePrice", "vat");

	private final ProductRepository productRepository;
	private final PurchaseItemRepository purchaseItemRepository;
	private final SaleRepository saleRepository;
	private final AuditLogRepository auditRepository;
	private final CurrentUser currentUser;

	@Transactional(readOnly = true)
	public ProductTimelineDTO getTimeline(Long productId) {
		if (!productRepository.existsById(productId))
			throw new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId);
		boolean full = currentUser.canSeeCosts();
		boolean admin = currentUser.isAdmin();

		List<AuditLogEntity> audit = auditRepository.findByProductIdOrderByCreatedDateAscIdAsc(productId);
		List<Entry> entries = new ArrayList<>();
		int purchased = 0;

		for (PurchaseItemEntity item : purchaseItemRepository.findActiveByProductId(productId)) {
			purchased += item.getQuantity() == null ? 0 : item.getQuantity();
			List<AuditLogEntity> updates = audit.stream()
					.filter(l -> "PurchaseItem".equals(l.getEntityType()) && AuditLogEntity.UPDATE.equals(l.getOperation()) && item.getId().equals(l.getEntityId()))
					.toList();
			entries.addAll(purchaseEntries(item, updates, full, admin));
		}

		entries.addAll(productEntries(audit, full, admin));

		Map<List<Object>, Entry> sales = new LinkedHashMap<>();
		int sold = 0;
		for (SaleEntity sale : saleRepository.findByProductId(productId)) {
			sold += sale.getQuantity() == null ? 0 : sale.getQuantity();
			Double price = full ? sale.getSalePrice() : null;
			sales.merge(Arrays.asList(sale.getDate(), price), Entry.builder().type(SALE).date(sale.getDate()).quantity(sale.getQuantity())
					.price(price).vat(full ? sale.getVat() : null).notes(sale.getNotes()).build(), this::mergeSales);
		}
		entries.addAll(sales.values());

		entries.sort(Comparator.comparing(Entry::date, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparingInt(e -> rank(e.type()))
				.thenComparing(Entry::at, Comparator.nullsLast(Comparator.reverseOrder())));
		return new ProductTimelineDTO(purchased, sold, entries);
	}

	/** A parità di giorno: vendite, modifiche, acquisti, creazione del prodotto. */
	private int rank(String type) {
		return switch (type) {
			case SALE -> 0;
			case PRODUCT_CHANGE, PURCHASE_CHANGE -> 1;
			case PURCHASE -> 2;
			default -> 3;
		};
	}

	private Entry mergeSales(Entry a, Entry b) {
		String notes = Stream.of(a.notes(), b.notes()).filter(n -> n != null && !n.isBlank()).collect(Collectors.joining("\n"));
		return Entry.builder().type(SALE).date(a.date()).quantity(a.quantity() + b.quantity()).price(a.price()).vat(a.vat())
				.notes(notes.isEmpty() ? null : notes).build();
	}

	/** Acquisto con i valori originali e, per ogni giorno con modifiche, una voce con i valori risultanti. */
	private List<Entry> purchaseEntries(PurchaseItemEntity item, List<AuditLogEntity> updates, boolean full, boolean admin) {
		Object purchase = Hibernate.unproxy(item.getPurchase());
		IncomingInvoiceEntity invoice = purchase instanceof IncomingInvoiceEntity i ? i : null;
		boolean showInvoice = full;
		Long invoiceId = showInvoice ? item.getPurchase().getId() : null;
		String number = showInvoice && invoice != null ? invoice.getNumber() : null;
		String supplier = showInvoice && invoice != null && invoice.getSupplier() != null ? invoice.getSupplier().getName() : null;
		String status = item.getPurchase().getStatus() == null ? null : item.getPurchase().getStatus().name();

		// Valore originale di ogni campo: il precedente della prima modifica, altrimenti quello attuale
		Map<String, String> state = new HashMap<>();
		state.put(QUANTITY, str(item.getQuantity()));
		state.put("purchasePrice", str(item.getPurchasePrice()));
		state.put("vat", str(item.getVat()));
		Set<String> seen = new HashSet<>();
		for (AuditLogEntity u : updates) if (state.containsKey(u.getField()) && seen.add(u.getField())) state.put(u.getField(), u.getOldValue());
		Map<String, String> original = new HashMap<>(state);

		record Day(LocalDate date, LocalDateTime at, String users, Set<String> changed, Map<String, String> before, Map<String, String> after) {}
		List<Day> days = new ArrayList<>();
		new TreeMap<>(updates.stream().filter(u -> state.containsKey(u.getField()))
				.collect(Collectors.groupingBy(u -> u.getCreatedDate().toLocalDate()))).forEach((date, rows) -> {
			Map<String, String> before = new HashMap<>(state);
			Map<String, String> last = new HashMap<>();
			rows.forEach(r -> last.put(r.getField(), r.getNewValue()));
			Set<String> changed = new LinkedHashSet<>();
			last.forEach((field, value) -> {
				if (!Objects.equals(state.get(field), value)) changed.add(field);
				state.put(field, value);
			});
			if (!full) changed.retainAll(List.of(QUANTITY));
			if (changed.isEmpty()) return;
			days.add(new Day(date, rows.get(rows.size() - 1).getCreatedDate(), users(rows, admin), changed, before, new HashMap<>(state)));
		});

		List<Entry> result = new ArrayList<>();
		Set<String> allChanged = days.stream().flatMap(d -> d.changed().stream()).collect(Collectors.toSet());
		result.add(Entry.builder().type(PURCHASE).date(item.getPurchase().getDate()).quantity(integer(original.get(QUANTITY)))
				.price(full ? number(original.get("purchasePrice")) : null).vat(full ? number(original.get("vat")) : null)
				.notes(full ? item.getPurchase().getNotes() : null).status(status)
				.invoiceId(invoiceId).invoiceNumber(number).supplier(supplier).superseded(allChanged).build());

		for (int i = 0; i < days.size(); i++) {
			Day day = days.get(i);
			Set<String> later = days.subList(i + 1, days.size()).stream().flatMap(d -> d.changed().stream()).collect(Collectors.toSet());
			boolean costChanged = day.changed().stream().anyMatch(COST_FIELDS::contains);
			boolean priceChanged = day.changed().contains("purchasePrice");
			boolean vatChanged = day.changed().contains("vat");
			result.add(Entry.builder().type(PURCHASE_CHANGE).date(day.date()).at(day.at()).users(day.users())
					.quantity(day.changed().contains(QUANTITY) ? integer(day.after().get(QUANTITY)) : null)
					.price(costChanged ? number(day.after().get("purchasePrice")) : null).vat(costChanged ? number(day.after().get("vat")) : null)
					.previousPrice(priceChanged ? number(day.before().get("purchasePrice")) : null)
					.previousVat(vatChanged ? number(day.before().get("vat")) : null)
					.invoiceId(invoiceId).invoiceNumber(number).supplier(supplier).superseded(later).build());
		}
		return result;
	}

	/** Creazione del prodotto e, per ogni giorno, un'unica voce con le modifiche nette di ogni campo. */
	private List<Entry> productEntries(List<AuditLogEntity> audit, boolean full, boolean admin) {
		List<AuditLogEntity> rows = audit.stream().filter(l -> "Product".equals(l.getEntityType())).toList();
		List<Entry> result = new ArrayList<>();
		rows.stream().filter(l -> AuditLogEntity.CREATE.equals(l.getOperation())).forEach(l ->
				result.add(Entry.builder().type(PRODUCT_CREATED).date(l.getCreatedDate().toLocalDate()).at(l.getCreatedDate()).users(users(List.of(l), admin)).build()));

		new TreeMap<>(rows.stream().filter(l -> AuditLogEntity.UPDATE.equals(l.getOperation()) && (full || !PRODUCT_PRICE.equals(l.getField())))
				.collect(Collectors.groupingBy(l -> l.getCreatedDate().toLocalDate()))).forEach((date, dayRows) -> {
			Map<String, String[]> net = new LinkedHashMap<>();
			for (AuditLogEntity r : dayRows) net.merge(r.getField(), new String[]{r.getOldValue(), r.getNewValue()}, (prev, next) -> new String[]{prev[0], next[1]});
			List<FieldChange> changes = net.entrySet().stream()
					.filter(e -> !Objects.equals(e.getValue()[0], e.getValue()[1]))
					.sorted(Comparator.comparingInt(e -> fieldRank(e.getKey())))
					.map(e -> new FieldChange(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
			if (changes.isEmpty()) return;
			result.add(Entry.builder().type(PRODUCT_CHANGE).date(date).at(dayRows.get(dayRows.size() - 1).getCreatedDate())
					.users(users(dayRows, admin)).changes(changes).build());
		});
		return result;
	}

	private int fieldRank(String field) {
		int i = PRODUCT_FIELDS.indexOf(field);
		return i < 0 ? PRODUCT_FIELDS.size() : i;
	}

	/** Gli utenti che hanno operato sono un'informazione per l'admin. */
	private String users(List<AuditLogEntity> rows, boolean admin) {
		return admin ? rows.stream().map(AuditLogEntity::getUsername).filter(Objects::nonNull).distinct().collect(Collectors.joining(", ")) : null;
	}

	private String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private Double number(String value) {
		return value == null ? null : Double.valueOf(value);
	}

	private Integer integer(String value) {
		return value == null ? null : Double.valueOf(value).intValue();
	}

}
