package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

	Optional<ProductEntity> findByCodeIgnoreCaseOrInternalCodeIgnoreCase(String code, String internalCode);
	List<ProductEntity> findAllByArchived(boolean archived, Sort sort);

}
