package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.product.CreateProductDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductDTO;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.mapper.ProductMapper;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class ProductService {

	/* == CONSTANTS == */
	private final ProductRepository repository;
	private final ProductMapper mapper;
	private final CategoryService categoryService;
	private final BrandService brandService;

	/* == CONSTRUCTOR == */
	public ProductService(ProductRepository repository, ProductMapper mapper, CategoryService categoryService, BrandService brandService) {
		this.repository = repository;
		this.mapper = mapper;
		this.categoryService = categoryService;
		this.brandService = brandService;
	}

	/* == PUBLIC METHODS == */
	public ProductDTO createProduct(CreateProductDTO dto) {
		ProductEntity product = createProduct(dto, ProductCondition.NEW);
		// Converto l'entity dell prodotto in DTO
		return mapper.toDto(product);
	}

	public ProductDTO readProduct(long id) {
		return mapper.toDto(getProduct(id));
	}

	public ProductDTO readProductByCode(String code) {
		return mapper.toDto(repository.findByCodeIgnoreCaseOrInternalCodeIgnoreCase(code, code).orElseThrow(() -> new IllegalArgumentException("Product not found")));
	}

	public List<ProductDTO> readProducts(Status status) {
		return mapper.toDto(repository.findAllByStatus(status));
	}

	public Page<ProductDTO> readProducts(Pageable pageable) {
		return mapper.toDto(repository.findAllByStatus(Status.COMPLETED, pageable));
	}

	public ProductDTO updateProduct(long id, CreateProductDTO dto) {
		ProductEntity product = getProduct(id);
		// Converto il DTO in oggetto, passo anche la categoria e la marca recuperata da DB o la creo
		product = mapper.toEntity(product, dto, categoryService.getCategory(dto.getCategoryId()), brandService.getOrInsertBrand(dto.getBrandId(), dto.getBrandName()));
		// Salvo prodotto su DB
		product = repository.save(product);
		// Converto l'entity della fattura in DTO
		return mapper.toDto(product);
	}

	public void deleteProduct(long id) {
		repository.deleteById(id);
	}

	/* == PACKAGE METHODS == */
	ProductEntity getProduct(long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
	}

	ProductEntity createProduct(CreateProductDTO dto, ProductCondition condition) {
		// Converto il DTO in oggetto, passo anche la categoria e la marca recuperata da DB o la creo
		ProductEntity product = mapper.toEntity(dto, categoryService.getCategory(dto.getCategoryId()), brandService.getOrInsertBrand(dto.getBrandId(), dto.getBrandName()));
		// Essendo primo inserimento lo salvo come bozza
		product.setStatus(Status.DRAFT);
		product.setCondition(condition);
		// Salvo prodotto su DB
		return repository.save(product);
	}
}
