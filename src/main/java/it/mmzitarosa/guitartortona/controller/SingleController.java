package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.dto.BrandDTO;
import it.mmzitarosa.guitartortona.dto.CategoryDTO;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.service.BankService;
import it.mmzitarosa.guitartortona.service.BrandService;
import it.mmzitarosa.guitartortona.service.CategoryService;
import it.mmzitarosa.guitartortona.service.SupplierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController @RequestMapping("/api/v1")
public class SingleController {

	/* == CONSTANTS == */
	private final BankService bankService;
	private final BrandService brandService;
	private final CategoryService categoryService;
	private final SupplierService supplierService;

	/* == CONSTRUCTOR == */
	public SingleController(BankService bankService, BrandService brandService, CategoryService categoryService, SupplierService supplierService) {
		this.bankService = bankService;
		this.brandService = brandService;
		this.categoryService = categoryService;
		this.supplierService = supplierService;
	}

	/* == READ BANKS == */
	@GetMapping("/banks") public List<BankDTO> readBanks() {
		return bankService.readBanks();
	}

	/* == READ BRANDS == */
	@GetMapping("/brands") public List<BrandDTO> readBrands() {
		return brandService.readBrands();
	}

	/* == READ CATEGORIES == */
	@GetMapping("/categories") public List<CategoryDTO> readCategories() {
		return categoryService.readCategories();
	}

	/* == READ SUPPLIERS == */
	@GetMapping("/suppliers") public List<SupplierDTO> readSuppliers() {
		return supplierService.readSuppliers();
	}

}
