package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.dto.product.ProductListDTO;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductSearchTest {

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
	@Autowired ProductRepository repository;

	@BeforeEach void seed() {
		jdbc.execute("DELETE FROM product");
		jdbc.execute("DELETE FROM brand");
		jdbc.execute("DELETE FROM category");
		jdbc.execute("INSERT INTO category (id, name) VALUES (1, 'Chitarra'), (2, 'Amplificatore')");
		jdbc.execute("INSERT INTO brand (id, name) VALUES (1, 'Fender'), (2, 'Marshall')");
		insert(1, "FS-001", "G-0001", 1, 1, "Stratocaster Player", 3, 0);
		insert(2, "FT-002", "G-0002", 1, 1, "Telecaster Player", 0, 2);
		insert(3, "MA-001", "G-0003", 2, 2, "Amplificatore a valvole", 0, 0);
		insert(4, null, "G-0004", 1, null, "Chitarra senza marca", 1, 0);
	}

	private void insert(long id, String code, String internalCode, long categoryId, Integer brandId, String description, int stock, int stockPending) {
		jdbc.update("INSERT INTO product (id, code, internal_code, category_id, brand_id, description, condition_id, stock, stock_pending, created_date, updated_date) VALUES (?,?,?,?,?,?,0,?,?,NOW(),NOW())",
				id, code, internalCode, categoryId, brandId, description, stock, stockPending);
	}

	private List<Long> search(String search, Long categoryId, Long brandId, boolean available) {
		return find(search, null, categoryId == null ? null : List.of(categoryId), brandId, available);
	}

	private List<Long> find(String search, String description, List<Long> categoryIds, Long brandId, boolean available) {
		return repository.findAll(ProductSpecification.withFilters(search, description, categoryIds, brandId, available),
						PageRequest.of(0, 15, search == null ? Sort.by("id") : Sort.by(Sort.Direction.DESC, "id")))
				.map(ProductListDTO::of).map(ProductListDTO::id).getContent();
	}

	@Test void noFiltersReturnsAll() {
		assertEquals(List.of(1L, 2L, 3L, 4L), search(null, null, null, false));
	}

	@Test void searchMatchesAllTokensAcrossFields() {
		assertEquals(List.of(2L, 1L), search("fender player", null, null, false));
		assertEquals(List.of(1L), search("fender strato", null, null, false));
	}

	@Test void searchByBrandAndCategoryName() {
		assertEquals(List.of(3L), search("marshall", null, null, false));
		assertEquals(List.of(4L, 2L, 1L), search("chitarra", null, null, false));
	}

	@Test void searchWorksWithoutBrand() {
		assertEquals(List.of(4L), search("senza marca", null, null, false));
	}

	@Test void searchDoesNotMatchCodes() {
		assertEquals(List.of(), search("fs-001", null, null, false));
		assertEquals(List.of(), search("g-0003", null, null, false));
	}

	@Test void likeWildcardsAreEscaped() {
		assertEquals(List.of(), search("%", null, null, false));
	}

	@Test void filtersByCategoryBrandAndAvailability() {
		assertEquals(List.of(1L, 2L, 4L), search(null, 1L, null, false));
		assertEquals(List.of(1L, 2L), search(null, null, 1L, false));
		assertEquals(List.of(1L, 2L, 4L), search(null, null, null, true));
	}

	@Test void descriptionFilterIgnoresOtherFields() {
		assertEquals(List.of(1L, 2L), find(null, "player", null, null, false));
		assertEquals(List.of(), find(null, "fender", null, null, false));
	}

	@Test void categoryFilterIncludesSubCategories() {
		jdbc.execute("INSERT INTO category (id, name, parent_category_id) VALUES (3, 'Chitarra Elettrica', 1)");
		insert(5, "CE-001", "G-0005", 3, 1, "Les Paul", 1, 0);
		assertEquals(List.of(1L, 2L, 4L, 5L), find(null, null, List.of(1L, 3L), null, false));
		assertEquals(List.of(5L), find(null, null, List.of(3L), null, false));
	}

	@Test void countQueryIsConsistentWithPaging() {
		var page = repository.findAll(ProductSpecification.withFilters("player", null, null, null, false), PageRequest.of(0, 1));
		assertEquals(2, page.getTotalElements());
		assertEquals(1, page.getContent().size());
	}
}
