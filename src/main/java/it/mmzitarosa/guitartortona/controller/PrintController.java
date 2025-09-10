package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.service.LedgerService;
import it.mmzitarosa.guitartortona.service.PrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.NumberFormat;
import java.util.Locale;

@Controller @RequestMapping("/api/v1") public class PrintController {

	@Autowired private PrintService printService;
	
	@GetMapping("/ledger/print") public String printLedger(@RequestParam String from, @RequestParam String to, Model model) {
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("ledger", printService.readLedgerBetweenDates(from, to, "dd-MM-yyyy"));
		return "ledger";
	}
}
