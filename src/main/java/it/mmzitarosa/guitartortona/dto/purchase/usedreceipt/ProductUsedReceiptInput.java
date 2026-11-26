package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Quantita' sempre 1; IVA facoltativa (default 0). Il codice produttore e il punto di riordino non si usano per l'usato. */
public record ProductUsedReceiptInput(
		@NotNull(message = "Product is mandatory") @Valid ProductInput product,
		@DecimalMin(value = "0.0", message = "VAT must be greater than or equal to zero") Double vat,
		@NotNull(message = "Purchase price is mandatory")
		@DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than zero") Double purchasePrice
) {
}
