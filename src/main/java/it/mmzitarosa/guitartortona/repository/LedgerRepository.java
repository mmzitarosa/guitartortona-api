package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntryEntity, Long> {
	
	List<LedgerEntryEntity> findAllByArchivedFalseAndDateBetween(LocalDate from, LocalDate to, Sort sort);
	List<LedgerEntryEntity> findAllByDateGreaterThanEqualAndArchived(LocalDate date, boolean archived, Sort sort);

}
