package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<BankEntity, Long> {
	
	Optional<SupplierEntity> findByNameIgnoreCase(String name);
	
}
