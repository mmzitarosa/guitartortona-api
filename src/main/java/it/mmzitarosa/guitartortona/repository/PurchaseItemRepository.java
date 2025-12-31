package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItemEntity, Long> {

	@Query("SELECT COUNT(pi) > 0 FROM PurchaseItemEntity pi " +
			"WHERE pi.purchase.id = :purchaseId " +
			"AND pi.product.id = :productId")
	boolean existsByPurchaseAndProduct(@Param("purchaseId") Long purchaseId, @Param("productId") Long productId);
}
