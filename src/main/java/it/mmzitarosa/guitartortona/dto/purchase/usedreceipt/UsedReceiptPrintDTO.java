package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import it.mmzitarosa.guitartortona.dto.customer.CustomerDTO;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public record UsedReceiptPrintDTO(
		String internalCode,
		String number,
		String date,
		Double amount,
		String notes,
		CustomerDTO customer,
		List<Item> items
) {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALY);

	public record Item(String internalCode, String letter, String reference, String category, String brand, String description, Double amount) {
		static Item of(UsedReceiptEntity receipt, PurchaseItemEntity entity, int index) {
			ProductEntity product = entity.getProduct();
			String reference = receipt.getItemReference(entity.getLetter());
			return new Item(product.getInternalCode(), entity.getLetter() == null ? String.valueOf((char) ('a' + index % 26)) : entity.getLetter().toLowerCase(), reference,
					product.getCategory() == null ? null : product.getCategory().getName(),
					product.getBrand() == null ? null : product.getBrand().getName(),
					product.getDescription(),
					entity.getPurchasePrice());
		}
	}

	/** "Cognome Nome" per la dichiarazione, null senza dati cliente */
	public String customerName() {
		return customer == null ? null : join(customer.lastName(), customer.firstName());
	}

	/** "Tipo numero" del documento, null se mancano entrambi */
	public String customerDocument() {
		return customer == null ? null : join(customer.documentType(), customer.documentNumber());
	}

	private static String join(String first, String second) {
		String joined = ((first == null ? "" : first) + " " + (second == null ? "" : second)).trim();
		return joined.isEmpty() ? null : joined;
	}

	public static UsedReceiptPrintDTO of(UsedReceiptEntity entity) {
		return new UsedReceiptPrintDTO(
				entity.getInternalCode(),
				entity.getReference(),
				entity.getDate().format(DATE_FORMAT),
				entity.getAmount(),
				entity.getNotes(),
				CustomerDTO.of(entity),
				IntStream.range(0, entity.getItems().size()).mapToObj(i -> Item.of(entity, entity.getItems().get(i), i)).toList());
	}
}
