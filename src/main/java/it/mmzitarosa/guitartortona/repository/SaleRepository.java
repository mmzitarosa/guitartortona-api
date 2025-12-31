package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {
}
