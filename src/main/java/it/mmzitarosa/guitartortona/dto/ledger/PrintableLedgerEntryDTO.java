package it.mmzitarosa.guitartortona.dto.ledger;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrintableLedgerEntryDTO {
	
	private String date;
	private String invoiceNumber;
	private String invoiceDate;
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
