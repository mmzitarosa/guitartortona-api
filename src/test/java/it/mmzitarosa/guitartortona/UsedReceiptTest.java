package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.dto.brand.BrandInput;
import it.mmzitarosa.guitartortona.dto.category.CategoryInput;
import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerAlignmentDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.ProductUsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptItemDTO;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.repository.AuditLogRepository;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.service.*;
import it.mmzitarosa.guitartortona.specification.ProductSpecification;
import it.mmzitarosa.guitartortona.utils.Constant.MovementType;
import it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import it.mmzitarosa.guitartortona.utils.ReceiptLetters;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MariaDBContainer;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UsedReceiptService.class, PurchaseItemService.class, CustomerService.class, LedgerService.class, BankService.class,
		ProductService.class, ProductTimelineService.class, CategoryService.class, BrandService.class, IncomingInvoiceService.class, SupplierService.class})
class UsedReceiptTest {

	static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11").withInitScript("db/migration/init.sql");

	static {
		MARIADB.start();
	}

	@DynamicPropertySource static void props(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MARIADB::getJdbcUrl);
		registry.add("spring.datasource.username", MARIADB::getUsername);
		registry.add("spring.datasource.password", MARIADB::getPassword);
	}

	@MockitoBean CurrentUser currentUser;
	@Autowired UsedReceiptService service;
	@Autowired CustomerService customerService;
	@Autowired PurchaseItemService itemService;
	@Autowired ProductService productService;
	@Autowired ProductTimelineService timeline;
	@Autowired CategoryService categoryService;
	@Autowired ProductRepository products;
	@Autowired CategoryRepository categories;
	@Autowired LedgerRepository ledger;
	@Autowired AuditLogRepository audit;
	@Autowired EntityManager em;

	private CategoryEntity category;

	@BeforeEach void seed() {
		category = new CategoryEntity();
		category.setName("Chitarra");
		category = categories.save(category);
		ReflectionTestUtils.invokeMethod(categoryService, "loadCategories");
		login(false);
	}

	private void login(boolean admin) {
		when(currentUser.isAdmin()).thenReturn(admin);
		when(currentUser.canSeeCosts()).thenReturn(true);
		when(currentUser.username()).thenReturn("tester");
	}

	private UsedReceiptInput receipt(LocalDate date, boolean withCustomer) {
		return receipt(date, withCustomer, null);
	}

	private UsedReceiptInput receipt(LocalDate date, boolean withCustomer, Long customerId) {
		CustomerInput customer = withCustomer
				? new CustomerInput(customerId, "Mario", "Rossi", "Via Roma 1", "15057", "Tortona", "AL", "CI", "AB123", "RSSMRA80A01L304X")
				: null;
		return new UsedReceiptInput(date, null, customer, false);
	}

	private ProductUsedReceiptInput item(String description, double price) {
		return new ProductUsedReceiptInput(new ProductInput(null, "IGNORED", new CategoryInput(category.getId()), new BrandInput(null, null), description, null, 5, null), null, price);
	}

	private UsedReceiptDetailDTO registered(LocalDate date, boolean withCustomer, String... descriptions) {
		Long id = service.createUsedReceipt(receipt(date, withCustomer)).id();
		for (String description : descriptions)
			itemService.addProductToUsedReceipt(id, item(description, 100));
		em.flush();
		em.clear();
		service.confirmUsedReceipt(id);
		em.flush();
		em.clear();
		return service.registerUsedReceipt(id);
	}

	@Test void lettersAreAlphabetical() {
		assertEquals("A", ReceiptLetters.fromIndex(1));
		assertEquals("Z", ReceiptLetters.fromIndex(26));
		assertEquals("AA", ReceiptLetters.fromIndex(27));
		assertEquals("AZ", ReceiptLetters.fromIndex(52));
		assertEquals(52, ReceiptLetters.toIndex("AZ"));
	}

	@Test void usedProductsHaveNoManufacturerCodeNorReorderPoint() {
		Long id = service.createUsedReceipt(receipt(LocalDate.of(2026, 3, 1), true)).id();
		UsedReceiptItemDTO item = itemService.addProductToUsedReceipt(id, item("Stratocaster", 250));

		var product = products.findById(item.product().id()).orElseThrow();
		assertEquals(ProductCondition.USED, product.getCondition());
		assertNull(product.getCode());
		assertNull(product.getReorderPoint());
		assertEquals(0.0, item.vat());
		assertEquals(250.0, service.getUsedReceiptDetail(id).amount());
	}

	@Test void registerAssignsNumberByReceiptYearAndStableLetters() {
		UsedReceiptDetailDTO first = registered(LocalDate.of(2025, 12, 30), true, "Strato", "Tele", "Les Paul");
		UsedReceiptDetailDTO second = registered(LocalDate.of(2025, 12, 31), true, "Jazzmaster");
		UsedReceiptDetailDTO nextYear = registered(LocalDate.of(2026, 1, 2), false, "Jaguar");

		assertEquals("1/25", first.number());
		assertEquals("2/25", second.number());
		assertEquals("1/26", nextYear.number());
		assertEquals(List.of("1/25 A", "1/25 B", "1/25 C"), first.items().stream().map(UsedReceiptItemDTO::reference).toList());
		assertEquals(Status.COMPLETED, first.status());
	}

	@Test void removedLetterIsNotReusedAndNewRowsContinue() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 5, 1), true, "Strato", "Tele", "Les Paul");
		itemService.deleteProductFromUsedReceipt(receipt.id(), receipt.items().get(1).id());
		itemService.deleteProductFromUsedReceipt(receipt.id(), receipt.items().get(2).id());
		itemService.addProductToUsedReceipt(receipt.id(), item("Jazzmaster", 80));
		em.flush();
		em.clear();

		UsedReceiptDetailDTO reloaded = service.getUsedReceiptDetail(receipt.id());
		assertEquals(List.of("A", "D"), reloaded.items().stream().map(UsedReceiptItemDTO::letter).toList());
		assertEquals("1/26", reloaded.number());
		assertEquals(180.0, reloaded.amount());
	}

	@Test void lettersAreAssignedOnConfirmAndLostWithTheProduct() {
		login(true);
		Long id = service.createUsedReceipt(receipt(LocalDate.of(2026, 5, 1), true)).id();
		itemService.addProductToUsedReceipt(id, item("Strato", 100));
		itemService.addProductToUsedReceipt(id, item("Tele", 100));
		assertEquals(List.of(), service.getUsedReceiptDetail(id).items().stream().map(UsedReceiptItemDTO::letter).filter(Objects::nonNull).toList());
		em.flush();
		em.clear();

		UsedReceiptDetailDTO pending = service.confirmUsedReceipt(id);
		assertEquals(List.of("A", "B"), pending.items().stream().map(UsedReceiptItemDTO::letter).toList());

		itemService.deleteProductFromUsedReceipt(id, pending.items().get(1).id());
		itemService.addProductToUsedReceipt(id, item("Jazzmaster", 80));
		em.flush();
		em.clear();
		assertEquals(List.of("A", "C"), service.getUsedReceiptDetail(id).items().stream().map(UsedReceiptItemDTO::letter).toList());

		UsedReceiptDetailDTO completed = service.registerUsedReceipt(id);
		assertEquals(List.of("1/26 A", "1/26 C"), completed.items().stream().map(UsedReceiptItemDTO::reference).toList());
	}

	@Test void registerCreatesPendingLedgerExpense() {
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato", "Tele");

		LedgerEntryEntity entry = ledger.findAll().stream().filter(l -> "1/26".equals(l.getInvoiceNumber())).findFirst().orElseThrow();
		assertEquals("Pagato materiale usato", entry.getDescription());
		assertEquals("Rossi Mario", entry.getReason());
		assertEquals(PaymentMethod.CASH, entry.getPaymentMethod());
		assertEquals(MovementType.EXPENSE, entry.getMovementType());
		assertEquals(Status.PENDING, entry.getStatus());
		assertEquals(receipt.amount(), entry.getAmount());
		assertEquals(LocalDate.of(2026, 2, 10), entry.getDate());
	}

	@Test void stepsMustBeFollowedInOrder() {
		Long id = service.createUsedReceipt(receipt(LocalDate.of(2026, 2, 10), false)).id();

		assertThrows(ConflictException.class, () -> service.confirmUsedReceipt(id));
		assertThrows(ConflictException.class, () -> service.registerUsedReceipt(id));

		itemService.addProductToUsedReceipt(id, item("Strato", 100));
		em.flush();
		em.clear();
		assertEquals(Status.PENDING, service.confirmUsedReceipt(id).status());
		assertNull(service.getUsedReceiptDetail(id).number());
		assertThrows(ConflictException.class, () -> service.confirmUsedReceipt(id));
	}

	@Test void operatorEditsOnlyDraftsAdminAlsoRegistered() {
		Long id = service.createUsedReceipt(receipt(LocalDate.of(2026, 2, 10), false)).id();
		itemService.addProductToUsedReceipt(id, item("Strato", 100));
		em.flush();
		em.clear();
		service.confirmUsedReceipt(id);
		em.flush();
		em.clear();

		assertThrows(ConflictException.class, () -> itemService.addProductToUsedReceipt(id, item("Tele", 50)));
		assertThrows(ConflictException.class, () -> service.archiveUsedReceipt(id));

		login(true);
		itemService.addProductToUsedReceipt(id, item("Tele", 50));
		em.flush();
		em.clear();
		assertEquals(150.0, service.getUsedReceiptDetail(id).amount());
	}

	@Test void archiveHidesFromOperatorsAndRestoreKeepsStatus() {
		Long id = service.createUsedReceipt(receipt(LocalDate.of(2026, 2, 10), false)).id();
		service.archiveUsedReceipt(id);
		assertThrows(NotFoundException.class, () -> service.getUsedReceiptDetail(id));
		assertTrue(service.getAllUsedReceipts(false, Sort.by("id")).stream().noneMatch(r -> r.id().equals(id)));

		login(true);
		assertEquals(Status.DRAFT, service.restoreUsedReceipt(id).status());
		assertFalse(service.getUsedReceiptDetail(id).archived());
	}

	@Test void hardDeleteOnlyWhenArchived() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		Long productId = receipt.items().get(0).product().id();

		assertThrows(ConflictException.class, () -> service.deleteUsedReceipt(receipt.id(), UsedReceiptService.LedgerChoice.IGNORE));
		service.archiveUsedReceipt(receipt.id());
		service.deleteUsedReceipt(receipt.id(), UsedReceiptService.LedgerChoice.IGNORE);
		em.flush();
		em.clear();

		assertThrows(NotFoundException.class, () -> service.getUsedReceiptDetail(receipt.id()));
		assertTrue(products.findById(productId).isEmpty());
	}

	private UsedReceiptInput input(UsedReceiptDetailDTO receipt, LocalDate date) {
		return new UsedReceiptInput(date, null, receipt.customer() == null ? null : new CustomerInput(receipt.customer().id(), "Mario", "Rossi", "Via Roma 1", "15057", "Tortona", "AL", "CI", "AB123", "RSSMRA80A01L304X"), false);
	}

	@Test void registeredReceiptIsLinkedToItsLedgerEntry() {
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");

		LedgerEntryEntity row = ledger.findByUsedReceiptId(receipt.id()).orElseThrow();
		assertEquals(row.getId(), service.getUsedReceiptDetail(receipt.id()).ledgerEntryId());
		assertNull(service.getUsedReceiptDetail(receipt.id()).ledgerAlignment());
	}

	@Test void editingARegisteredReceiptProposesAlignmentWithoutTouchingTheLedger() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		Long productId = receipt.items().get(0).id();

		itemService.updateProductInUsedReceipt(receipt.id(), productId, item("Strato", 80));
		em.flush();
		em.clear();
		service.updateUsedReceipt(receipt.id(), input(receipt, LocalDate.of(2026, 3, 1)));
		em.flush();
		em.clear();

		var alignment = service.getUsedReceiptDetail(receipt.id()).ledgerAlignment();
		assertEquals(LedgerAlignmentDTO.Action.UPDATE, alignment.action());
		assertEquals(100.0, alignment.current().amount());
		assertEquals(80.0, alignment.proposed().amount());
		assertEquals(LocalDate.of(2026, 3, 1), alignment.proposed().date());
		LedgerEntryEntity row = ledger.findByUsedReceiptId(receipt.id()).orElseThrow();
		assertEquals(100.0, row.getAmount());
		assertEquals(LocalDate.of(2026, 2, 10), row.getDate());

		service.alignLedger(receipt.id());
		em.flush();
		em.clear();

		row = ledger.findByUsedReceiptId(receipt.id()).orElseThrow();
		assertEquals(80.0, row.getAmount());
		assertEquals(LocalDate.of(2026, 3, 1), row.getDate());
		assertNull(service.getUsedReceiptDetail(receipt.id()).ledgerAlignment());
		assertEquals("ALIGN", audit.findAll().get(audit.findAll().size() - 1).getOperation());
	}

	@Test void ignoringLeavesTheLedgerAndIsAudited() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		service.updateUsedReceipt(receipt.id(), input(receipt, LocalDate.of(2026, 2, 20)));
		em.flush();
		em.clear();

		service.ignoreLedger(receipt.id());
		em.flush();
		em.clear();

		assertEquals(LocalDate.of(2026, 2, 10), ledger.findByUsedReceiptId(receipt.id()).orElseThrow().getDate());
		assertTrue(audit.findAll().stream().anyMatch(l -> "IGNORE".equals(l.getOperation()) && receipt.id().equals(l.getEntityId())));
		assertThrows(ConflictException.class, () -> service.ignoreLedger(receipt.id()));
	}

	@Test void archivedReceiptProposesArchivingTheLedgerEntryAndDeletionNeedsAChoice() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");

		service.archiveUsedReceipt(receipt.id());
		em.flush();
		em.clear();
		assertEquals(LedgerAlignmentDTO.Action.ARCHIVE, service.getUsedReceiptDetail(receipt.id()).ledgerAlignment().action());
		assertEquals("USED_RECEIPT_LEDGER_CHOICE_REQUIRED",
				assertThrows(ConflictException.class, () -> service.deleteUsedReceipt(receipt.id(), null)).getCode());

		service.alignLedger(receipt.id());
		em.flush();
		em.clear();
		assertTrue(ledger.findByUsedReceiptId(receipt.id()).orElseThrow().isArchived());

		service.deleteUsedReceipt(receipt.id(), null);
		em.flush();
		em.clear();
		assertTrue(ledger.findAll().stream().anyMatch(e -> e.isArchived() && e.getUsedReceiptId() == null && "Pagato materiale usato".equals(e.getDescription())));
	}

	@Test void deletingWithIgnoreKeepsTheLedgerEntryVisible() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		service.archiveUsedReceipt(receipt.id());
		long rowId = ledger.findByUsedReceiptId(receipt.id()).orElseThrow().getId();

		service.deleteUsedReceipt(receipt.id(), UsedReceiptService.LedgerChoice.IGNORE);
		em.flush();
		em.clear();

		LedgerEntryEntity row = ledger.findById(rowId).orElseThrow();
		assertFalse(row.isArchived());
		assertNull(row.getUsedReceiptId());
	}

	@Test void registeredReceiptKeepsItsYearAndOnlyAdminSeesTheAlignment() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");

		ConflictException e = assertThrows(ConflictException.class, () -> service.updateUsedReceipt(receipt.id(), input(receipt, LocalDate.of(2027, 1, 2))));
		assertEquals("USED_RECEIPT_YEAR_FROZEN", e.getCode());
		service.updateUsedReceipt(receipt.id(), input(receipt, LocalDate.of(2026, 12, 31)));
		em.flush();
		em.clear();

		assertNotNull(service.getUsedReceiptDetail(receipt.id()).ledgerAlignment());
		assertEquals("1/26", service.getUsedReceiptDetail(receipt.id()).number());
		login(false);
		assertNull(service.getUsedReceiptDetail(receipt.id()).ledgerAlignment());
	}

	@Test void printableFromPendingOnAndRendersTemplate() {
		Long id = service.createUsedReceipt(receipt(LocalDate.of(2026, 2, 10), true)).id();
		assertThrows(ConflictException.class, () -> service.getUsedReceiptPrint(id));

		itemService.addProductToUsedReceipt(id, item("Strato", 1234.5));
		em.flush();
		em.clear();
		service.confirmUsedReceipt(id);
		em.flush();
		em.clear();

		var pending = service.getUsedReceiptPrint(id);
		assertNull(pending.number());
		assertNull(pending.items().get(0).reference());
		assertEquals("a", pending.items().get(0).letter());

		em.flush();
		em.clear();
		service.registerUsedReceipt(id);
		em.flush();
		em.clear();

		var registered = service.getUsedReceiptPrint(id);
		String html = render(registered);
		assertEquals("1/26", registered.number());
		assertEquals("10 febbraio 2026", registered.date());
		assertTrue(html.contains("1.234,50"));
		assertTrue(html.contains("DICHIARAZIONE DI COMPRAVENDITA DI BENI USATI"));
		assertTrue(html.contains("Rossi Mario"));
		assertTrue(html.contains("Via Roma 1"));
		assertTrue(html.contains("CI AB123"));
		assertTrue(html.contains("Regolamento (UE) 2016/679"));

		login(true);
		service.archiveUsedReceipt(id);
		assertThrows(ConflictException.class, () -> service.getUsedReceiptPrint(id));
	}

	private String render(Object receipt) {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		Context context = new Context();
		context.setVariable("receipt", receipt);
		return engine.process("usedReceipt", context);
	}

	@Test void conditionFilterAndReferenceSearch() {
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato usata");

		var used = products.findAll(ProductSpecification.withFilters(null, "strato usata", null, null, ProductCondition.USED, false), PageRequest.of(0, 10));
		var newOnes = products.findAll(ProductSpecification.withFilters(null, "strato usata", null, null, ProductCondition.NEW, false), PageRequest.of(0, 10));
		assertEquals(1, used.getTotalElements());
		assertEquals(0, newOnes.getTotalElements());

		assertEquals(receipt.items().get(0).product().id(), productService.getProductDetailByCode("1/26 a").id());
		assertThrows(NotFoundException.class, () -> productService.getProductDetailByCode("1/26 B"));

		when(currentUser.canSeeCosts()).thenReturn(false);
		assertThrows(NotFoundException.class, () -> productService.getProductDetailByCode("1/26 A"));
	}

	@Test void productDetailAndTimelineReferToTheReceipt() {
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato usata");
		Long productId = receipt.items().get(0).product().id();

		assertEquals("1/26 A", productService.getProductDetail(productId).usedReference());
		assertEquals(receipt.id(), productService.getProductDetail(productId).usedReceiptId());
		var purchase = timeline.getTimeline(productId).entries().stream().filter(e -> "PURCHASE".equals(e.type())).findFirst().orElseThrow();
		assertEquals(receipt.id(), purchase.receiptId());
		assertEquals("1/26", purchase.receiptNumber());
		assertNull(purchase.invoiceId());
	}

	@Test void listShowsSaleDataAndMarginOnlyOnceSold() {
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato", "Tele");
		var sold = products.findById(receipt.items().get(0).product().id()).orElseThrow();
		SaleEntity sale = new SaleEntity();
		sale.setProduct(sold);
		sale.setDate(LocalDate.of(2026, 3, 1));
		sale.setQuantity(1);
		sale.setSalePrice(180.0);
		sale.setVat(22.0);
		sale.setReceiptNumber("123");
		em.persist(sale);
		em.flush();
		em.clear();

		var items = service.getAllUsedReceipts(false, Sort.by("id")).stream().filter(r -> r.id().equals(receipt.id())).findFirst().orElseThrow().items();
		assertEquals(80.0, items.get(0).margin());
		assertEquals(180.0, items.get(0).salePrice());
		assertEquals("123", items.get(0).saleReceipt());
		assertNull(items.get(1).salePrice());
		assertNull(items.get(1).margin());
		assertEquals("1/26 B", items.get(1).reference());
	}

	@Test void receiptIsFoundByInternalCode() {
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		assertEquals(receipt.id(), service.getUsedReceiptDetailByCode(receipt.internalCode().toLowerCase()).id());
		assertThrows(NotFoundException.class, () -> service.getUsedReceiptDetailByCode("G999999999R"));
	}

	@Test void customerRectificationLeavesRegisteredReceiptsUntouched() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		Long customerId = receipt.customer().id();

		customerService.updateCustomer(customerId, new CustomerInput(null, "Luigi", "Bianchi", "Via Po 2", "15057", "Tortona", "AL", null, null, null));
		em.flush();
		em.clear();

		assertEquals("Luigi", customerService.getCustomerDetail(customerId).firstName());
		assertEquals("Mario", service.getUsedReceiptDetail(receipt.id()).customer().firstName());
	}

	@Test void archivedCustomerIsHiddenAndCannotBeChosenOrEdited() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		Long customerId = receipt.customer().id();

		customerService.archiveCustomer(customerId);
		em.flush();
		em.clear();

		assertTrue(customerService.getAllCustomers(false).stream().noneMatch(c -> c.id().equals(customerId)));
		assertTrue(customerService.getAllCustomers(true).stream().anyMatch(c -> c.id().equals(customerId)));
		assertThrows(ConflictException.class, () -> customerService.updateCustomer(customerId, new CustomerInput(null, "Luigi", "Bianchi", "Via Po 2", "15057", "Tortona", "AL", null, null, null)));
		assertThrows(ConflictException.class, () -> service.createUsedReceipt(receipt(LocalDate.of(2026, 3, 1), true, customerId)));
		// La ricevuta che lo ha già collegato resta modificabile
		assertEquals(customerId, service.updateUsedReceipt(receipt.id(), input(receipt, LocalDate.of(2026, 2, 11))).customer().id());

		assertEquals(customerId, customerService.restoreCustomer(customerId).id());
		assertTrue(customerService.getAllCustomers(false).stream().anyMatch(c -> c.id().equals(customerId)));
	}

	@Test void customerIsDeletedPhysicallyOnlyIfArchivedAndReceiptsKeepTheirCopy() {
		login(true);
		UsedReceiptDetailDTO receipt = registered(LocalDate.of(2026, 2, 10), true, "Strato");
		Long customerId = receipt.customer().id();

		assertThrows(ConflictException.class, () -> customerService.deleteCustomer(customerId));
		customerService.archiveCustomer(customerId);
		em.flush();
		em.clear();
		customerService.deleteCustomer(customerId);
		em.flush();
		em.clear();

		assertThrows(NotFoundException.class, () -> customerService.getCustomerDetail(customerId));
		UsedReceiptDetailDTO after = service.getUsedReceiptDetail(receipt.id());
		assertNull(after.customer().id());
		assertEquals("Rossi", after.customer().lastName());
		assertEquals("Tortona", after.customer().city());
	}

}
