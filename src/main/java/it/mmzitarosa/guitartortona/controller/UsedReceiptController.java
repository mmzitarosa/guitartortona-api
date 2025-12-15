package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.*;
import it.mmzitarosa.guitartortona.service.PurchaseItemService;
import it.mmzitarosa.guitartortona.service.UsedReceiptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController @RequestMapping("/api/v1")
public class UsedReceiptController {

	/* == CONSTANTS == */
	private final UsedReceiptService service;
	private final PurchaseItemService purchaseItemService;

	/* == CONSTRUCTOR == */
	public UsedReceiptController(UsedReceiptService service, PurchaseItemService purchaseItemService) {
		this.service = service;
		this.purchaseItemService = purchaseItemService;
	}

	/* == CREATE == */
	@PostMapping("/usedReceipt") public UsedReceiptDTO createUsedReceipt(@Valid @RequestBody CreateUsedReceiptDTO request) {
		return service.createUsedReceipt(request);
	}

	/* == READ == */
	@GetMapping("/usedReceipts") public List<UsedReceiptLightDTO> readUsedReceipts(@SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return service.readUsedReceipts(false, sort);
	}

	@GetMapping("/archive/usedReceipts") public List<UsedReceiptLightDTO> readArchivedUsedReceipts(@SortDefault(sort = {"date", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return service.readUsedReceipts(true, sort);
	}

	@GetMapping("/usedReceipt/{id}") public UsedReceiptProductsDTO readUsedReceipt(@PathVariable long id) {
		return service.readUsedReceipt(id);
	}

	/* == UPDATE **/
	@PutMapping("/usedReceipt/{id}") public UsedReceiptDTO updateUsedReceipt(@PathVariable long id, @Valid @RequestBody CreateUsedReceiptDTO request) {
		return service.updateUsedReceipt(id, request);
	}

	@PatchMapping("/usedReceipt/{id}/complete") public void completeUsedReceipt(@PathVariable long id) {
		service.completeUsedReceipt(id);
	}

	/* == DELETE == */
	@DeleteMapping("/usedReceipt/{id}") public void deleteUsedReceipt(@PathVariable long id) {
		service.deleteUsedReceipt(id);
	}

	/* == OTHERS == */
	/* == (CREATE) CREATE AND ADD PRODUCT TO USED RECEIPT == */
	@PostMapping("/usedReceipt/{id}/product")
	public PurchaseItemDTO addProduct(@PathVariable long id, @Valid @RequestBody AddUsedReceiptProductDTO request) {
		return purchaseItemService.addUsedReceiptProduct(id, request);
	}

	/* == (UPDATE) UPDATE USED RECEIPT PRODUCT == */
	@PutMapping("/usedReceipt/product/{id}")
	public PurchaseItemDTO updateUsedReceiptProduct(@PathVariable long id, @Valid @RequestBody UpdateUsedReceiptProductDTO request) {
		return purchaseItemService.updateUsedReceiptProduct(id, request);
	}
}