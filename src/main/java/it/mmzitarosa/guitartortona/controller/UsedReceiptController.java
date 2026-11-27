package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.ProductUsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptListDTO;
import it.mmzitarosa.guitartortona.service.PurchaseItemService;
import it.mmzitarosa.guitartortona.service.UsedReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController @RequestMapping("/api/v1")
public class UsedReceiptController {

	private final UsedReceiptService service;
	private final PurchaseItemService purchaseItemService;

	/* == CREATE == */
	@PostMapping("/usedReceipt")
	public ResponseEntity<UsedReceiptDetailDTO> createUsedReceipt(@Valid @RequestBody UsedReceiptInput request) {
		return ResponseEntity.ok(service.createUsedReceipt(request));
	}

	/* == READ == */
	@GetMapping("/usedReceipts")
	public ResponseEntity<List<UsedReceiptListDTO>> getAllUsedReceipts(@SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return ResponseEntity.ok(service.getAllUsedReceipts(false, sort));
	}

	@GetMapping("/archive/usedReceipts")
	public ResponseEntity<List<UsedReceiptListDTO>> getArchivedUsedReceipts(@SortDefault(sort = {"archivedDate"}, direction = Sort.Direction.DESC) Sort sort) {
		return ResponseEntity.ok(service.getAllUsedReceipts(true, sort));
	}

	@GetMapping("/usedReceipt")
	public ResponseEntity<UsedReceiptDetailDTO> getUsedReceiptDetailByCode(@RequestParam String code) {
		return ResponseEntity.ok(service.getUsedReceiptDetailByCode(code));
	}

	@GetMapping("/usedReceipt/{id}")
	public ResponseEntity<UsedReceiptDetailDTO> getUsedReceiptDetail(@PathVariable long id) {
		return ResponseEntity.ok(service.getUsedReceiptDetail(id));
	}

	/* == UPDATE == */
	@PutMapping("/usedReceipt/{id}")
	public ResponseEntity<UsedReceiptDetailDTO> updateUsedReceipt(@PathVariable long id, @Valid @RequestBody UsedReceiptInput request) {
		return ResponseEntity.ok(service.updateUsedReceipt(id, request));
	}

	@PatchMapping("/usedReceipt/{id}/confirm")
	public ResponseEntity<UsedReceiptDetailDTO> confirmUsedReceipt(@PathVariable long id) {
		return ResponseEntity.ok(service.confirmUsedReceipt(id));
	}

	@PatchMapping("/usedReceipt/{id}/register")
	public ResponseEntity<UsedReceiptDetailDTO> registerUsedReceipt(@PathVariable long id) {
		return ResponseEntity.ok(service.registerUsedReceipt(id));
	}

	/* Scelta dell'admin sul brogliaccio dopo una modifica, un'archiviazione o un'eliminazione della ricevuta */
	@PatchMapping("/usedReceipt/{id}/ledger/align")
	public ResponseEntity<UsedReceiptDetailDTO> alignLedger(@PathVariable long id) {
		return ResponseEntity.ok(service.alignLedger(id));
	}

	@PatchMapping("/usedReceipt/{id}/ledger/ignore")
	public ResponseEntity<UsedReceiptDetailDTO> ignoreLedger(@PathVariable long id) {
		return ResponseEntity.ok(service.ignoreLedger(id));
	}

	@PatchMapping("/usedReceipt/{id}/restore")
	public ResponseEntity<UsedReceiptDetailDTO> restoreUsedReceipt(@PathVariable long id) {
		return ResponseEntity.ok(service.restoreUsedReceipt(id));
	}

	@PatchMapping("/usedReceipt/{id}/archive")
	public ResponseEntity<Void> archiveUsedReceipt(@PathVariable long id) {
		service.archiveUsedReceipt(id);
		return ResponseEntity.noContent().build();
	}

	/* == DELETE (fisica, solo admin e solo se già archiviata) == */
	@DeleteMapping("/usedReceipt/{id}")
	public ResponseEntity<Void> deleteUsedReceipt(@PathVariable long id, @RequestParam(required = false) UsedReceiptService.LedgerChoice ledger) {
		service.deleteUsedReceipt(id, ledger);
		return ResponseEntity.noContent().build();
	}

	/* == PRODUCTS == */
	@PostMapping("/usedReceipt/{id}/product")
	public ResponseEntity<UsedReceiptItemDTO> addProductToUsedReceipt(@PathVariable long id, @Valid @RequestBody ProductUsedReceiptInput request) {
		return ResponseEntity.ok(purchaseItemService.addProductToUsedReceipt(id, request));
	}

	@PutMapping("/usedReceipt/{id}/product/{purchaseItemId}")
	public ResponseEntity<UsedReceiptItemDTO> updateProductInUsedReceipt(@PathVariable long id, @PathVariable long purchaseItemId, @Valid @RequestBody ProductUsedReceiptInput request) {
		return ResponseEntity.ok(purchaseItemService.updateProductInUsedReceipt(id, purchaseItemId, request));
	}

	@DeleteMapping("/usedReceipt/{id}/product/{purchaseItemId}")
	public ResponseEntity<Void> deleteProductFromUsedReceipt(@PathVariable long id, @PathVariable long purchaseItemId) {
		purchaseItemService.deleteProductFromUsedReceipt(id, purchaseItemId);
		return ResponseEntity.noContent().build();
	}

}
