package it.mmzitarosa.guitartortona.dto.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static it.mmzitarosa.guitartortona.utils.Constant.*;

@Getter @Setter public class LedgerEntryLightDTO {
	
	private Long id;						// Id del record
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;					// Data della fattura, già formatta: "22/09/1996"
	private String invoiceNumber;			// Numero record "A123"
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate invoiceDate;				// Data della fattura, già formatta: "22/09/1996"
	private String description;				// Descrizione
	private String reason;					// Causale
	private PaymentMethod paymentMethod;	// Banca/Cassa
	private Long bankId;					// Banca
	private PaymentType paymentType;		// Saldo/Acconto
	private String receiptNumber;			// Ultime 3 Cifre
	private MovementType movementType;		// Entrata/Uscita
	private Double amount;					// Importo in entrata/uscita da banca/cassa

	public static LedgerEntryLightDTO of(LedgerEntryEntity entity) {
		LedgerEntryLightDTO dto = new LedgerEntryLightDTO();
		dto.setId(entity.getId());
		dto.setDate(entity.getDate());
		dto.setInvoiceNumber(entity.getInvoiceNumber());
		dto.setInvoiceDate(entity.getInvoiceDate());
		dto.setDescription(entity.getDescription());
		dto.setReason(entity.getReason());
		dto.setPaymentMethod(entity.getPaymentMethod());
		dto.setBankId(entity.getBank() != null ? entity.getBank().getId() : null);
		dto.setPaymentType(entity.getPaymentType());
		dto.setReceiptNumber(entity.getReceiptNumber());
		dto.setMovementType(entity.getMovementType());
		dto.setAmount(entity.getAmount());
		return dto;
	}


}
