package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomingInvoiceRepository extends JpaRepository<IncomingInvoiceEntity, Long> { }
