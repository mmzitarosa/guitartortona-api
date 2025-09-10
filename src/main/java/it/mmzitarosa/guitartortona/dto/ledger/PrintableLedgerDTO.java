package it.mmzitarosa.guitartortona.dto.ledger;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PrintableLedgerDTO {
	
	private List<LedgerEntryDTO> ledgerEntries;
	private Double incomeCashAmount;
	private Double expenseCashAmount;
	private Double incomeBankAmount;
	private Double expenseBankAmount;

}
