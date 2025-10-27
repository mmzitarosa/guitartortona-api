package it.mmzitarosa.guitartortona.dto.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class PrintableLedgerEntryDTO {

	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;
	private String invoiceNumber;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate invoiceDate;
	private String description;
	private String reason;
	private String bankName;
	private String paymentType;
	private String receiptNumber;
	private Double incomeCashAmount;
	private Double expenseCashAmount;
	private Double incomeBankAmount;
	private Double expenseBankAmount;

}
