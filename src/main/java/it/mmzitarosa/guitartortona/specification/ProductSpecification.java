package it.mmzitarosa.guitartortona.specification;

import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductSpecification {

	private ProductSpecification() {}

	/**
	 * Filtri combinabili (AND). Ogni parametro nullo viene ignorato.
	 * search: ricerca libera su descrizione, marca e categoria (i codici hanno la ricerca puntuale dedicata).
	 * description: filtro sulla sola descrizione.
	 * categoryIds: categoria e relative sotto-categorie.
	 */
	public static Specification<ProductEntity> withFilters(String search, String description, Collection<Long> categoryIds, Long brandId, ProductCondition condition, boolean available) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (categoryIds != null && !categoryIds.isEmpty())
				predicates.add(root.get("category").get("id").in(categoryIds));
			if (description != null && !description.isBlank()) {
				for (String token : description.toLowerCase().trim().split("\\s+"))
					predicates.add(cb.like(cb.lower(cb.coalesce(root.get("description"), "")), "%" + escapeLike(token) + "%", '\\'));
			}
			if (brandId != null)
				predicates.add(cb.equal(root.get("brand").get("id"), brandId));
			if (condition != null)
				predicates.add(cb.equal(root.get("condition"), condition));
			if (available)
				predicates.add(cb.or(cb.greaterThan(root.get("stock"), 0), cb.greaterThan(root.get("stockPending"), 0)));

			if (search != null && !search.isBlank()) {
				Join<ProductEntity, BrandEntity> brand = root.join("brand", JoinType.LEFT);
				Join<ProductEntity, CategoryEntity> category = root.join("category", JoinType.LEFT);

				List<Expression<String>> fields = List.of(
						cb.lower(cb.coalesce(root.get("description"), "")),
						cb.lower(cb.coalesce(brand.get("name"), "")),
						cb.lower(cb.coalesce(category.get("name"), "")));

				// Ogni parola deve comparire in almeno un campo
				for (String token : search.toLowerCase().trim().split("\\s+")) {
					String pattern = "%" + escapeLike(token) + "%";
					predicates.add(cb.or(fields.stream()
							.map(f -> cb.like(f, pattern, '\\'))
							.toArray(Predicate[]::new)));
				}
			}

			return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
