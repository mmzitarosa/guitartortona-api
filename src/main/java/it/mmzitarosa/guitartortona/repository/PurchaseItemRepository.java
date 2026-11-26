package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItemEntity, Long> {

	@Query("SELECT COUNT(pi) > 0 FROM PurchaseItemEntity pi " +
			"WHERE pi.purchase.id = :purchaseId " +
			"AND pi.product.id = :productId")
	boolean existsByPurchaseAndProduct(@Param("purchaseId") Long purchaseId, @Param("productId") Long productId);

	@Query("SELECT COALESCE(SUM(pi.purchasePrice * pi.quantity), 0) FROM PurchaseItemEntity pi WHERE pi.purchase.id = :purchaseId")
	Double sumAmountByPurchaseId(@Param("purchaseId") Long purchaseId);

	@Query("SELECT pi FROM PurchaseItemEntity pi WHERE pi.product.id = :productId AND pi.purchase.archived = false")
	List<PurchaseItemEntity> findActiveByProductId(@Param("productId") Long productId);
}
