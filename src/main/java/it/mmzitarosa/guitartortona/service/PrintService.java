package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductPrintDTO;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import it.mmzitarosa.guitartortona.repository.ProductRepository;
import it.mmzitarosa.guitartortona.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.MovementType.EXPENSE;
import static it.mmzitarosa.guitartortona.utils.Constant.MovementType.INCOME;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod.BANK;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod.CASH;

@RequiredArgsConstructor
@Service @Transactional(readOnly = true)
public class PrintService {

	private final LedgerRepository ledgerRepository;
	private final ProductRepository productRepository;

	public PrintableLedgerDTO readLedger(String from, String to, String datePattern, Sort sort) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);

		List<LedgerEntryEntity> entities = ledgerRepository.findAllByArchivedFalseAndDateBetween(LocalDate.parse(from, formatter), LocalDate.parse(to, formatter), sort);

		PrintableLedgerDTO dto = new PrintableLedgerDTO();

		List<LedgerEntryDTO> ledgerEntryDTOs = new ArrayList<>();

		double incomeCash = 0.0;
		double expenseCash = 0.0;
		double incomeBank = 0.0;
		double expenseBank = 0.0;

		for (LedgerEntryEntity entity : entities) {
			Double amount = entity.getAmount();
			if (amount != null) {
				Constant.PaymentMethod paymentMethod = entity.getPaymentMethod();
				Constant.MovementType movementType = entity.getMovementType();

				if (paymentMethod == CASH) {
					if (movementType == INCOME) incomeCash += amount;
					else if (movementType == EXPENSE) expenseCash += amount;
				} else if (paymentMethod == BANK) {
					if (movementType == INCOME) incomeBank += amount;
					else if (movementType == EXPENSE) expenseBank += amount;
				}
			}

			ledgerEntryDTOs.add(LedgerEntryDTO.of(entity));
		}

		dto.setLedgerEntries(ledgerEntryDTOs);
		dto.setIncomeCashAmount(incomeCash);
		dto.setExpenseCashAmount(expenseCash);
		dto.setIncomeBankAmount(incomeBank);
		dto.setExpenseBankAmount(expenseBank);

		return dto;
	}

	@Transactional(readOnly = true) public ProductPrintDTO getProduct(String internalCode) {
		ProductEntity product = productRepository.findByInternalCodeIgnoreCase(internalCode)
				.orElseThrow(() -> new RuntimeException("Product not found: " + internalCode));
		return ProductPrintDTO.of(product);
	}




}
