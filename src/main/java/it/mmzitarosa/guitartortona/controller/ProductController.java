package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.product.CreateProductDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductLightDTO;
import it.mmzitarosa.guitartortona.service.ProductService;
import it.mmzitarosa.guitartortona.service.ProductStockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/v1")
public class ProductController {

	/* == CONSTANTS == */
	private final ProductService service;
	private final ProductStockService stockService;

	/* == CONSTRUCTOR == */
	public ProductController(ProductService service, ProductStockService stockService) {
		this.service = service;
		this.stockService = stockService;
	}

	/* == CREATE == */
	@PostMapping("/product") public ProductDTO createProduct(@Valid @RequestBody CreateProductDTO request) {
		return service.createProduct(request);
	}

	/* == READ == */
	@GetMapping("/products") public List<ProductLightDTO> readProducts(@RequestParam(name = "search", required = false) String search, @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort) {
		if (search != null) return stockService.searchProducts(search, sort);
		return stockService.readProducts(false, sort);
	}

	@GetMapping("/archive/products") public List<ProductLightDTO> readArchivedProducts(@SortDefault(sort = {"archived_date"}, direction = Sort.Direction.DESC) Sort sort) {
		return stockService.readProducts(true, sort);
	}

	@GetMapping("/product/{id}") public ProductDTO readProduct(@PathVariable long id) {
		return service.readProduct(id);
	}

	@GetMapping("/product") public ProductDTO readProductByCode(@RequestParam String code) {
		return service.searchProductByCode(code);
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
