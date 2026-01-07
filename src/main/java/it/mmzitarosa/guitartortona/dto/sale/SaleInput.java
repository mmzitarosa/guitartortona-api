package it.mmzitarosa.guitartortona.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SaleInput(
		Long id,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		@NotNull(message = "Quantity is mandatory") @Min(value = 1, message = "Quantity must be greater than or equal to zero") Integer quantity,
		@NotNull(message = "VAT is mandatory") @DecimalMin(value = "0.0", message = "VAT must be greater than or equal to zero") Double vat,
		@Size(max = 5, message = "Receipt number must be at most 5 characters") String receiptNumber,
		@NotNull(message = "Sale price is mandatory") @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than zero") Double salePrice,
		String notes
) { }
