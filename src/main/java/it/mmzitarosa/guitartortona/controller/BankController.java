package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.service.BankService;
import it.mmzitarosa.guitartortona.service.SupplierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController @RequestMapping("/api/v1") public class BankController {

	private final BankService bankService;

	public BankController(BankService bankService) {
		this.bankService = bankService;
	}

	@GetMapping("/banks") public List<BankDTO> readBanks() {
		return bankService.readBanks();
	}
	
}
