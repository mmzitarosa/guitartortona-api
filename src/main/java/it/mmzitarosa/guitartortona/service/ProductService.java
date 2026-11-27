package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.PageDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;

import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import it.mmzitarosa.guitartortona.dto.product.ProductListDTO;
import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.repository.UsedReceiptRepository;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import org.hibernate.Hibernate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.specification.ProductSpecification;
import it.mmzitarosa.guitartortona.specification.ProductVisibilitySpecification;
import org.springframework.data.jpa.domain.Specification;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service @Transactional
public class ProductService {

	/* == CONSTANTS == */
	private static final Pattern USED_RECEIPT_REFERENCE = Pattern.compile("^(\\d+)/(\\d{2})\\s+([A-Za-z]+)$");
	private final ProductRepository repository;
	private final UsedReceiptRepository usedReceiptRepository;
	private final CategoryService categoryService;
	private final BrandService brandService;
	private final CurrentUser currentUser;

	/**
	 * Garantisce che la giacenza non scenda sotto 0 applicando la variazione indicata (negativa = riduzione)
	 */
	public void assertStockNotNegative(ProductEntity product, int delta) {
		if (delta >= 0)
			return;
		int stock = Objects.requireNonNullElse(repository.findStockById(product.getId()), 0);
		if (stock + delta < 0)
			throw new ConflictException("PRODUCT_STOCK_WOULD_BE_NEGATIVE",
					"Product stock would become negative: " + product.getDescription() + " (stock " + stock + ", change " + delta + ")");
	}

	/**
	 * GET /products
	 */
	@Transactional(readOnly = true) public PageDTO<ProductListDTO> getProducts(String search, String description, Long categoryId, Long brandId, ProductCondition condition, boolean available, int page, int size, Sort sort) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
		Specification<ProductEntity> spec = ProductSpecification.withFilters(search, description, categoryId == null ? null : categoryService.getCategoryAndDescendantIds(categoryId), brandId, condition, available);
		if (!currentUser.isAdmin())
			spec = spec.and(ProductVisibilitySpecification.notOnlyInArchivedPurchases());
		return PageDTO.of(repository.findAll(spec, pageable).map(ProductListDTO::of));
	}

	/**
	 * GET /product/{id}
	 */
	@Transactional(readOnly = true) public ProductDetailDTO getProductDetail(Long id) {
		ProductEntity product = getProduct(id);
		return detail(product);
	}

	/** Il riferimento di un prodotto usato (es. 1/26 A) è riservato a user e admin. */
	private ProductDetailDTO detail(ProductEntity product) {
		String reference = null;
		if (currentUser.canSeeCosts()) {
			reference = product.getPurchaseItems().stream()
					.filter(i -> !i.getPurchase().isArchived())
					.map(i -> Hibernate.unproxy(i.getPurchase()) instanceof UsedReceiptEntity receipt ? receipt.getItemReference(i.getLetter()) : null)
					.filter(Objects::nonNull).findFirst().orElse(null);
		}
		return ProductDetailDTO.of(product, reference);
	}

	/**
	 * GET /product?code={code}
	 * Oltre a codice e codice interno accetta il riferimento di una ricevuta dell'usato (es. 1/26 A), riservato a user e admin.
	 */
	@Transactional(readOnly = true) public ProductDetailDTO getProductDetailByCode(String code) {
		Matcher reference = USED_RECEIPT_REFERENCE.matcher(code.trim());
		if (reference.matches()) {
			if (!currentUser.canSeeCosts())
				throw new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + code);
			return detail(usedReceiptRepository.findProductByReference(Integer.parseInt(reference.group(1)), 2000 + Integer.parseInt(reference.group(2)), reference.group(3))
					.filter(this::isVisible)
					.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + code)));
		}

		ProductEntity product = repository.findByCodeIgnoreCaseOrInternalCodeIgnoreCase(code, code)
				.filter(this::isVisible)
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + code));
		return detail(product);
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
				.filter(this::isVisible)
				.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + id));
	}

	/**
	 * Helper: i non admin non vedono i prodotti creati solo da fatture archiviate
	 */
	private boolean isVisible(ProductEntity product) {
		return currentUser.isAdmin() || repository.exists(ProductVisibilitySpecification.hasId(product.getId())
				.and(ProductVisibilitySpecification.notOnlyInArchivedPurchases()));
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
	 * Helper: Crea prodotto usato (nessun codice produttore, nessun punto di riordino)
	 */
	protected ProductEntity createUsedProduct(ProductInput input) {
		return createNewProduct(forUsed(input), ProductCondition.USED);
	}

	/**
	 * Helper: Modifica prodotto usato
	 */
	protected ProductEntity updateUsedProduct(ProductEntity product, ProductInput input) {
		return updateProduct(product, forUsed(input));
	}

	private ProductInput forUsed(ProductInput input) {
		return new ProductInput(input.id(), null, input.category(), input.brand(), input.description(), input.price(), null, input.notes());
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