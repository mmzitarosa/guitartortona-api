package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.StockAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustmentEntity, Long> {

	List<StockAdjustmentEntity> findByProductId(Long productId);
}
