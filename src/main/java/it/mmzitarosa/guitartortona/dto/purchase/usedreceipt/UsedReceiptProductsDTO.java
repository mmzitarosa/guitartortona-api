package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter public class UsedReceiptProductsDTO extends UsedReceiptDTO {

	private List<PurchaseItemDTO> items;

}
