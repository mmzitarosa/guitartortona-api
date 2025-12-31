package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.purchase.ProductPurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceInput;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceListDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.ProductIncomingInvoiceInput;
import it.mmzitarosa.guitartortona.service.IncomingInvoiceService;
import it.mmzitarosa.guitartortona.service.PurchaseItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController @RequestMapping("/api/v1")
public class IncomingInvoiceController {

	private final IncomingInvoiceService service;
	private final PurchaseItemService purchaseItemService;

	/* == CREATE == */
	@PostMapping("/incomingInvoice")
	public ResponseEntity<IncomingInvoiceDetailDTO> createIncomingInvoice(@Valid @RequestBody IncomingInvoiceInput request) {
		return ResponseEntity.ok(service.createIncomingInvoice(request));
	}

	/* == READ == */
	@GetMapping("/incomingInvoices")
	public ResponseEntity<List<IncomingInvoiceListDTO>> getAllIncomingInvoices(@SortDefault(sort = {"date", "supplier", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return ResponseEntity.ok(service.getAllIncomingInvoices(false, sort));
	}

	@GetMapping("/archive/incomingInvoices")
	public ResponseEntity<List<IncomingInvoiceListDTO>> getArchivedIncomingInvoices(@SortDefault(sort = {"archivedDate"}, direction = Sort.Direction.DESC) Sort sort) {
		return ResponseEntity.ok(service.getAllIncomingInvoices(true, sort));
	}

	@GetMapping("/incomingInvoice/{id}")
	public ResponseEntity<IncomingInvoiceDetailDTO> getIncomingInvoiceDetail(@PathVariable long id) {
		return ResponseEntity.ok(service.getIncomingInvoiceDetail(id));
	}

	/* == UPDATE **/
	@PutMapping("/incomingInvoice/{id}")
	public ResponseEntity<IncomingInvoiceDetailDTO> updateIncomingInvoice(@PathVariable long id, @Valid @RequestBody IncomingInvoiceInput request) {
		return ResponseEntity.ok(service.updateIncomingInvoice(id, request));
	}

	@PatchMapping("/incomingInvoice/{id}/complete")
	public ResponseEntity<IncomingInvoiceDetailDTO> completeIncocompleteIncomingInvoicemingInvoice(@PathVariable long id) {
		return ResponseEntity.ok(service.completeIncomingInvoice(id));
	}

	@PatchMapping("/incomingInvoice/{id}/restore")
	public ResponseEntity<IncomingInvoiceDetailDTO> restoreIncomingInvoice(@PathVariable long id) {
		return ResponseEntity.ok(service.restoreIncomingInvoice(id));
	}

	/* == DELETE == */
	@DeleteMapping("/incomingInvoice/{id}")
	public ResponseEntity<Void> deleteIncomingInvoice(@PathVariable long id) {
		service.deleteIncomingInvoice(id);
		return ResponseEntity.noContent().build();
	}

	/* == OTHERS == */
	/* == (CREATE) ADD PRODUCT TO INCOMING INVOICE == */
	@PostMapping("/incomingInvoice/{id}/product")
	public ResponseEntity<ProductPurchaseItemDTO> addProductToIncomingInvoice(@PathVariable long id, @Valid @RequestBody ProductIncomingInvoiceInput request) {
		return ResponseEntity.ok(purchaseItemService.addProductToIncomingInvoice(id, request));
	}

	/* == (UPDATE) UPDATE INCOMING INVOICE PRODUCT == */
	@PutMapping("/incomingInvoice/{id}/product/{purchaseItemId}")
	public ResponseEntity<ProductPurchaseItemDTO> updateProductInIncomingInvoice(@PathVariable long id, @PathVariable long purchaseItemId, @Valid @RequestBody ProductIncomingInvoiceInput request) {
		return ResponseEntity.ok(purchaseItemService.updateProductInIncomingInvoice(id, purchaseItemId, request));
	}

	/* == (DELETE) DELETE INCOMING INVOICE PRODUCT == */
	@DeleteMapping("/incomingInvoice/{id}/product/{purchaseItemId}")
	public ResponseEntity<Void> deleteProductFromIncomingInvoice(@PathVariable long id, @PathVariable long purchaseItemId) {
		purchaseItemService.deleteProductFromIncomingInvoice(id, purchaseItemId);
		return ResponseEntity.noContent().build();
	}

}
