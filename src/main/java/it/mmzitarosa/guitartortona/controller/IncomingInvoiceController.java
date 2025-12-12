package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.*;
import it.mmzitarosa.guitartortona.service.IncomingInvoiceService;
import it.mmzitarosa.guitartortona.service.PurchaseItemService;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class IncomingInvoiceController {

	/* == CONSTANTS == */
	private final IncomingInvoiceService service;
	private final PurchaseItemService purchaseItemService;

	/* == CONSTRUCTOR == */
	public IncomingInvoiceController(IncomingInvoiceService incomingInvoiceService, PurchaseItemService purchaseItemService) {
		this.service = incomingInvoiceService;
		this.purchaseItemService = purchaseItemService;
	}

	/* == CREATE == */
	@PostMapping("/incomingInvoice") public IncomingInvoiceDTO createIncomingInvoice(@Valid @RequestBody CreateIncomingInvoiceDTO request) {
		return service.createIncomingInvoice(request);
	}

	/* == READ == */
	@GetMapping("/incomingInvoices") public List<IncomingInvoiceLightDTO> readIncomingInvoices(@SortDefault(sort = {"date", "supplier", "id"}, direction = Sort.Direction.DESC) Sort sort) {
		return service.readIncomingInvoices(false, sort);
	}

	@GetMapping("/archive/incomingInvoices")
	public List<IncomingInvoiceLightDTO> readArchivedIncomingInvoices(@SortDefault(sort = {"archived_date"}, direction = Sort.Direction.DESC) Sort sort) {
		return service.readIncomingInvoices(true, sort);
	}

	@GetMapping("/incomingInvoice/{id}")
	public IncomingInvoiceProductsDTO readIncomingInvoice(@PathVariable long id) {
		return service.readIncomingInvoice(id, Status.DRAFT, Status.COMPLETED);
	}

	/* == UPDATE **/
	@PutMapping("/incomingInvoice/{id}")
	public IncomingInvoiceDTO updateIncomingInvoice(@PathVariable long id, @Valid @RequestBody CreateIncomingInvoiceDTO request) {
		return service.updateIncomingInvoice(id, request);
	}

	@PatchMapping("/incomingInvoice/{id}/complete")
	public void completeIncomingInvoice(@PathVariable long id) {
		service.completeIncomingInvoice(id);
	}

	/* == DELETE == */
	@DeleteMapping("/incomingInvoice/{id}")
	public void deleteIncomingInvoice(@PathVariable long id) {
		service.deleteIncomingInvoice(id);
	}

	/* == OTHERS == */
	/* == (CREATE) ADD PRODUCT TO INCOMING INVOICE == */
	@PostMapping("/incomingInvoice/{id}/product")
	public PurchaseItemDTO addIncomingInvoiceProduct(@PathVariable long id, @Valid @RequestBody AddIncomingInvoiceProductDTO request) {
		return purchaseItemService.addIncomingInvoiceProduct(id, request);
	}

	/* == (UPDATE) UPDATE INCOMING INVOICE PRODUCT == */
	@PutMapping("/incomingInvoice/{invoiceId}/product/{id}")
	public PurchaseItemDTO updateIncomingInvoiceProduct(@PathVariable long id, @Valid @RequestBody UpdateIncomingInvoiceProductDTO request) {
		return purchaseItemService.updateIncomingInvoiceProduct(id, request);
	}

	/* == (UPDATE) UPDATE INCOMING INVOICE PRODUCT == */
	@DeleteMapping("/incomingInvoice/{invoiceId}/product/{id}")
	public void deleteIncomingInvoiceProduct(@PathVariable long id) {
		purchaseItemService.deletePurchaseItem(id);
	}


}
