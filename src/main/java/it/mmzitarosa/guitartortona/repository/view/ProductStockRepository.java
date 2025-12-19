package it.mmzitarosa.guitartortona.repository.view;

import it.mmzitarosa.guitartortona.entity.view.ProductStockView;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductStockRepository extends JpaRepository<ProductStockView, Long>, JpaSpecificationExecutor<ProductStockView> {
	List<ProductStockView> findAllByArchived(boolean archived, Sort sort);
}
