package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.PageDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductEventDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import it.mmzitarosa.guitartortona.dto.product.ProductListDTO;
import it.mmzitarosa.guitartortona.dto.sale.CartSaleInput;
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
	public ResponseEntity<PageDTO<ProductListDTO>> getProducts(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String description,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Long brandId,
			@RequestParam(defaultValue = "false") boolean available,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size,
			@SortDefault(sort = "id", direction = Sort.Direction.DESC) Sort sort) {
		return ResponseEntity.ok(service.getProducts(search, description, categoryId, brandId, available, page, size, sort));
	}

	@GetMapping("/product/{id}") public ResponseEntity<ProductDetailDTO> getProductDetail(@PathVariable long id) {
		return ResponseEntity.ok(service.getProductDetail(id));
	}

	@GetMapping("/product/{id}/history") public ResponseEntity<List<ProductEventDTO>> getProductHistory(@PathVariable long id) {
		return ResponseEntity.ok(service.getProductHistory(id));
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

	@PostMapping("/sales")
	public ResponseEntity<Void> addCartSale(@Valid @RequestBody CartSaleInput request) {
		saleService.addCartSale(request);
		return ResponseEntity.noContent().build();
	}
}
