package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IncomingInvoiceRepository extends JpaRepository<IncomingInvoiceEntity, Long> {
	List<IncomingInvoiceEntity> findAllByArchived(boolean archived, Sort sort);

	boolean existsBySupplierAndNumber(SupplierEntity supplier, String number);

	@Query("SELECT ii FROM IncomingInvoiceEntity ii " +
			"LEFT JOIN FETCH ii.items pi " +
			"LEFT JOIN FETCH pi.product p " +
			"LEFT JOIN FETCH p.brand " +
			"LEFT JOIN FETCH p.category " +
			"JOIN FETCH ii.supplier " +
			"WHERE ii.id = :id")
	Optional<IncomingInvoiceEntity> findByIdWithDetails(@Param("id") Long id);



}
