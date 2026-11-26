package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
