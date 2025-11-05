package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomingInvoiceRepository extends JpaRepository<IncomingInvoiceEntity, Long> {
	List<IncomingInvoiceEntity> findAllByStatus(Status status);
	Page<IncomingInvoiceEntity> findAllByStatus(Status status, Pageable pageable);
}
