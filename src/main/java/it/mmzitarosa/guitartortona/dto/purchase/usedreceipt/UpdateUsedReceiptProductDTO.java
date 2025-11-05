package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class UpdateUsedReceiptProductDTO {

	@NotNull(message = "Amount is mandatory")
	@DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than zero")
	private Double purchasePrice;

}
