package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
	
	Optional<ProductEntity> findByCodeIgnoreCaseOrInternalCodeIgnoreCase(String code, String internalCode);
	List<ProductEntity> findAllByStatus(Status status);
	Page<ProductEntity> findAllByStatus(Status status, Pageable pageable);

}
