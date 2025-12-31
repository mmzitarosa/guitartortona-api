package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import it.mmzitarosa.guitartortona.dto.product.ProductInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductIncomingInvoiceInput(
		@NotNull(message = "Product is mandatory") ProductInput product,
		@DecimalMin(value = "0.0", message = "VAT must be greater than or equal to zero") Double vat,
		@DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than zero") Double purchasePrice,
		@NotNull(message = "Quantity is mandatory")
		@Min(value = 1, message = "Quantity must be greater than or equal to zero") Integer quantity
) {}

