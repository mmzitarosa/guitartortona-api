package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsedReceiptRepository extends JpaRepository<UsedReceiptEntity, Long> {
	Optional<UsedReceiptEntity> findByInternalCodeIgnoreCase(String internalCode);

	List<UsedReceiptEntity> findAllByArchived(boolean archived, Sort sort);

	@Query("SELECT i.product FROM UsedReceiptEntity r JOIN r.items i " +
			"WHERE r.number = :number AND r.year = :year AND UPPER(i.letter) = UPPER(:letter)")
	Optional<ProductEntity> findProductByReference(@Param("number") int number, @Param("year") int year, @Param("letter") String letter);
}
