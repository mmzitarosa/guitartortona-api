package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

	List<SaleEntity> findByProductId(Long productId);
}
