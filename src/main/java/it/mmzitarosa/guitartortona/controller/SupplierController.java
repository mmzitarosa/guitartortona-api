package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.service.SupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/v1") public class SupplierController {

	private final SupplierService supplierService;

	public SupplierController(SupplierService supplierService) {
		this.supplierService = supplierService;
	}

	@GetMapping("/suppliers")
	public List<SupplierDTO> readSuppliers() {
		return supplierService.readSuppliers();
	}
	
}
