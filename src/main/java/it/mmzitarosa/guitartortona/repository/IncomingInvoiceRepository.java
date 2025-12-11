package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;

public interface IncomingInvoiceRepository extends JpaRepository<IncomingInvoiceEntity, Long>, JpaSpecificationExecutor<IncomingInvoiceEntity> {
	Page<IncomingInvoiceEntity> findAllByArchived(boolean archived, Pageable pageable);
	Page<IncomingInvoiceEntity> findAllByStatusIn(Collection<Status> statuses, Pageable pageable);
	long countByStatus(Status status);
}
