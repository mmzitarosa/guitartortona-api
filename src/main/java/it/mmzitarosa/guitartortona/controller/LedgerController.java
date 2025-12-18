package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryLightDTO;
import it.mmzitarosa.guitartortona.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/v1")
public class LedgerController {

	/* == CONSTANTS == */
	private final LedgerService service;

	/* == CONSTRUCTOR == */
	public LedgerController(LedgerService service) {
		this.service = service;
	}

	/* == CREATE == */
	@PostMapping("/ledgerEntry") public LedgerEntryDTO createLedgerEntry(@Valid @RequestBody CreateLedgerEntryDTO request) {
		return service.createLedgerEntry(request);
	}

	/* == READ == */
	@GetMapping("/ledger") public List<LedgerEntryLightDTO> readLedger(@RequestParam int year, @SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return service.readLedger(year, false, sort);
	}

	@GetMapping("/archive/ledger") public List<LedgerEntryLightDTO> readArchivedLedgerEntries(@RequestParam int year, @SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return service.readLedger(year, true, sort);
	}

	@GetMapping("/ledgerEntry/{id}") public LedgerEntryDTO readLedgerEntry(@PathVariable long id) {
		return service.readLedgerEntry(id);
	}

	/* == UPDATE == */
	@PutMapping("/ledgerEntry/{id}") public LedgerEntryDTO updateLedgerEntry(@PathVariable long id, @Valid @RequestBody CreateLedgerEntryDTO request) {
		return service.updateLedgerEntry(id, request);
	}

	/* == DELETE == */
	@DeleteMapping("/ledgerEntry/{id}") public void deleteLedgerEntry(@PathVariable long id) {
		service.deleteLedgerEntry(id);
	}

}
