package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.service.PrintService;
import it.mmzitarosa.guitartortona.service.UsedReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller @RequestMapping("/api/v1") public class PrintController {

	@Autowired private PrintService service;
	@Autowired private UsedReceiptService usedReceiptService;
	
	@GetMapping("/ledger/print") public String printLedger(@RequestParam String from, @RequestParam String to, Model model, @SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("ledger", service.readLedger(from, to, "dd-MM-yyyy", sort));
		return "ledger";
	}

	@GetMapping("/product/print") public String printLabel(@RequestParam String code, @RequestParam(required = false, defaultValue = "1") String quantity, Model model) {
		model.addAttribute("quantity", quantity);
		model.addAttribute("product", service.getProduct(code));
		return "label";
	}

	@GetMapping("/usedReceipt/{id}/print") public String printUsedReceipt(@PathVariable Long id, @RequestParam(required = false, defaultValue = "receipt") String mode, Model model) {
		model.addAttribute("receipt", usedReceiptService.getUsedReceiptPrint(id));
		if("labels".equals(mode)) {
			return "labels";
		}
		return "usedReceipt";
	}


}
