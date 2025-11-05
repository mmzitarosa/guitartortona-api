package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter public class IncomingInvoiceProductsDTO extends IncomingInvoiceDTO {

	private List<PurchaseItemDTO> items;

}
