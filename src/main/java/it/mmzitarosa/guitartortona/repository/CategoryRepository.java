package it.mmzitarosa.guitartortona.repository;

import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

}
