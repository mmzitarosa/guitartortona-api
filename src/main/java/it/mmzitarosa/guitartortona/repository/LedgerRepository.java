package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntryEntity, Long> {
	
	List<LedgerEntryEntity> findAllByDateBetweenOrderByDateAsc(Date from, Date to);
	Page<LedgerEntryEntity> findAllByDateBetweenOrderByDateAsc(Date from, Date to, Pageable pageable);
	
}
