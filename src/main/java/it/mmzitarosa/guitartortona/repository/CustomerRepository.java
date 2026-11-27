package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.CustomerEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

	List<CustomerEntity> findAllByArchived(boolean archived, Sort sort);

}
