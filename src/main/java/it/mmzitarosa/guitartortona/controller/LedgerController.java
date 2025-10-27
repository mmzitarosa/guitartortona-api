package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.service.LedgerService;
import it.mmzitarosa.guitartortona.utils.Constant;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
	@GetMapping("/ledger") public Page<LedgerEntryDTO> readLedger(@RequestParam(required = false) String from, @RequestParam(required = false) String to, @PageableDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable) {
		if (from == null || to == null) return service.readLedger(pageable);
		return service.readLedgerBetweenDates(from, to, "dd-MM-yyyy", pageable);
	}

	@GetMapping("/archive/ledger") public List<LedgerEntryDTO> readArchivedLedgerEntries() {
		return service.readLedger(Constant.Status.ARCHIVED);
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
