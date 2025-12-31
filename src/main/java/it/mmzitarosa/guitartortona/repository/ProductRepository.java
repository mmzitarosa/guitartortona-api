package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

	List<ProductEntity> findAllByStockGreaterThanOrStockPendingGreaterThan(Integer stock, Integer stockPending, Sort sort);
	Optional<ProductEntity> findByInternalCodeIgnoreCase(String internalCode);
	Optional<ProductEntity> findByCodeIgnoreCaseOrInternalCodeIgnoreCase(String code, String internalCode);

}
