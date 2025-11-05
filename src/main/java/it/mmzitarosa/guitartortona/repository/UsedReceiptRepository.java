package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsedReceiptRepository extends JpaRepository<UsedReceiptEntity, Long> {
	List<UsedReceiptEntity> findAllByStatus(Status status);
	Page<UsedReceiptEntity> findAllByStatus(Status status, Pageable pageable);
}
