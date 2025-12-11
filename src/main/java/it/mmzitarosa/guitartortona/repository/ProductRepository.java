package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

	Optional<ProductEntity> findByCodeIgnoreCaseOrInternalCodeIgnoreCase(String code, String internalCode);
	List<ProductEntity> findAllByArchived(boolean archived);
	Page<ProductEntity> findAllByStatus(Status status, Pageable pageable);

}
