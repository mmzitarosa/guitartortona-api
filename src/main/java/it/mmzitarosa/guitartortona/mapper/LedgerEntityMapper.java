package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerEntryDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.utils.Constant;
import it.mmzitarosa.guitartortona.utils.Constant.MovementType;
import it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.MovementType.EXPENSE;
import static it.mmzitarosa.guitartortona.utils.Constant.MovementType.INCOME;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod.BANK;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod.CASH;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentType.DEPOSIT;

@Component
public class LedgerEntityMapper {

	@Autowired
	private BankMapper bankMapper;
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	public LedgerEntryDTO toDto(LedgerEntryEntity entity) {
		LedgerEntryDTO dto = new LedgerEntryDTO();
		dto.setId(entity.getId());
		dto.setDate(sdf.format(entity.getDate()));
		dto.setInvoiceNumber(entity.getInvoiceNumber());
		dto.setInvoiceDate(entity.getInvoiceDate() != null ? sdf.format(entity.getInvoiceDate()) : null);
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

	public PrintableLedgerEntryDTO toPrintableDto(LedgerEntryEntity entity) {
		PrintableLedgerEntryDTO dto = new PrintableLedgerEntryDTO();
		dto.setDate(sdf.format(entity.getDate()));
		dto.setInvoiceNumber(entity.getInvoiceNumber());
		dto.setInvoiceDate(entity.getInvoiceDate() != null ? sdf.format(entity.getInvoiceDate()) : null);
		dto.setDescription(entity.getDescription());
		dto.setReason(entity.getReason());
		dto.setBankName(entity.getBank() != null ? bankMapper.toDto(entity.getBank()).getName() : null);
		dto.setPaymentType(entity.getPaymentType() != null ? entity.getPaymentType() == DEPOSIT ? "Acconto" : "Saldo" : null);
		dto.setReceiptNumber(entity.getReceiptNumber());
		if (entity.getAmount() != null) {
//			NumberFormat n = NumberFormat.getCurrencyInstance(Locale.ITALY);
//			String amount = n.format(entity.getAmount());
			Double amount = entity.getAmount();

			boolean isCash = entity.getPaymentMethod() == CASH;
			boolean isBank = entity.getPaymentMethod() == BANK;
			boolean isIncome = entity.getMovementType() == INCOME;
			boolean isExpense = entity.getMovementType() == EXPENSE;

			if (isCash) {
				if (isIncome) dto.setIncomeCashAmount(amount);
				else if (isExpense) dto.setExpenseCashAmount(amount);
			} else if (isBank) {
				if (isIncome) dto.setIncomeBankAmount(amount);
				else if (isExpense) dto.setExpenseBankAmount(amount);
			}

		}
		return dto;
	}

	public Page<LedgerEntryDTO> toDto(Page<LedgerEntryEntity> entities) {
		return entities.map(this::toDto);
	}

	public List<LedgerEntryDTO> toDto(List<LedgerEntryEntity> entities) {
		return entities.stream().map(this::toDto).toList();
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

	public LedgerEntryEntity toEntity(CreateLedgerEntryDTO dto, BankEntity bankEntity) {
		try {
			LedgerEntryEntity entity = new LedgerEntryEntity();
			entity.setDate(sdf.parse(dto.getDate()));
			entity.setInvoiceNumber(dto.getInvoiceNumber());
			entity.setInvoiceDate(dto.getInvoiceDate() != null ? sdf.parse(dto.getInvoiceDate()) : null);
			entity.setDescription(dto.getDescription());
			entity.setReason(dto.getReason());
			entity.setPaymentMethod(dto.getPaymentMethod());
			entity.setBank(bankEntity);
			entity.setPaymentType(dto.getPaymentType());
			entity.setReceiptNumber(dto.getReceiptNumber());
			entity.setMovementType(dto.getMovementType());
			entity.setAmount(dto.getAmount());
			entity.setNotes(dto.getNotes());
			return entity;
		} catch (ParseException e) {
			throw new RuntimeException("Invalid date format", e);
		}
	}

	public LedgerEntryEntity toEntity(LedgerEntryDTO dto, BankEntity bankEntity) {
		try {
			LedgerEntryEntity entity = new LedgerEntryEntity();
			entity.setId(dto.getId());
			entity.setDate(sdf.parse(dto.getDate()));
			entity.setInvoiceNumber(dto.getInvoiceNumber());
			entity.setInvoiceDate(sdf.parse(dto.getInvoiceDate()));
			entity.setDescription(dto.getDescription());
			entity.setReason(dto.getReason());
			entity.setPaymentMethod(dto.getPaymentMethod());
			entity.setBank(bankEntity);
			entity.setPaymentType(dto.getPaymentType());
			entity.setReceiptNumber(dto.getReceiptNumber());
			entity.setMovementType(dto.getMovementType());
			entity.setAmount(dto.getAmount());
			entity.setNotes(dto.getNotes());
			return entity;
		} catch (ParseException e) {
			throw new RuntimeException("Invalid date format", e);
		}
	}

}
