package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceListDTO;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.service.IncomingInvoiceService;
import it.mmzitarosa.guitartortona.service.SupplierService;
import it.mmzitarosa.guitartortona.specification.ProductVisibilitySpecification;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MariaDBContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({IncomingInvoiceService.class, SupplierService.class})
class InvoiceArchiveTest {

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
	@Autowired IncomingInvoiceService service;
	@Autowired IncomingInvoiceRepository invoices;
	@Autowired PurchaseItemRepository items;
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
		supplier = new SupplierEntity();
		supplier.setName("Fornitore");
		supplier = suppliers.save(supplier);
		asAdmin(false);
	}

	private void asAdmin(boolean admin) {
		when(currentUser.isAdmin()).thenReturn(admin);
	}

	private IncomingInvoiceEntity invoice(String number, Status status) {
		IncomingInvoiceEntity invoice = new IncomingInvoiceEntity();
		invoice.setSupplier(supplier);
		invoice.setNumber(number);
		invoice.setDate(LocalDate.now());
		invoice.setAmount(10.0);
		invoice.setStatus(status);
		return invoices.save(invoice);
	}

	private ProductEntity productOn(IncomingInvoiceEntity... purchases) {
		ProductEntity product = new ProductEntity();
		product.setCategory(category);
		product.setCondition(ProductCondition.NEW);
		product.setDescription("Prodotto");
		product = products.save(product);
		for (IncomingInvoiceEntity purchase : purchases) {
			PurchaseItemEntity item = new PurchaseItemEntity();
			item.setPurchase(purchase);
			item.setProduct(product);
			item.setQuantity(1);
			items.save(item);
		}
		em.flush();
		return product;
	}

	private boolean visibleToUser(ProductEntity product) {
		return products.exists(ProductVisibilitySpecification.hasId(product.getId()).and(ProductVisibilitySpecification.notOnlyInArchivedPurchases()));
	}

	@Test void archiveKeepsStatusAndRestoreBringsItBack() {
		asAdmin(true);
		IncomingInvoiceEntity completed = invoice("C1", Status.COMPLETED);

		service.archiveIncomingInvoice(completed.getId());
		em.flush();
		assertTrue(invoices.findById(completed.getId()).orElseThrow().isArchived());
		assertEquals(Status.COMPLETED, invoices.findById(completed.getId()).orElseThrow().getStatus());

		service.restoreIncomingInvoice(completed.getId());
		em.flush();
		IncomingInvoiceEntity restored = invoices.findById(completed.getId()).orElseThrow();
		assertFalse(restored.isArchived());
		assertEquals(Status.COMPLETED, restored.getStatus());
		assertNull(restored.getArchivedDate());
	}

	@Test void archivedDateIsKeptOnLaterUpdates() {
		IncomingInvoiceEntity draft = invoice("D1", Status.DRAFT);
		service.archiveIncomingInvoice(draft.getId());
		em.flush();
		assertNotNull(invoices.findById(draft.getId()).orElseThrow().getArchivedDate());

		asAdmin(true);
		IncomingInvoiceEntity archived = invoices.findById(draft.getId()).orElseThrow();
		archived.setNotes("modificata");
		em.flush();
		assertNotNull(invoices.findById(draft.getId()).orElseThrow().getArchivedDate());
	}

	@Test void userArchivesOnlyDrafts() {
		IncomingInvoiceEntity completed = invoice("C1", Status.COMPLETED);
		assertThrows(ConflictException.class, () -> service.archiveIncomingInvoice(completed.getId()));
		assertFalse(invoices.findById(completed.getId()).orElseThrow().isArchived());

		IncomingInvoiceEntity draft = invoice("D1", Status.DRAFT);
		service.archiveIncomingInvoice(draft.getId());
		assertTrue(invoices.findById(draft.getId()).orElseThrow().isArchived());
	}

	@Test void archivedInvoiceIsHiddenFromNonAdmins() {
		IncomingInvoiceEntity draft = invoice("D1", Status.DRAFT);
		service.archiveIncomingInvoice(draft.getId());

		assertThrows(NotFoundException.class, () -> service.getIncomingInvoiceDetail(draft.getId()));
		assertTrue(service.getAllIncomingInvoices(true, Sort.unsorted()).isEmpty());
		assertTrue(service.getAllIncomingInvoices(false, Sort.unsorted()).stream().map(IncomingInvoiceListDTO::id).noneMatch(draft.getId()::equals));

		asAdmin(true);
		assertEquals(draft.getId(), service.getIncomingInvoiceDetail(draft.getId()).id());
		assertTrue(service.getAllIncomingInvoices(true, Sort.unsorted()).stream().map(IncomingInvoiceListDTO::id).anyMatch(draft.getId()::equals));
	}

	@Test void permanentDeleteRequiresArchivedInvoice() {
		asAdmin(true);
		IncomingInvoiceEntity draft = invoice("D1", Status.DRAFT);

		assertThrows(ConflictException.class, () -> service.deleteIncomingInvoice(draft.getId()));
		assertTrue(invoices.existsById(draft.getId()));

		service.archiveIncomingInvoice(draft.getId());
		service.deleteIncomingInvoice(draft.getId());
		em.flush();
		assertFalse(invoices.existsById(draft.getId()));
	}

	@Test void productsOfArchivedInvoicesAreHiddenUnlessUsedElsewhere() {
		IncomingInvoiceEntity archived = invoice("A1", Status.DRAFT);
		IncomingInvoiceEntity active = invoice("A2", Status.COMPLETED);
		ProductEntity onlyArchived = productOn(archived);
		ProductEntity shared = productOn(archived, active);
		ProductEntity withoutPurchases = productOn();

		service.archiveIncomingInvoice(archived.getId());
		em.flush();
		em.clear();

		assertFalse(visibleToUser(onlyArchived));
		assertTrue(visibleToUser(shared));
		assertTrue(visibleToUser(withoutPurchases));
	}

}
