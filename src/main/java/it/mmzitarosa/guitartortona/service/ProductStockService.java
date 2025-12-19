package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.product.ProductLightDTO;
import it.mmzitarosa.guitartortona.mapper.ProductStockMapper;
import it.mmzitarosa.guitartortona.repository.view.ProductStockRepository;
import it.mmzitarosa.guitartortona.specification.ProductSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
public class ProductStockService {

	private final ProductStockRepository repository;
	private final ProductStockMapper mapper;

	public ProductStockService(ProductStockRepository repository, ProductStockMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public List<ProductLightDTO> readProducts(boolean archived, Sort sort) {
		return mapper.toDto(repository.findAllByArchived(archived, sort));
	}

	public List<ProductLightDTO> searchProducts(String search, Sort sort) {
		return mapper.toDto(repository.findAll(ProductSpecification.withSearch(search), sort));
	}


}
