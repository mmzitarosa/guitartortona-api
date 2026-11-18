package it.mmzitarosa.guitartortona.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CartSaleInput(
		@NotNull(message = "Date is mandatory") @JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		@Size(max = 5, message = "Receipt number must be at most 5 characters") String receiptNumber,
		@NotEmpty(message = "Items are mandatory") List<@Valid Item> items
) {

	public record Item(
			@NotNull(message = "Product is mandatory") Long productId,
			@NotNull(message = "Quantity is mandatory") @Min(value = 1, message = "Quantity must be greater than or equal to 1") Integer quantity,
			@NotNull(message = "VAT is mandatory") @DecimalMin(value = "0.0", message = "VAT must be greater than or equal to zero") Double vat,
			@NotNull(message = "Sale price is mandatory") @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than zero") Double salePrice,
			String notes
	) { }
}
