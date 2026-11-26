package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * customer assente = ricevuta senza dati cliente.
 * updateCustomer: propaga i dati ricevuti anche all'anagrafica (solo se customer.id e' valorizzato).
 */
public record UsedReceiptInput(
		@NotNull(message = "Date is mandatory") @JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		@Size(max = 255, message = "Notes must be at most 255 characters") String notes,
		@Valid CustomerInput customer,
		boolean updateCustomer
) {
}
