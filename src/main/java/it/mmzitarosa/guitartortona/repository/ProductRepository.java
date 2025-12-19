package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

	Optional<ProductEntity> findByCodeIgnoreCaseOrInternalCodeIgnoreCase(String code, String internalCode);

}
