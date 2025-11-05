package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class AddIncomingInvoiceProductDTO {

	@NotNull(message = "Product is mandatory")
	private Long productId;		// Id del prodotto precedentemente aggiunto
	@DecimalMin(value = "0.0", inclusive = true, message = "VAT must be greater than or equal to zero")
	private Double vat;			// IVA da applicare al costo
	@DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than zero")
	private Double purchasePrice;
	@NotNull(message = "Quantity is mandatory")
	@Min(value = 1, message = "Quantity must be greater than or equal to zero")
	private Integer quantity;

}
