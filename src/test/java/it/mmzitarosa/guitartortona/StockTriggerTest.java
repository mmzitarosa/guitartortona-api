package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StockTriggerTest {

	static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11").withInitScript("db/migration/init.sql");

	static {
		MARIADB.start();
	}

	@DynamicPropertySource static void props(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MARIADB::getJdbcUrl);
		registry.add("spring.datasource.username", MARIADB::getUsername);
		registry.add("spring.datasource.password", MARIADB::getPassword);
	}

	@Autowired JdbcTemplate jdbc;
	@Autowired EntityManager em;
	@Autowired ProductRepository products;
	@Autowired CategoryRepository categories;
	@Autowired IncomingInvoiceRepository invoices;
	@Autowired PurchaseItemRepository items;
	@Autowired SupplierRepository suppliers;

	// Un prodotto nuovo viene aggiornato dopo l'insert (codice interno): non deve azzerare il valore scritto dai trigger
	@Test void pendingStockSurvivesNewProductInSameTransaction() {
		CategoryEntity category = new CategoryEntity();
		category.setName("Chitarra");
		category = categories.save(category);

		IncomingInvoiceEntity invoice = new IncomingInvoiceEntity();
		invoice.setStatus(Status.DRAFT);
		invoice.setArchived(false);
		invoice.setDate(java.time.LocalDate.now());
		invoice.setAmount(10.0);
		invoice.setNumber("T1");
		SupplierEntity supplier = new SupplierEntity();
		supplier.setName("Fornitore");
		invoice.setSupplier(suppliers.save(supplier));
		invoice = invoices.save(invoice);

		ProductEntity product = new ProductEntity();
		product.setCategory(category);
		product.setCondition(ProductCondition.NEW);
		product.setDescription("Test");
		product.setPrice(1.0);
		product = products.save(product);

		PurchaseItemEntity item = new PurchaseItemEntity();
		item.setPurchase(invoice);
		item.setProduct(product);
		item.setQuantity(1);
		items.save(item);
		em.flush();

		assertEquals(1, jdbc.queryForObject("SELECT stock_pending FROM product WHERE id = ?", Integer.class, product.getId()));

		invoice.setStatus(Status.COMPLETED);
		invoices.save(invoice);
		em.flush();

		assertEquals(1, jdbc.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, product.getId()));
		assertEquals(0, jdbc.queryForObject("SELECT stock_pending FROM product WHERE id = ?", Integer.class, product.getId()));
	}

}
