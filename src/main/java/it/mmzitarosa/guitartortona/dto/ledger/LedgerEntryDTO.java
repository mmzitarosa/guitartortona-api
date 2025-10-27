package it.mmzitarosa.guitartortona.dto.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.dto.BankDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static it.mmzitarosa.guitartortona.utils.Constant.*;

@Getter @Setter public class LedgerEntryDTO {
	
	private Long id;						// Id del record
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;					// Data della fattura, già formatta: "22/09/1996"
	private String invoiceNumber;			// Numero record "A123"
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate invoiceDate;				// Data della fattura, già formatta: "22/09/1996"
	private String description;				// Descrizione
	private String reason;					// Causale
	private PaymentMethod paymentMethod;	// Banca/Cassa
	private BankDTO bank;					// Banca
	private PaymentType paymentType;		// Saldo/Acconto
	private String receiptNumber;			// Ultime 3 Cifre
	private MovementType movementType;		// Entrata/Uscita
	private Double amount;					// Importo in entrata/uscita da banca/cassa
	private String notes;					// Eventuali note

}
