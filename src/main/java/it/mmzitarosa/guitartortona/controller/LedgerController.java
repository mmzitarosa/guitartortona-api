package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@RestController @RequestMapping("/api/v1") public class LedgerController {

	private final LedgerService ledgerService;

	public LedgerController(LedgerService ledgerService) {
		this.ledgerService = ledgerService;
	}

	@GetMapping("/ledger") public Page<LedgerEntryDTO> readLedger(@RequestParam(required = false) String from, @RequestParam(required = false) String to, @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC, size = 15) Pageable pageable) {
		if (from == null || to == null) return ledgerService.readLedger(pageable);
		return ledgerService.readLedgerBetweenDates(from, to, "dd-MM-yyyy", pageable);
	}
	
	@GetMapping("/ledgerEntry/{id}") public LedgerEntryDTO readLedgerEntryById(@PathVariable long id) {
		return ledgerService.readLedgerEntryById(id);
	}
	
	@PostMapping("/ledgerEntry")
	public LedgerEntryDTO createLedgerEntry(@Valid @RequestBody CreateLedgerEntryDTO request) {
		return ledgerService.createLedgerEntry(request);
	}
	
	@PutMapping("/ledgerEntry/{id}") public LedgerEntryDTO updateLedgerEntryById(@PathVariable long id, @Valid @RequestBody CreateLedgerEntryDTO request) {
		return ledgerService.updateLedgerEntryById(id, request);
	}

	@DeleteMapping("/ledgerEntry/{id}") public void deleteLedgerEntryById(@PathVariable long id) {
		ledgerService.deleteLedgerEntryById(id);
	}

}
