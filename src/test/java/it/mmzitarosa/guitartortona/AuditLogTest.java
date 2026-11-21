package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.audit.AuditEventListener;
import it.mmzitarosa.guitartortona.dto.product.ProductTimelineDTO;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import it.mmzitarosa.guitartortona.service.ProductTimelineService;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none",
		"app.security.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({AuditEventListener.class, CurrentUser.class, ProductTimelineService.class})
class AuditLogTest {

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
	@Autowired ProductTimelineService timeline;

	private List<Map<String, Object>> log(long productId) {
		return jdbc.queryForList("SELECT operation, field, old_value, new_value FROM audit_log WHERE entity_type = 'Product' AND entity_id = ? ORDER BY id", productId);
	}

	// Creazione, modifica per campo ed eliminazione sono registrate; il codice interno generato e la giacenza no
	@Test void recordsCreateUpdateAndDelete() {
		CategoryEntity category = new CategoryEntity();
		category.setName("Chitarra");
		category = categories.save(category);

		ProductEntity product = new ProductEntity();
		product.setCategory(category);
		product.setCondition(ProductCondition.NEW);
		product.setDescription("Vecchia");
		product.setPrice(1.0);
		product = products.save(product);
		em.flush();

		List<Map<String, Object>> afterCreate = log(product.getId());
		assertEquals(1, afterCreate.size());
		assertEquals("CREATE", afterCreate.get(0).get("operation"));
		assertTrue(afterCreate.get(0).get("new_value").toString().contains("Vecchia"));

		product.setDescription("Nuova");
		product.setPrice(2.0);
		em.flush();

		List<Map<String, Object>> afterUpdate = log(product.getId());
		assertEquals(3, afterUpdate.size());
		Map<String, Object> description = afterUpdate.stream().filter(r -> "description".equals(r.get("field"))).findFirst().orElseThrow();
		assertEquals("Vecchia", description.get("old_value"));
		assertEquals("Nuova", description.get("new_value"));
		assertTrue(afterUpdate.stream().anyMatch(r -> "price".equals(r.get("field"))));

		products.delete(product);
		em.flush();

		List<Map<String, Object>> afterDelete = log(product.getId());
		assertEquals("DELETE", afterDelete.get(afterDelete.size() - 1).get("operation"));
		assertTrue(afterDelete.stream().noneMatch(r -> "internalCode".equals(r.get("field")) || "stock".equals(r.get("field"))));
	}

	// L'acquisto riporta i valori originali della fattura; le modifiche dello stesso giorno diventano un'unica voce
	@Test void timelineKeepsOriginalPurchaseValuesAndMergesChanges() {
		CategoryEntity category = new CategoryEntity();
		category.setName("Chitarra");
		category = categories.save(category);
		ProductEntity product = new ProductEntity();
		product.setCategory(category);
		product.setCondition(ProductCondition.NEW);
		product.setDescription("Test");
		product.setPrice(1.0);
		product = products.save(product);

		SupplierEntity supplier = new SupplierEntity();
		supplier.setName("Fornitore");
		IncomingInvoiceEntity invoice = new IncomingInvoiceEntity();
		invoice.setStatus(Status.DRAFT);
		invoice.setDate(java.time.LocalDate.now());
		invoice.setAmount(10.0);
		invoice.setNumber("T1");
		invoice.setSupplier(suppliers.save(supplier));
		invoice = invoices.save(invoice);

		PurchaseItemEntity item = new PurchaseItemEntity();
		item.setPurchase(invoice);
		item.setProduct(product);
		item.setQuantity(1);
		item.setPurchasePrice(100.0);
		item.setVat(22.0);
		item = items.save(item);
		em.flush();

		item.setPurchasePrice(200.0);
		item.setVat(4.0);
		em.flush();
		item.setPurchasePrice(300.0);
		item.setQuantity(2);
		em.flush();
		em.clear();

		List<ProductTimelineDTO.Entry> entries = timeline.getTimeline(product.getId()).entries();
		ProductTimelineDTO.Entry purchase = entries.stream().filter(e -> ProductTimelineDTO.PURCHASE.equals(e.type())).findFirst().orElseThrow();
		assertEquals(100.0, purchase.price());
		assertEquals(22.0, purchase.vat());
		assertEquals(1, purchase.quantity());
		assertTrue(purchase.superseded().containsAll(List.of("purchasePrice", "vat", "quantity")));

		List<ProductTimelineDTO.Entry> changes = entries.stream().filter(e -> ProductTimelineDTO.PURCHASE_CHANGE.equals(e.type())).toList();
		assertEquals(1, changes.size());
		assertEquals(300.0, changes.get(0).price());
		assertEquals(100.0, changes.get(0).previousPrice());
		assertEquals(4.0, changes.get(0).vat());
		assertEquals(22.0, changes.get(0).previousVat());
		assertEquals(2, changes.get(0).quantity());
		assertEquals("T1", changes.get(0).invoiceNumber());
	}

}
