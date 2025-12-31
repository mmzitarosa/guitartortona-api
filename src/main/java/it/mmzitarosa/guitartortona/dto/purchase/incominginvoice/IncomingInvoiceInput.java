package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.dto.supplier.SupplierInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IncomingInvoiceInput(
		@NotEmpty(message = "Invoice number is mandatory")
		@Size(max = 50, message = "Invoice number must be at most 50 characters") String number,
		@NotNull(message = "Supplier is mandatory") SupplierInput supplier,
		@NotNull(message = "Date is mandatory") @JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		@NotNull(message = "Amount is mandatory")
		@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero") Double amount,
		@Size(max = 255, message = "Notes must be at most 255 characters") String notes
) {
}