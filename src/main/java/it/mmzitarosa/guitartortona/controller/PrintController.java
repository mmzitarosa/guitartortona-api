package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.service.PrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller @RequestMapping("/api/v1") public class PrintController {

	@Autowired private PrintService service;
	
	@GetMapping("/ledger/print") public String printLedger(@RequestParam String from, @RequestParam String to, Model model, @SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("ledger", service.readLedger(from, to, "dd-MM-yyyy", sort));
		return "ledger";
	}
}
