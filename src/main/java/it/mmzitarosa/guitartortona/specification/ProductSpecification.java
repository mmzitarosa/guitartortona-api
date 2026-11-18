package it.mmzitarosa.guitartortona.specification;

import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
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
	 * search: ricerca libera su descrizione, codici, marca e categoria (con ordinamento per pertinenza).
	 * description: filtro sulla sola descrizione.
	 * categoryIds: categoria e relative sotto-categorie.
	 */
	public static Specification<ProductEntity> withFilters(String search, String description, Collection<Long> categoryIds, Long brandId, boolean available) {
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
			if (available)
				predicates.add(cb.or(cb.greaterThan(root.get("stock"), 0), cb.greaterThan(root.get("stockPending"), 0)));

			if (search != null && !search.isBlank()) {
				Join<ProductEntity, BrandEntity> brand = root.join("brand", JoinType.LEFT);
				Join<ProductEntity, CategoryEntity> category = root.join("category", JoinType.LEFT);

				Expression<String> code = cb.lower(cb.coalesce(root.get("code"), ""));
				Expression<String> internalCode = cb.lower(cb.coalesce(root.get("internalCode"), ""));
				List<Expression<String>> fields = List.of(
						cb.lower(cb.coalesce(root.get("description"), "")),
						code,
						internalCode,
						cb.lower(cb.coalesce(brand.get("name"), "")),
						cb.lower(cb.coalesce(category.get("name"), "")));

				// Ogni parola deve comparire in almeno un campo
				for (String token : search.toLowerCase().trim().split("\\s+")) {
					String pattern = "%" + escapeLike(token) + "%";
					predicates.add(cb.or(fields.stream()
							.map(f -> cb.like(f, pattern, '\\'))
							.toArray(Predicate[]::new)));
				}

				// Pertinenza: codice esatto, poi codice che inizia con la ricerca, poi il resto.
				// L'ordinamento si applica solo alla query dei dati, non al count.
				if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
					String exact = search.toLowerCase().trim();
					Expression<Integer> relevance = cb.<Integer>selectCase()
							.when(cb.or(cb.equal(code, exact), cb.equal(internalCode, exact)), 0)
							.when(cb.or(cb.like(code, escapeLike(exact) + "%", '\\'),
									cb.like(internalCode, escapeLike(exact) + "%", '\\')), 1)
							.otherwise(2);
					query.orderBy(cb.asc(relevance), cb.desc(root.get("id")));
				}
			}

			return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
