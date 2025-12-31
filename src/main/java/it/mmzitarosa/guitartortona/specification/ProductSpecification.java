package it.mmzitarosa.guitartortona.specification;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

	public static Specification<ProductEntity> withSearch(String search) {
		return (root, query, cb) -> {
			if (search == null || search.isBlank()) {
				return cb.conjunction();
			}

			// Divide il termine di ricerca in parole
			String[] tokens = search.toLowerCase().trim().split("\\s+");

			List<Predicate> predicates = new ArrayList<>();

			// Per ogni token, cerca in TUTTI i campi (OR)
			for (String token : tokens) {
				String pattern = "%" + token + "%";

				List<Predicate> orPredicates = new ArrayList<>();
				orPredicates.add(cb.like(cb.lower(root.get("categoryName")), pattern));
				orPredicates.add(cb.like(cb.lower(root.get("brandName")), pattern));
				orPredicates.add(cb.like(cb.lower(root.get("description")), pattern));

				// Ogni token deve essere trovato in almeno UN campo
				predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
			}

			// Tutti i token devono essere presenti
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}

