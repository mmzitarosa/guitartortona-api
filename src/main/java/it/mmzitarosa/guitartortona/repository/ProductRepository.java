package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

	List<ProductEntity> findAllByStockGreaterThanOrStockPendingGreaterThan(Integer stock, Integer stockPending, Sort sort);
	/** Legge la giacenza dal DB: l'entità può essere obsoleta perché la giacenza è aggiornata dai trigger */
	@Query("SELECT p.stock FROM ProductEntity p WHERE p.id = :id")
	Integer findStockById(@Param("id") Long id);
	Optional<ProductEntity> findByInternalCodeIgnoreCase(String internalCode);
	Optional<ProductEntity> findByCodeIgnoreCaseOrInternalCodeIgnoreCase(String code, String internalCode);

}
