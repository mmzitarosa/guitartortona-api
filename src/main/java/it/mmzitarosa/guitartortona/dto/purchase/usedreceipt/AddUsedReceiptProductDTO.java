package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import it.mmzitarosa.guitartortona.dto.product.CreateProductDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class AddUsedReceiptProductDTO {

	@NotNull(message = "Product is mandatory")
	private CreateProductDTO product;		// Prodotto da aggiungere
	@NotNull(message = "Amount is mandatory")
	@DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than zero")
	private Double purchasePrice;

}
