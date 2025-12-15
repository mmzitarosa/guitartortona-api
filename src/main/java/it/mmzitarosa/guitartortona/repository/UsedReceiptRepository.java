package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsedReceiptRepository extends JpaRepository<UsedReceiptEntity, Long> {
	List<UsedReceiptEntity> findAllByArchived(boolean archived, Sort sort);
}
