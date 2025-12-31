package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import it.mmzitarosa.guitartortona.dto.product.ProductListDTO;
import it.mmzitarosa.guitartortona.dto.sale.SaleInput;
import it.mmzitarosa.guitartortona.service.ProductService;
import it.mmzitarosa.guitartortona.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController @RequestMapping("/api/v1")
public class ProductController {

	private final ProductService service;
	private final SaleService saleService;

	/* == READ == */
	@GetMapping("/products")
	public ResponseEntity<List<ProductListDTO>> getAllProducts(@RequestParam(name = "search", required = false) String search, @SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort) {
		if (search != null) return ResponseEntity.ok(service.searchProducts(search, sort));
		return ResponseEntity.ok(service.getAllProducts(sort));
	}

	@GetMapping("/product/{id}") public ResponseEntity<ProductDetailDTO> getProductDetail(@PathVariable long id) {
		return ResponseEntity.ok(service.getProductDetail(id));
	}

	@GetMapping("/product") public ResponseEntity<ProductDetailDTO> getProductDetailByCode(@RequestParam String code) {
		return ResponseEntity.ok(service.getProductDetailByCode(code));
	}

	/* == UPDATE **/
	@PutMapping("/product/{id}") public ResponseEntity<ProductDetailDTO> updateProduct(@PathVariable long id, @Valid @RequestBody ProductInput request) {
		return ResponseEntity.ok(service.updateProduct(id, request));
	}

	/* == OTHERS == */
	@PostMapping("/product/{id}/sell")
	public ResponseEntity<ProductDetailDTO> addProductSale(@PathVariable long id, @Valid @RequestBody SaleInput request) {
		return ResponseEntity.ok(saleService.addProductSale(id, request));
	}
}
