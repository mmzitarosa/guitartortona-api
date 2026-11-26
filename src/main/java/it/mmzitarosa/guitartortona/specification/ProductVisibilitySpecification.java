package it.mmzitarosa.guitartortona.specification;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class ProductVisibilitySpecification {

	private ProductVisibilitySpecification() {}

	/**
	 * Esclude i prodotti i cui acquisti sono tutti archiviati (creati da una fattura eliminata e mai usati altrove).
	 * Serve per i non admin: i prodotti senza acquisti restano visibili.
	 */
	public static Specification<ProductEntity> notOnlyInArchivedPurchases() {
		return (root, query, cb) -> {
			Subquery<Long> archived = query.subquery(Long.class);
			var archivedItem = archived.from(PurchaseItemEntity.class);
			archived.select(archivedItem.get("id")).where(
					cb.equal(archivedItem.get("product"), root),
					cb.isTrue(archivedItem.get("purchase").get("archived")));

			Subquery<Long> active = query.subquery(Long.class);
			var activeItem = active.from(PurchaseItemEntity.class);
			active.select(activeItem.get("id")).where(
					cb.equal(activeItem.get("product"), root),
					cb.isFalse(activeItem.get("purchase").get("archived")));

			return cb.not(cb.and(cb.exists(archived), cb.not(cb.exists(active))));
		};
	}

	public static Specification<ProductEntity> hasId(Long id) {
		return (root, query, cb) -> cb.equal(root.get("id"), id);
	}

}
