package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItemEntity, Long> {

	List<PurchaseItemEntity> findByPurchaseId(Long purchaseId);
	List<PurchaseItemEntity> findByProductId(Long productId);

}
