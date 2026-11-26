package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.UsedReceiptCounterEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsedReceiptCounterRepository extends JpaRepository<UsedReceiptCounterEntity, Integer> {

	@Modifying
	@Query(value = "INSERT IGNORE INTO used_receipt_counter (year, last_number) VALUES (:year, 0)", nativeQuery = true)
	void createIfMissing(@Param("year") int year);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM UsedReceiptCounterEntity c WHERE c.year = :year")
	Optional<UsedReceiptCounterEntity> findByYearForUpdate(@Param("year") int year);
}
