package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.utils.Constant.MovementType;
import it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.MovementType.EXPENSE;
import static it.mmzitarosa.guitartortona.utils.Constant.MovementType.INCOME;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod.BANK;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod.CASH;

@Component public class LedgerEntityMapper {

	/* == CONSTANTS == */
	private final BankMapper bankMapper;

	/* == CONSTRUCTOR == */
	public LedgerEntityMapper(BankMapper bankMapper) {
		this.bankMapper = bankMapper;
	}

	/* == PUBLIC METHODS == */
	public LedgerEntryDTO toDto(LedgerEntryEntity entity) {
		LedgerEntryDTO dto = new LedgerEntryDTO();
		dto.setId(entity.getId());
		dto.setDate(entity.getDate());
		dto.setInvoiceNumber(entity.getInvoiceNumber());
		dto.setInvoiceDate(entity.getInvoiceDate());
		dto.setDescription(entity.getDescription());
		dto.setReason(entity.getReason());
		dto.setPaymentMethod(entity.getPaymentMethod());
		dto.setBank(entity.getBank() != null ? bankMapper.toDto(entity.getBank()) : null);
		dto.setPaymentType(entity.getPaymentType());
		dto.setReceiptNumber(entity.getReceiptNumber());
		dto.setMovementType(entity.getMovementType());
		dto.setAmount(entity.getAmount());
		dto.setNotes(entity.getNotes());
		return dto;
	}

	public List<LedgerEntryDTO> toDto(List<LedgerEntryEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

	public Page<LedgerEntryDTO> toDto(Page<LedgerEntryEntity> entities) {
		return entities.map(this::toDto);
	}

	public PrintableLedgerDTO toPrintableDto(List<LedgerEntryEntity> entities) {
		PrintableLedgerDTO dto = new PrintableLedgerDTO();

		List<LedgerEntryDTO> ledgerEntryDTOs = new ArrayList<>();

		double incomeCash = 0.0;
		double expenseCash = 0.0;
		double incomeBank = 0.0;
		double expenseBank = 0.0;

		for (LedgerEntryEntity entity : entities) {
			Double amount = entity.getAmount();
			if (amount != null) {
				PaymentMethod paymentMethod = entity.getPaymentMethod();
				MovementType movementType = entity.getMovementType();

				if (paymentMethod == CASH) {
					if (movementType == INCOME) incomeCash += amount;
					else if (movementType == EXPENSE) expenseCash += amount;
				} else if (paymentMethod == BANK) {
					if (movementType == INCOME) incomeBank += amount;
					else if (movementType == EXPENSE) expenseBank += amount;
				}
			}

			ledgerEntryDTOs.add(toDto(entity));
		}

		dto.setLedgerEntries(ledgerEntryDTOs);
		dto.setIncomeCashAmount(incomeCash);
		dto.setExpenseCashAmount(expenseCash);
		dto.setIncomeBankAmount(incomeBank);
		dto.setExpenseBankAmount(expenseBank);

		return dto;
	}

	public LedgerEntryEntity toEntity(CreateLedgerEntryDTO dto, BankEntity bank) {
		return toEntity(new LedgerEntryEntity(), dto, bank);
	}

	public LedgerEntryEntity toEntity(LedgerEntryEntity entity, CreateLedgerEntryDTO dto, BankEntity bank) {
		entity.setDate(dto.getDate());
		entity.setInvoiceNumber(dto.getInvoiceNumber());
		entity.setInvoiceDate(dto.getInvoiceDate());
		entity.setDescription(dto.getDescription());
		entity.setReason(dto.getReason());
		entity.setPaymentMethod(dto.getPaymentMethod());
		entity.setBank(bank);
		entity.setPaymentType(dto.getPaymentType());
		entity.setReceiptNumber(dto.getReceiptNumber());
		entity.setMovementType(dto.getMovementType());
		entity.setAmount(dto.getAmount());
		entity.setNotes(dto.getNotes());
		return entity;
	}

}
