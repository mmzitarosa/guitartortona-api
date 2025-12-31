package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.bank.BankDTO;
import it.mmzitarosa.guitartortona.dto.brand.BrandDTO;
import it.mmzitarosa.guitartortona.dto.category.CategoryDTO;
import it.mmzitarosa.guitartortona.dto.supplier.SupplierDTO;
import it.mmzitarosa.guitartortona.service.BankService;
import it.mmzitarosa.guitartortona.service.BrandService;
import it.mmzitarosa.guitartortona.service.CategoryService;
import it.mmzitarosa.guitartortona.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController @RequestMapping("/api/v1")
public class SingleController {

	/* == CONSTANTS == */
	private final BankService bankService;
	private final BrandService brandService;
	private final CategoryService categoryService;
	private final SupplierService supplierService;

	/* == READ BANKS == */
	@GetMapping("/banks") public ResponseEntity<List<BankDTO>> getAllBanks() {
		return ResponseEntity.ok(bankService.getAllBanks());
	}

	/* == READ BRANDS == */
	@GetMapping("/brands") public ResponseEntity<List<BrandDTO>> getAllBrands() {
		return ResponseEntity.ok(brandService.getAllBrands());
	}

	/* == READ CATEGORIES == */
	@GetMapping("/categories") public ResponseEntity<List<CategoryDTO>> getAllCategories() {
		return ResponseEntity.ok(categoryService.getAllCategories());
	}

	/* == READ SUPPLIERS == */
	@GetMapping("/suppliers") public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
		return ResponseEntity.ok(supplierService.getAllSuppliers());
	}

}
