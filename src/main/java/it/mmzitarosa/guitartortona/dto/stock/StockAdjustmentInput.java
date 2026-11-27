package it.mmzitarosa.guitartortona.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentInput(
		@NotNull(message = "Counted stock is mandatory") @Min(value = 0, message = "Counted stock must be greater than or equal to zero") Integer countedStock,
		@NotBlank(message = "Reason is mandatory") @Size(max = 255, message = "Reason must be at most 255 characters") String reason
) { }
