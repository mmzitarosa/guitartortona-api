package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.product.CreateProductDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductDTO;
import it.mmzitarosa.guitartortona.service.ProductService;
import it.mmzitarosa.guitartortona.utils.Constant;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/v1")
public class ProductController {

	/* == CONSTANTS == */
	private final ProductService service;

	/* == CONSTRUCTOR == */
	public ProductController(ProductService service) {
		this.service = service;
	}

	/* == CREATE == */
	@PostMapping("/product") public ProductDTO createProduct(@Valid @RequestBody CreateProductDTO request) {
		return service.createProduct(request);
	}

	/* == READ == */
	@GetMapping("/products") public Page<ProductDTO> readProducts(@PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
		return service.readProducts(pageable);
	}

	@GetMapping("/archive/products") public List<ProductDTO> readArchivedProducts() {
		return service.readProducts(Constant.Status.ARCHIVED);
	}

	@GetMapping("/drafts/products") public List<ProductDTO> readDraftedProducts() {
		return service.readProducts(Constant.Status.DRAFT);
	}

	@GetMapping("/product/{id}") public ProductDTO readProduct(@PathVariable long id) {
		return service.readProduct(id);
	}

	@GetMapping("/product") public ProductDTO readProductByCode(@RequestParam String code) {
		return service.readProductByCode(code);
	}

	/* == UPDATE **/
	@PutMapping("/product/{id}") public ProductDTO updateProduct(@PathVariable long id, @Valid @RequestBody CreateProductDTO request) {
		return service.updateProduct(id, request);
	}

	/* == DELETE == */
	/* La cancellazione deve avvenire eventualmente dalla fattura/ricevuta */
	/* @DeleteMapping("/product/{id}") public void deleteProduct(@PathVariable long id) {
		service.deleteProduct(id);
	} */

}
