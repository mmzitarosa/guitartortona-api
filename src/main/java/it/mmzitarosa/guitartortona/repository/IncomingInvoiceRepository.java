package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomingInvoiceRepository extends JpaRepository<IncomingInvoiceEntity, Long> {
	List<IncomingInvoiceEntity> findAllByArchived(boolean archived, Sort sort);
}
