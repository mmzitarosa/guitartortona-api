package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
	
	Optional<BrandEntity> findByNameIgnoreCase(String name);
	
}
