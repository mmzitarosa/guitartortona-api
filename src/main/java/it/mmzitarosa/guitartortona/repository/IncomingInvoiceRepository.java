package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface IncomingInvoiceRepository extends JpaRepository<IncomingInvoiceEntity, Long> {
	Page<IncomingInvoiceEntity> findAllByStatusIn(Collection<Status> statuses, Pageable pageable);
	long countByStatus(Status status);
}
