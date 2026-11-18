package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.exception.NotFoundException;

import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import it.mmzitarosa.guitartortona.dto.product.ProductListDTO;
import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.specification.ProductSpecification;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service @Transactional
public class ProductService {

	/* == CONSTANTS == */
	private final ProductRepository repository;
	private final CategoryService categoryService;
	private final BrandService brandService;

	/**
	 * GET /products
	 */
	@Transactional(readOnly = true) public List<ProductListDTO> getAllProducts(Sort sort) {
		return repository.findAll(sort).stream()
				.map(ProductListDTO::of)
				.toList();
	}

	/**
	 * GET /products?search={search}
	 */
	@Deprecated @Transactional(readOnly = true) public List<ProductListDTO> searchProducts(String search, Sort sort) {
		return repository.findAll(ProductSpecification.withSearch(search), sort).stream()
				.map(ProductListDTO::of)
				.toList();
	}

	/**
	 * GET /product/{id}
	 */
	@Transactional(readOnly = true) public ProductDetailDTO getProductDetail(Long id) {
		ProductEntity product = getProduct(id);
		return ProductDetailDTO.of(product);
	}

	/**
	 * GET /product?code={code}
	 */
	@Transactional(readOnly = true) public ProductDetailDTO getProductDetailByCode(String code) {
		ProductEntity product = repository.findByCodeIgnoreCaseOrInternalCodeIgnoreCase(code, code)
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + code));
		return ProductDetailDTO.of(product);
	}

	/**
	 * PUT /product/{id}
	 */
	public ProductDetailDTO updateProduct(Long id, ProductInput input) {
		ProductEntity product = getProduct(id);

		product = updateProduct(product, input);

		return getProductDetail(id);
	}


	/**
	 * Helper: Recupera product
	 */
	protected ProductEntity getProduct(long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + id));
	}

	/**
	 * Helper: Trova o crea product.
	 * Se il prodotto esiste già, i suoi dati vengono aggiornati con quelli ricevuti.
	 */
	protected ProductEntity findOrCreateProduct(ProductInput input, ProductCondition condition) {
		ProductEntity existing = null;
		if (input.id() != null)
			existing = getProduct(input.id());
		else if (input.code() != null)
			existing = repository.findByCodeIgnoreCaseOrInternalCodeIgnoreCase(input.code(), input.code()).orElse(null);

		return existing != null ? updateProduct(existing, input) : createNewProduct(input, condition);
	}

	/**
	 * Helper: Crea nuovo product
	 */
	private ProductEntity createNewProduct(ProductInput input, ProductCondition condition) {
		BrandEntity brand = brandService.findOrCreateBrand(input.brand());
		CategoryEntity category = categoryService.getCategory(input.category().id());

		ProductEntity product = new ProductEntity();
		product.setCode(input.code());
		product.setBrand(brand);
		product.setCategory(category);
		product.setDescription(input.description());
		product.setPrice(input.price());
		product.setReorderPoint(input.reorderPoint());
		product.setNotes(input.notes());
		product.setCondition(condition);

		return repository.save(product);
	}

	/**
	 * Helper: Modifica product
	 */
	protected ProductEntity updateProduct(ProductEntity product, ProductInput input) {
		BrandEntity brand = brandService.findOrCreateBrand(input.brand());
		CategoryEntity category = categoryService.getCategory(input.category().id());

		// Se il prodotto è stato trovato scansionando il codice interno, il codice prodotto non va sovrascritto
		boolean codeIsInternalCode = input.code() != null && input.code().equalsIgnoreCase(product.getInternalCode());
		if (!codeIsInternalCode)
			product.setCode(input.code());
		product.setBrand(brand);
		product.setCategory(category);
		product.setDescription(input.description());
		product.setPrice(input.price());
		product.setReorderPoint(input.reorderPoint());
		product.setNotes(input.notes());

		return repository.save(product);
	}

}