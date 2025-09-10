package it.mmzitarosa.guitartortona.dto.incominginvoice;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class IncomingInvoiceDTO {
	
	private Long id;				// Id della fattura
	@NotEmpty(message = "Invoice number is mandatory")
	@Size(max = 50, message = "Invoice number must be at most 50 characters")
	private String number;			// Numero fattura "A123"
	private SupplierDTO supplier;	// Fornitore, oggetto completo 
	@NotEmpty(message = "Date is mandatory")
	@Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Date must be in format dd/MM/yyyy")
	private String date;			// Data della fattura, già formatta: "22/09/1996"
	@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
	private Double amount;			// Importo totale delle fattura
	@Size(max = 255, message = "Notes must be at most 255 characters")
	private String notes;			// Eventuali note

	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
}
