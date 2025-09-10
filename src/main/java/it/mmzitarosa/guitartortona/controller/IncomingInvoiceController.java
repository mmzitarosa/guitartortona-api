package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.incominginvoice.IncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.service.IncomingInvoiceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1") public class IncomingInvoiceController {

	private final IncomingInvoiceService incomingInvoiceService;

	public IncomingInvoiceController(IncomingInvoiceService incomingInvoiceService) {
		this.incomingInvoiceService = incomingInvoiceService;
	}

	@GetMapping("/incomingInvoice/{id}")
	public IncomingInvoiceDTO readIncomingInvoiceById(@PathVariable long id) {
		return incomingInvoiceService.readIncomingInvoiceById(id);
	}
	
	@PostMapping("/incomingInvoice")
	public IncomingInvoiceDTO createIncomingInvoice(@Valid @RequestBody CreateIncomingInvoiceDTO request) throws InterruptedException {
		return incomingInvoiceService.createIncomingInvoice(request);
	}
	
}
