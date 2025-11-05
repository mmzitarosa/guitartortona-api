package it.mmzitarosa.guitartortona.dto.purchase;

import it.mmzitarosa.guitartortona.dto.product.ProductDTO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class PurchaseItemDTO {

	private Long id;
	private ProductDTO product;		// Prodotto
	private Integer quantity;		// Quantità del prodotto
	private Double vat;				// IVA da applicare al costo
	private Double purchasePrice;	// Costo IVA esclusa

}