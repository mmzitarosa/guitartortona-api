package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter public class IncomingInvoiceLightDTO {
	
	private Long id;				// Id della fattura
	private String number;			// Numero fattura "A123"
	private Long supplierId;		// Fornitore
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;			// Data della fattura, già formatta: "22/09/1996"
	private Double amount;			// Importo totale della fattura

	private Status status;
	private Long daysLeft;			// Se archiviato, restituisce i giorni restanti

}
