package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.BankEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<BankEntity, Long> {

}
