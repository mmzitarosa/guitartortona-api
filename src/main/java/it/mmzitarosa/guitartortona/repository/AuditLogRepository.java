package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {

	/** Tutte le righe riferite a un prodotto (il prodotto stesso, le sue righe di acquisto e le vendite), dalla più vecchia. */
	List<AuditLogEntity> findByProductIdOrderByCreatedDateAscIdAsc(Long productId);

}
