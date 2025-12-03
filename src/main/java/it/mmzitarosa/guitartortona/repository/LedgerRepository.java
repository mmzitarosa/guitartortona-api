package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntryEntity, Long> {
	
	List<LedgerEntryEntity> findAllByStatusAndDateBetweenOrderByDateAsc(Status status, LocalDate from, LocalDate to);
	Page<LedgerEntryEntity> findAllByStatusAndDateBetweenOrderByDateAsc(Status status, LocalDate from, LocalDate to, Pageable pageable);

	List<LedgerEntryEntity> findAllByStatus(Status status);
	Page<LedgerEntryEntity> findAllByStatus(Status status, Pageable pageable);


}
