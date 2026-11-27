package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.audit.AuditEventListener;
import it.mmzitarosa.guitartortona.dto.product.ProductTimelineDTO;
import it.mmzitarosa.guitartortona.dto.stock.StockAdjustmentInput;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.service.BrandService;
import it.mmzitarosa.guitartortona.service.CategoryService;
import it.mmzitarosa.guitartortona.service.ProductService;
import it.mmzitarosa.guitartortona.service.ProductTimelineService;
import it.mmzitarosa.guitartortona.service.StockAdjustmentService;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none",
		"app.security.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({AuditEventListener.class, CurrentUser.class, ProductTimelineService.class, StockAdjustmentService.class, ProductService.class, CategoryService.class, BrandService.class})
class StockAdjustmentTest {

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
	@Autowired StockAdjustmentService service;
	@Autowired ProductTimelineService timeline;

	private ProductEntity productWithStock(int stock) {
		CategoryEntity category = new CategoryEntity();
		category.setName("Chitarra");
		category = categories.save(category);
		ProductEntity product = new ProductEntity();
		product.setCategory(category);
		product.setCondition(ProductCondition.NEW);
		product.setDescription("Test");
		product.setPrice(1.0);
		product = products.save(product);
		em.flush();
		jdbc.update("UPDATE product SET stock = ? WHERE id = ?", stock, product.getId());
		em.clear();
		return product;
	}

	private int stock(long id) {
		return jdbc.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, id);
	}

	// La rettifica è un movimento: il trigger riallinea la giacenza, e compare in timeline e audit
	@Test void adjustmentMovesStockAndAppearsInTimelineAndAudit() {
		ProductEntity product = productWithStock(5);

		service.adjustStock(product.getId(), new StockAdjustmentInput(3, "Conteggio annuale"));
		em.flush();
		em.clear();

		assertEquals(3, stock(product.getId()));
		ProductTimelineDTO.Entry entry = timeline.getTimeline(product.getId()).entries().stream()
				.filter(e -> ProductTimelineDTO.STOCK_ADJUSTMENT.equals(e.type())).findFirst().orElseThrow();
		assertEquals(-2, entry.quantity());
		assertEquals(5, entry.previousStock());
		assertEquals(3, entry.countedStock());
		assertEquals("Conteggio annuale", entry.notes());
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE entity_type = 'StockAdjustment' AND operation = 'CREATE' AND product_id = ?",
				Integer.class, product.getId()));
	}

	@Test void adjustmentCanRaiseStock() {
		ProductEntity product = productWithStock(1);
		service.adjustStock(product.getId(), new StockAdjustmentInput(4, "Trovati in magazzino"));
		em.flush();
		assertEquals(4, stock(product.getId()));
	}

	// La giacenza non può diventare negativa e una rettifica senza differenza non ha senso
	@Test void rejectsNegativeCountAndNoDifference() {
		ProductEntity product = productWithStock(2);
		assertThrows(Exception.class, () -> {
			service.adjustStock(product.getId(), new StockAdjustmentInput(-1, "Errore"));
			em.flush();
		});
		em.clear();
		assertEquals(2, stock(product.getId()));
		assertThrows(ConflictException.class, () -> service.adjustStock(product.getId(), new StockAdjustmentInput(2, "Uguale")));
	}

}
