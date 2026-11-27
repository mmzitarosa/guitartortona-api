package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.dto.brand.BrandInput;
import it.mmzitarosa.guitartortona.dto.category.CategoryInput;
import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.ProductIncomingInvoiceInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.ProductUsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptInput;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.service.*;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MariaDBContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * La giacenza non può mai scendere sotto 0: archiviazione e modifica righe di fatture e ricevute già completate.
 */
@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UsedReceiptService.class, PurchaseItemService.class, CustomerService.class, LedgerService.class, BankService.class,
		ProductService.class, CategoryService.class, BrandService.class, IncomingInvoiceService.class, SupplierService.class})
class StockGuardTest {

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
	@Autowired IncomingInvoiceService invoiceService;
	@Autowired UsedReceiptService receiptService;
	@Autowired PurchaseItemService itemService;
	@Autowired CategoryService categoryService;
	@Autowired IncomingInvoiceRepository invoices;
	@Autowired ProductRepository products;
	@Autowired CategoryRepository categories;
	@Autowired SupplierRepository suppliers;
	@Autowired EntityManager em;

	private CategoryEntity category;
	private SupplierEntity supplier;

	@BeforeEach void seed() {
		category = new CategoryEntity();
		category.setName("Chitarra");
		category = categories.save(category);
		ReflectionTestUtils.invokeMethod(categoryService, "loadCategories");
		supplier = new SupplierEntity();
		supplier.setName("Fornitore");
		supplier = suppliers.save(supplier);
		when(currentUser.isAdmin()).thenReturn(true);
		when(currentUser.canSeeCosts()).thenReturn(true);
	}

	private ProductInput productInput(String code, String description) {
		return new ProductInput(null, code, new CategoryInput(category.getId()), new BrandInput(null, null), description, null, null, null);
	}

	private IncomingInvoiceEntity completedInvoice() {
		IncomingInvoiceEntity invoice = new IncomingInvoiceEntity();
		invoice.setSupplier(supplier);
		invoice.setNumber("F1");
		invoice.setDate(LocalDate.now());
		invoice.setAmount(10.0);
		invoice.setStatus(Status.COMPLETED);
		return invoices.save(invoice);
	}

	private Long addItem(Long invoiceId, String code, int quantity) {
		return itemService.addProductToIncomingInvoice(invoiceId, new ProductIncomingInvoiceInput(productInput(code, "Prodotto " + code), 22.0, 10.0, quantity)).id();
	}

	private void sell(Long productId, int quantity) {
		SaleEntity sale = new SaleEntity();
		sale.setProduct(products.findById(productId).orElseThrow());
		sale.setDate(LocalDate.now());
		sale.setQuantity(quantity);
		sale.setSalePrice(100.0);
		sale.setVat(22.0);
		em.persist(sale);
		em.flush();
		em.clear();
	}

	private Long productIdOf(Long purchaseItemId) {
		return em.createQuery("SELECT pi.product.id FROM PurchaseItemEntity pi WHERE pi.id = :id", Long.class).setParameter("id", purchaseItemId).getSingleResult();
	}

	private int stock(Long productId) {
		em.clear();
		return products.findById(productId).orElseThrow().getStock();
	}

	@Test void cannotArchiveCompletedInvoiceWithSoldProducts() {
		IncomingInvoiceEntity invoice = completedInvoice();
		Long itemId = addItem(invoice.getId(), "P1", 3);
		Long productId = productIdOf(itemId);
		em.flush();
		sell(productId, 2);

		var error = assertThrows(ConflictException.class, () -> invoiceService.archiveIncomingInvoice(invoice.getId()));
		assertEquals("PRODUCT_STOCK_WOULD_BE_NEGATIVE", error.getCode());
		assertFalse(invoices.findById(invoice.getId()).orElseThrow().isArchived());
		assertEquals(1, stock(productId));
	}

	@Test void canArchiveCompletedInvoiceWhenStockIsEnough() {
		IncomingInvoiceEntity invoice = completedInvoice();
		Long productId = productIdOf(addItem(invoice.getId(), "P1", 3));
		em.flush();

		invoiceService.archiveIncomingInvoice(invoice.getId());
		em.flush();
		assertEquals(0, stock(productId));
	}

	@Test void cannotReduceQuantityBelowSold() {
		IncomingInvoiceEntity invoice = completedInvoice();
		Long itemId = addItem(invoice.getId(), "P1", 3);
		Long productId = productIdOf(itemId);
		em.flush();
		sell(productId, 2);

		var input = new ProductIncomingInvoiceInput(productInput("P1", "Prodotto P1"), 22.0, 10.0, 1);
		var error = assertThrows(ConflictException.class, () -> itemService.updateProductInIncomingInvoice(invoice.getId(), itemId, input));
		assertEquals("PRODUCT_STOCK_WOULD_BE_NEGATIVE", error.getCode());

		itemService.updateProductInIncomingInvoice(invoice.getId(), itemId, new ProductIncomingInvoiceInput(productInput("P1", "Prodotto P1"), 22.0, 10.0, 2));
		em.flush();
		assertEquals(0, stock(productId));
	}

	@Test void cannotDeleteSoldLineFromCompletedInvoice() {
		IncomingInvoiceEntity invoice = completedInvoice();
		Long itemId = addItem(invoice.getId(), "P1", 3);
		Long productId = productIdOf(itemId);
		em.flush();
		sell(productId, 1);

		var error = assertThrows(ConflictException.class, () -> itemService.deleteProductFromIncomingInvoice(invoice.getId(), itemId));
		assertEquals("PRODUCT_STOCK_WOULD_BE_NEGATIVE", error.getCode());
		assertEquals(2, stock(productId));
	}

	@Test void deletingUnsoldLineFromCompletedInvoiceIsAllowed() {
		IncomingInvoiceEntity invoice = completedInvoice();
		Long itemId = addItem(invoice.getId(), "P1", 3);
		Long productId = productIdOf(itemId);
		em.flush();

		itemService.deleteProductFromIncomingInvoice(invoice.getId(), itemId);
		em.flush();
		assertEquals(0, stock(productId));
	}

	@Test void cannotArchiveRegisteredReceiptWithSoldProducts() {
		Long id = receiptService.createUsedReceipt(new UsedReceiptInput(LocalDate.of(2026, 3, 1), null,
				new CustomerInput(null, "Mario", "Rossi", "Via Roma 1", "15057", "Tortona", "AL", "CI", "AB123", "RSSMRA80A01L304X"), false)).id();
		Long itemId = itemService.addProductToUsedReceipt(id, new ProductUsedReceiptInput(productInput(null, "Strato"), null, 100.0)).id();
		Long productId = productIdOf(itemId);
		em.flush();
		em.clear();
		receiptService.confirmUsedReceipt(id);
		em.flush();
		em.clear();
		UsedReceiptDetailDTO registered = receiptService.registerUsedReceipt(id);
		em.flush();
		sell(productId, 1);

		var error = assertThrows(ConflictException.class, () -> receiptService.archiveUsedReceipt(registered.id()));
		assertEquals("USED_RECEIPT_HAS_SALES", error.getCode());
	}

}
