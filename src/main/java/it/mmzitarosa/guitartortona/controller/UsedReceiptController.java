package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.service.PurchaseItemService;
import it.mmzitarosa.guitartortona.service.UsedReceiptService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

}