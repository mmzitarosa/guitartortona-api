package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter public class IncomingInvoiceDTO {
	
	private Long id;				// Id della fattura
	private String number;			// Numero fattura "A123"
	private SupplierDTO supplier;	// Fornitore, oggetto completo 
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;			// Data della fattura, già formatta: "22/09/1996"
	private Double amount;			// Importo totale delle fattura
	private String notes;			// Eventuali note

	private Status status;
	private Long daysLeft;			// Se archiviato, restituisce i giorni restanti

}
