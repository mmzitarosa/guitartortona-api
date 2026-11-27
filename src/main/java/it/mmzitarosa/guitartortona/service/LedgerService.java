package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryLightDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.MovementType;
import static it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod;
import static it.mmzitarosa.guitartortona.utils.Constant.Status;

@RequiredArgsConstructor
@Service @Transactional
public class LedgerService {

	/* == CONSTANTS == */
	private final LedgerRepository repository;
	private final BankService bankService;
	private final CurrentUser currentUser;


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

	/**
	 * Helper: riga di brogliaccio generata automaticamente da un altro documento (resta PENDING finché non viene verificata)
	 */
	protected void createAutoEntry(LocalDate date, String invoiceNumber, LocalDate invoiceDate, String description, String reason,
								   PaymentMethod paymentMethod, MovementType movementType, Double amount) {
		LedgerEntryEntity ledgerEntry = new LedgerEntryEntity();
		ledgerEntry.setDate(date);
		ledgerEntry.setInvoiceNumber(invoiceNumber);
		ledgerEntry.setInvoiceDate(invoiceDate);
		ledgerEntry.setDescription(description);
		ledgerEntry.setReason(reason);
		ledgerEntry.setPaymentMethod(paymentMethod);
		ledgerEntry.setMovementType(movementType);
		ledgerEntry.setAmount(amount);
		ledgerEntry.setStatus(Status.PENDING);
		repository.save(ledgerEntry);
	}

	public LedgerEntryDTO readLedgerEntry(long id) {
		return LedgerEntryDTO.of(getLedgerEntry(id));
	}

	/** Le righe archiviate sono visibili solo agli admin */
	public List<LedgerEntryLightDTO> readLedger(int year, boolean archived, Sort sort) {
		if (archived && !currentUser.isAdmin())
			return List.of();
		return repository.findAllByDateGreaterThanEqualAndArchived(LocalDate.of(year, 1, 1), archived, sort).stream().map(LedgerEntryLightDTO::of).toList();
	}

	public LedgerEntryDTO updateLedgerEntry(long id, CreateLedgerEntryDTO input) {
		LedgerEntryEntity ledgerEntry = getEditableLedgerEntry(id);

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

	/**
	 * PATCH /ledgerEntry/{id}/archive
	 * Eliminazione apparente, consentita su qualsiasi riga: lo stato non cambia, quindi il ripristino la riporta com'era.
	 */
	public void archiveLedgerEntry(long id) {
		LedgerEntryEntity ledgerEntry = getLedgerEntry(id);
		if (ledgerEntry.isArchived())
			return;
		ledgerEntry.setArchived(true);
		repository.save(ledgerEntry);
	}

	/**
	 * PATCH /ledgerEntry/{id}/restore (solo admin)
	 */
	public LedgerEntryDTO restoreLedgerEntry(long id) {
		LedgerEntryEntity ledgerEntry = getLedgerEntry(id);
		if (ledgerEntry.isArchived()) {
			ledgerEntry.setArchived(false);
			ledgerEntry = repository.save(ledgerEntry);
		}
		return LedgerEntryDTO.of(ledgerEntry);
	}

	/**
	 * DELETE /ledgerEntry/{id} (solo admin)
	 * Cancellazione fisica, ammessa solo per righe già archiviate.
	 */
	public void deleteLedgerEntry(long id) {
		LedgerEntryEntity ledgerEntry = getLedgerEntry(id);
		if (!ledgerEntry.isArchived())
			throw new ConflictException("LEDGER_ENTRY_NOT_ARCHIVED", "Only archived ledger entries can be permanently deleted: " + id);
		repository.delete(ledgerEntry);
	}

	/**
	 * Helper: riga non archiviata, quindi modificabile
	 */
	protected LedgerEntryEntity getEditableLedgerEntry(long id) {
		LedgerEntryEntity ledgerEntry = getLedgerEntry(id);
		if (ledgerEntry.isArchived())
			throw new ConflictException("LEDGER_ENTRY_NOT_EDITABLE", "Ledger entry is not editable: " + id);
		return ledgerEntry;
	}

	/**
	 * Helper: recupera la riga. Le archiviate sono visibili solo agli admin.
	 */
	protected LedgerEntryEntity getLedgerEntry(long id) {
		return repository.findById(id)
				.filter(e -> !e.isArchived() || currentUser.isAdmin())
				.orElseThrow(() -> new NotFoundException("LEDGER_ENTRY_NOT_FOUND", "LedgerEntry not found"));
	}

}
