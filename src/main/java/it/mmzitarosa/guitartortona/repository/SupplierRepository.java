package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {
	
	Optional<SupplierEntity> findByNameIgnoreCase(String name);
	
}
