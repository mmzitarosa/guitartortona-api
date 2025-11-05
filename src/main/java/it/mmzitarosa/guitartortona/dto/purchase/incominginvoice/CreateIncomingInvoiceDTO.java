package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter public class CreateIncomingInvoiceDTO {

	@NotEmpty(message = "Invoice number is mandatory")
	@Size(max = 50, message = "Invoice number must be at most 50 characters")
	private String number;		// Numero fattura "A123"
	private Long supplierId;	// Id del fornitore se già esistente, altrimenti null
	@Size(max = 150, message = "Supplier name must be at most 150 characters")
	private String supplierName;	// Da valorizzare per creare nuovo fornitore, alternativa al supplierId
	@NotNull(message = "Date is mandatory")
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;		// Data della fattura, già formatta: "22/09/1996"
	@NotNull(message = "Amount is mandatory")
	@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
	private Double amount;		// Importo totale delle fattura
	@Size(max = 255, message = "Notes must be at most 255 characters")
	private String notes;			// Eventuali note

}
