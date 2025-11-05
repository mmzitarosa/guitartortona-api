package it.mmzitarosa.guitartortona.dto.ledger;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.utils.Constant;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static it.mmzitarosa.guitartortona.utils.Constant.*;

@Getter @Setter
public class LedgerEntryDTO {
	
	private Long id;				// Id del record
	@NotEmpty(message = "Date is mandatory")
	@Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Date must be in format dd/MM/yyyy")
	private String date;			// Data della fattura, già formatta: "22/09/1996"
	@Size(max = 50, message = "Invoice number must be at most 50 characters")
	private String invoiceNumber;			// Numero record "A123"
	@Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Invoice date must be in format dd/MM/yyyy")
	private String invoiceDate;			// Data della fattura, già formatta: "22/09/1996"
	@NotEmpty(message = "Description is mandatory")
	@Size(max = 255, message = "Description must be at most 255 characters")
	private String description;
	@Size(max = 100, message = "Reason must be at most 255 characters")
	private String reason;
	private PaymentMethod paymentMethod;
	private BankDTO bank;
	private PaymentType paymentType;
	@Size(max = 5, message = "Receipt number must be at most 5 characters")
	private String receiptNumber;
	private MovementType movementType;
	@DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
	private Double amount;
	@Size(max = 255, message = "Notes must be at most 255 characters")
	private String notes;			// Eventuali note

}
