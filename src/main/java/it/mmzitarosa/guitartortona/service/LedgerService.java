package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryLightDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

@RequiredArgsConstructor
@Service @Transactional
public class LedgerService {

	/* == CONSTANTS == */
	private final LedgerRepository repository;
	private final BankService bankService;


	public LedgerEntryDTO createLedgerEntry(CreateLedgerEntryDTO input) {
		// Trova bank
		BankEntity bank = input.getBankId()!= null ? bankService.getBank(input.getBankId()) : null;

		// Converto il DTO in oggetto
		LedgerEntryEntity ledgerEntry = new LedgerEntryEntity();
		ledgerEntry.setDate(input.getDate());
		ledgerEntry.setInvoiceNumber(input.getInvoiceNumber());
		ledgerEntry.setInvoiceDate(input.getInvoiceDate());
		ledgerEntry.setDescription(input.getDescription());
		ledgerEntry.setReason(input.getReason());
		ledgerEntry.setPaymentMethod(input.getPaymentMethod());
		ledgerEntry.setBank(bank);
		ledgerEntry.setPaymentType(input.getPaymentType());
		ledgerEntry.setReceiptNumber(input.getReceiptNumber());
		ledgerEntry.setMovementType(input.getMovementType());
		ledgerEntry.setAmount(input.getAmount());
		ledgerEntry.setNotes(input.getNotes());

		// Anche se primo inserimento la salvo già come COMPLETED, //TODO capire se necessaria validazione e salvarlo come DRAFT
		ledgerEntry.setStatus(Status.COMPLETED);
		// Salvo record su DB
		ledgerEntry = repository.save(ledgerEntry);
		// Converto l'entity del record in DTO
		return LedgerEntryDTO.of(ledgerEntry);
	}

	public LedgerEntryDTO readLedgerEntry(long id) {
		return LedgerEntryDTO.of(repository.findById(id).orElseThrow(() -> new IllegalArgumentException("LedgerEntry not found")));
	}

	public List<LedgerEntryLightDTO> readLedger(int year, boolean archived, Sort sort) {
		return repository.findAllByDateGreaterThanEqualAndArchived(LocalDate.of(year, 1, 1), archived, sort).stream().map(LedgerEntryLightDTO::of).toList();
	}

	public LedgerEntryDTO updateLedgerEntry(long id, CreateLedgerEntryDTO input) {
		LedgerEntryEntity ledgerEntry = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("LedgerEntry not found"));

		// Trova bank
		BankEntity bank = input.getBankId()!= null ? bankService.getBank(input.getBankId()) : null;

		// Converto il DTO in oggetto
		ledgerEntry.setDate(input.getDate());
		ledgerEntry.setInvoiceNumber(input.getInvoiceNumber());
		ledgerEntry.setInvoiceDate(input.getInvoiceDate());
		ledgerEntry.setDescription(input.getDescription());
		ledgerEntry.setReason(input.getReason());
		ledgerEntry.setPaymentMethod(input.getPaymentMethod());
		ledgerEntry.setBank(bank);
		ledgerEntry.setPaymentType(input.getPaymentType());
		ledgerEntry.setReceiptNumber(input.getReceiptNumber());
		ledgerEntry.setMovementType(input.getMovementType());
		ledgerEntry.setAmount(input.getAmount());
		ledgerEntry.setNotes(input.getNotes());

		// Salvo record su DB
		ledgerEntry = repository.save(ledgerEntry);
		// Converto l'entity del record in DTO
		return LedgerEntryDTO.of(ledgerEntry);
	}

	public void deleteLedgerEntry(long id) {
		repository.deleteById(id);
	}

}
