package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerAlignmentDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptListDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptPrintDTO;
import it.mmzitarosa.guitartortona.entity.AuditLogEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptCounterEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.repository.AuditLogRepository;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.repository.SaleRepository;
import it.mmzitarosa.guitartortona.repository.UsedReceiptCounterRepository;
import it.mmzitarosa.guitartortona.repository.UsedReceiptRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.utils.Constant.MovementType;
import it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import it.mmzitarosa.guitartortona.utils.ReceiptLetters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ciclo di vita: DRAFT -> (conferma) PENDING -> (registra) COMPLETED.
 * Le lettere dei prodotti nascono alla conferma, il numero alla registra: nessuno dei due cambia più.
 */
@RequiredArgsConstructor
@Service @Transactional
public class UsedReceiptService {

	private static final String LEDGER_FIELD = "ledger";

	/** Cosa fare della riga di brogliaccio quando si elimina definitivamente la ricevuta */
	public enum LedgerChoice { ALIGN, IGNORE }


	private final UsedReceiptRepository repository;
	private final UsedReceiptCounterRepository counterRepository;
	private final PurchaseItemRepository purchaseItemRepository;
	private final SaleRepository saleRepository;
	private final CustomerService customerService;
	private final LedgerService ledgerService;
	private final AuditLogRepository auditLogRepository;
	private final CurrentUser currentUser;

	/**
	 * GET /usedReceipts
	 * GET /archive/usedReceipts
	 */
	@Transactional(readOnly = true) public List<UsedReceiptListDTO> getAllUsedReceipts(boolean archived, Sort sort) {
		if (archived && !currentUser.isAdmin())
			return List.of();
		return repository.findAllByArchived(archived, sort).stream().map(UsedReceiptListDTO::of).toList();
	}

	/**
	 * GET /usedReceipt/{id}
	 */
	@Transactional(readOnly = true) public UsedReceiptDetailDTO getUsedReceiptDetail(Long id) {
		return detail(getUsedReceipt(id));
	}

	/**
	 * GET /usedReceipt?code={codice interno}
	 */
	@Transactional(readOnly = true) public UsedReceiptDetailDTO getUsedReceiptDetailByCode(String code) {
		return repository.findByInternalCodeIgnoreCase(code.trim())
				.filter(r -> !r.isArchived() || currentUser.isAdmin())
				.map(this::detail)
				.orElseThrow(() -> new NotFoundException("USED_RECEIPT_NOT_FOUND", "Used receipt not found: " + code));
	}

	/**
	 * GET /usedReceipt/{id}/print: stampabile da PENDING in poi, non in bozza o archiviata
	 */
	@Transactional(readOnly = true) public UsedReceiptPrintDTO getUsedReceiptPrint(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);
		if (receipt.isArchived() || receipt.isDraft())
			throw new ConflictException("USED_RECEIPT_NOT_PRINTABLE", "Only confirmed receipts can be printed: " + id);
		return UsedReceiptPrintDTO.of(receipt);
	}

	/**
	 * POST /usedReceipt
	 */
	public UsedReceiptDetailDTO createUsedReceipt(UsedReceiptInput input) {
		UsedReceiptEntity receipt = new UsedReceiptEntity();
		receipt.setStatus(Status.DRAFT);
		receipt.setArchived(false);
		receipt.setAmount(0.0);
		apply(receipt, input);
		receipt = repository.save(receipt);

		return getUsedReceiptDetail(receipt.getId());
	}

	/**
	 * PUT /usedReceipt/{id}
	 */
	public UsedReceiptDetailDTO updateUsedReceipt(Long id, UsedReceiptInput input) {
		UsedReceiptEntity receipt = getEditableUsedReceipt(id);
		apply(receipt, input);
		repository.save(receipt);

		return getUsedReceiptDetail(id);
	}

	/**
	 * PATCH /usedReceipt/{id}/confirm
	 * DRAFT -> PENDING: assegna le lettere ai prodotti; da qui si può stampare, ma il numero non c'è ancora.
	 */
	public UsedReceiptDetailDTO confirmUsedReceipt(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);

		if (receipt.isArchived() || !receipt.isDraft())
			throw new ConflictException("USED_RECEIPT_NOT_DRAFT", "Only draft receipts can be confirmed: " + id);
		if (receipt.getItems().isEmpty())
			throw new ConflictException("USED_RECEIPT_EMPTY", "Receipt without products cannot be confirmed: " + id);

		for (PurchaseItemEntity item : receipt.getItems())
			if (item.getLetter() == null)
				assignLetter(receipt, item);
		receipt.setStatus(Status.PENDING);
		repository.save(receipt);

		return getUsedReceiptDetail(id);
	}

	/**
	 * PATCH /usedReceipt/{id}/register
	 * PENDING -> COMPLETED: assegna numero (per anno della data ricevuta, non della registrazione) e riga di brogliaccio.
	 */
	public UsedReceiptDetailDTO registerUsedReceipt(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);

		if (receipt.isArchived() || !receipt.isPending())
			throw new ConflictException("USED_RECEIPT_NOT_PENDING", "Only confirmed receipts can be registered: " + id);

		int year = receipt.getDate().getYear();
		counterRepository.createIfMissing(year);
		UsedReceiptCounterEntity counter = counterRepository.findByYearForUpdate(year).orElseThrow();
		counter.setLastNumber(counter.getLastNumber() + 1);
		counterRepository.save(counter);

		receipt.setYear(year);
		receipt.setNumber(counter.getLastNumber());
		// Le ricevute confermate prima che la lettera nascesse alla conferma ne sono ancora prive
		for (PurchaseItemEntity item : receipt.getItems())
			if (item.getLetter() == null)
				assignLetter(receipt, item);
		receipt.setStatus(Status.COMPLETED);
		repository.save(receipt);

		ledgerService.createAutoEntry(receipt.getDate(), receipt.getReference(), receipt.getDate(), "Pagato materiale usato",
				receipt.getCustomerFullName() == null ? "" : receipt.getCustomerFullName(), PaymentMethod.CASH, MovementType.EXPENSE, receipt.getAmount(), receipt.getId());

		return detail(receipt);
	}

	/**
	 * PATCH /usedReceipt/{id}/archive
	 * Eliminazione apparente: lo stato non cambia, quindi il ripristino riporta la ricevuta com'era.
	 * Gli utenti possono archiviare solo le bozze, gli admin qualsiasi ricevuta.
	 */
	public void archiveUsedReceipt(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);

		if (receipt.isArchived())
			return;
		if (!receipt.isDraft() && !currentUser.isAdmin())
			throw new ConflictException("USED_RECEIPT_NOT_ARCHIVABLE", "Only draft receipts can be archived: " + id);

		receipt.setArchived(true);
		repository.save(receipt);
	}

	/**
	 * PATCH /usedReceipt/{id}/restore (solo admin)
	 */
	public UsedReceiptDetailDTO restoreUsedReceipt(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);

		if (receipt.isArchived()) {
			receipt.setArchived(false);
			repository.save(receipt);
		}

		return getUsedReceiptDetail(id);
	}

	/**
	 * DELETE /usedReceipt/{id}?ledger=ALIGN|IGNORE (solo admin)
	 * Cancellazione fisica, ammessa solo per ricevute già archiviate e senza vendite sui prodotti.
	 * Se la riga di brogliaccio collegata è ancora visibile l'admin deve scegliere: ALIGN la elimina, IGNORE la lascia com'è.
	 */
	public void deleteUsedReceipt(Long id, LedgerChoice ledger) {
		UsedReceiptEntity receipt = getUsedReceipt(id);

		if (!receipt.isArchived())
			throw new ConflictException("USED_RECEIPT_NOT_ARCHIVED", "Only archived receipts can be permanently deleted: " + id);
		if (receipt.getItems().stream().anyMatch(item -> saleRepository.existsByProductId(item.getProduct().getId())))
			throw new ConflictException("USED_RECEIPT_HAS_SALES", "Receipt has products already sold: " + id);

		Optional<LedgerEntryEntity> row = ledgerService.findByUsedReceipt(id).filter(e -> !e.isArchived());
		if (row.isPresent()) {
			if (ledger == null)
				throw new ConflictException("USED_RECEIPT_LEDGER_CHOICE_REQUIRED", "Choose what to do with the ledger entry of receipt: " + id);
			if (ledger == LedgerChoice.ALIGN)
				ledgerService.archiveLedgerEntry(row.get().getId());
			audit(receipt, ledger == LedgerChoice.ALIGN ? AuditLogEntity.ALIGN : AuditLogEntity.IGNORE, "Ledger entry " + row.get().getId() + " on receipt deletion");
		}

		// I trigger sullo stock partono solo se le righe vengono cancellate esplicitamente (non dal cascade del DB)
		purchaseItemRepository.deleteAll(new ArrayList<>(receipt.getItems()));
		purchaseItemRepository.flush();
		repository.delete(receipt);
	}

	/**
	 * PATCH /usedReceipt/{id}/ledger/align (solo admin)
	 * Porta la riga di brogliaccio ai valori della ricevuta, oppure la elimina se la ricevuta è eliminata.
	 */
	public UsedReceiptDetailDTO alignLedger(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);
		LedgerAlignmentDTO alignment = pendingAlignment(receipt)
				.orElseThrow(() -> new ConflictException("USED_RECEIPT_LEDGER_ALIGNED", "Nothing to align for receipt: " + id));

		if (alignment.action() == LedgerAlignmentDTO.Action.ARCHIVE)
			ledgerService.archiveLedgerEntry(alignment.ledgerEntryId());
		else
			ledgerService.alignEntry(ledgerService.getLedgerEntry(alignment.ledgerEntryId()), receipt.getDate(), ledgerReason(receipt), receipt.getAmount());
		audit(receipt, AuditLogEntity.ALIGN, describe(alignment));

		return detail(receipt);
	}

	/**
	 * PATCH /usedReceipt/{id}/ledger/ignore (solo admin)
	 * Lascia la riga di brogliaccio com'è; la scelta resta nell'audit.
	 */
	public UsedReceiptDetailDTO ignoreLedger(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);
		LedgerAlignmentDTO alignment = pendingAlignment(receipt)
				.orElseThrow(() -> new ConflictException("USED_RECEIPT_LEDGER_ALIGNED", "Nothing to ignore for receipt: " + id));

		audit(receipt, AuditLogEntity.IGNORE, describe(alignment));

		return detail(receipt);
	}

	/**
	 * Helper: dettaglio con riga di brogliaccio collegata e, per l'admin, la differenza da allineare
	 */
	private UsedReceiptDetailDTO detail(UsedReceiptEntity receipt) {
		Long ledgerEntryId = ledgerService.findByUsedReceipt(receipt.getId()).map(LedgerEntryEntity::getId).orElse(null);
		return UsedReceiptDetailDTO.of(receipt, ledgerEntryId, pendingAlignment(receipt).orElse(null));
	}

	/**
	 * Helper: differenza tra ricevuta registrata e riga collegata. Non c'è nulla da proporre se la riga è già eliminata
	 * o se coincide con la ricevuta; una ricevuta eliminata propone di eliminare anche la riga.
	 */
	private Optional<LedgerAlignmentDTO> pendingAlignment(UsedReceiptEntity receipt) {
		if (!currentUser.isAdmin() || !receipt.isCompleted())
			return Optional.empty();
		return proposal(receipt).filter(alignment -> !alreadyIgnored(receipt, alignment));
	}

	/**
	 * Helper: una differenza già ignorata non viene riproposta finché la ricevuta o la riga non cambiano ancora
	 */
	private boolean alreadyIgnored(UsedReceiptEntity receipt, LedgerAlignmentDTO alignment) {
		return auditLogRepository.findFirstByEntityTypeAndEntityIdAndFieldOrderByIdDesc("UsedReceipt", receipt.getId(), LEDGER_FIELD)
				.filter(l -> AuditLogEntity.IGNORE.equals(l.getOperation()) && describe(alignment).equals(l.getNewValue()))
				.isPresent();
	}

	private Optional<LedgerAlignmentDTO> proposal(UsedReceiptEntity receipt) {
		return ledgerService.findByUsedReceipt(receipt.getId()).filter(e -> !e.isArchived()).flatMap(row -> {
			LedgerAlignmentDTO.Values current = new LedgerAlignmentDTO.Values(row.getDate(), row.getReason(), row.getAmount());
			if (receipt.isArchived())
				return Optional.of(new LedgerAlignmentDTO(row.getId(), LedgerAlignmentDTO.Action.ARCHIVE, current, null));
			LedgerAlignmentDTO.Values proposed = new LedgerAlignmentDTO.Values(receipt.getDate(), ledgerReason(receipt), receipt.getAmount());
			boolean same = Objects.equals(current.date(), proposed.date()) && Objects.equals(current.reason(), proposed.reason())
					&& current.amount() != null && proposed.amount() != null && Math.abs(current.amount() - proposed.amount()) < 0.005;
			return same ? Optional.empty() : Optional.of(new LedgerAlignmentDTO(row.getId(), LedgerAlignmentDTO.Action.UPDATE, current, proposed));
		});
	}

	private static String ledgerReason(UsedReceiptEntity receipt) {
		return receipt.getCustomerFullName() == null ? "" : receipt.getCustomerFullName();
	}

	private static String describe(LedgerAlignmentDTO alignment) {
		return "Ledger entry " + alignment.ledgerEntryId() + " " + alignment.action() + ": " + alignment.current() + " -> " + alignment.proposed();
	}

	/**
	 * Helper: la scelta sul brogliaccio è una decisione dell'utente, non una modifica di un campo: va nel registro di audit
	 */
	private void audit(UsedReceiptEntity receipt, String operation, String detail) {
		AuditLogEntity log = new AuditLogEntity();
		log.setCreatedDate(LocalDateTime.now());
		log.setUsername(currentUser.username());
		log.setEntityType("UsedReceipt");
		log.setEntityId(receipt.getId());
		log.setOperation(operation);
		log.setField(LEDGER_FIELD);
		log.setNewValue(detail);
		auditLogRepository.save(log);
	}

	/**
	 * Helper: ricalcola l'importo come somma delle righe
	 */
	protected void recalculateAmount(UsedReceiptEntity receipt) {
		receipt.setAmount(purchaseItemRepository.sumAmountByPurchaseId(receipt.getId()));
		repository.save(receipt);
	}

	/**
	 * Helper: assegna la lettera successiva all'ultima mai assegnata (le lettere delle righe rimosse non si riusano)
	 */
	protected void assignLetter(UsedReceiptEntity receipt, PurchaseItemEntity item) {
		receipt.setLastLetterIndex(receipt.getLastLetterIndex() + 1);
		item.setLetter(ReceiptLetters.fromIndex(receipt.getLastLetterIndex()));
	}

	/**
	 * Helper: Recupera ricevuta modificabile (non archiviata; per gli utenti solo bozze, gli admin possono modificare anche le altre)
	 */
	protected UsedReceiptEntity getEditableUsedReceipt(long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);
		if (receipt.isArchived() || (!receipt.isDraft() && !currentUser.isAdmin()))
			throw new ConflictException("USED_RECEIPT_NOT_EDITABLE", "Receipt is not editable: " + id);
		return receipt;
	}

	/**
	 * Helper: Recupera ricevuta. Le archiviate sono visibili solo agli admin.
	 */
	protected UsedReceiptEntity getUsedReceipt(long id) {
		return repository.findById(id)
				.filter(r -> !r.isArchived() || currentUser.isAdmin())
				.orElseThrow(() -> new NotFoundException("USED_RECEIPT_NOT_FOUND", "Used receipt not found: " + id));
	}

	/**
	 * Helper: copia i dati in ingresso. I dati cliente sono una copia sulla ricevuta; l'anagrafica si aggiorna solo se richiesto.
	 */
	private void apply(UsedReceiptEntity receipt, UsedReceiptInput input) {
		// Anno e numero nascono alla registrazione e non cambiano più: la data può muoversi solo dentro quell'anno
		if (receipt.getYear() != null && input.date().getYear() != receipt.getYear())
			throw new ConflictException("USED_RECEIPT_YEAR_FROZEN", "Registered receipt date must stay in year " + receipt.getYear());
		receipt.setDate(input.date());
		receipt.setNotes(CustomerService.blankToNull(input.notes()));

		CustomerInput customer = input.customer();
		if (customer == null) {
			receipt.setCustomer(null);
			receipt.setCustomerFirstName(null);
			receipt.setCustomerLastName(null);
			receipt.setCustomerAddress(null);
			receipt.setCustomerZipCode(null);
			receipt.setCustomerCity(null);
			receipt.setCustomerProvince(null);
			receipt.setCustomerDocumentType(null);
			receipt.setCustomerDocumentNumber(null);
			receipt.setCustomerTaxCode(null);
			return;
		}
		receipt.setCustomer(customerService.resolveCustomer(customer, input.updateCustomer()));
		receipt.setCustomerFirstName(customer.firstName().trim());
		receipt.setCustomerLastName(customer.lastName().trim());
		receipt.setCustomerAddress(customer.address().trim());
		receipt.setCustomerZipCode(customer.zipCode().trim());
		receipt.setCustomerCity(customer.city().trim());
		receipt.setCustomerProvince(customer.province().trim());
		receipt.setCustomerDocumentType(CustomerService.blankToNull(customer.documentType()));
		receipt.setCustomerDocumentNumber(CustomerService.blankToNull(customer.documentNumber()));
		receipt.setCustomerTaxCode(CustomerService.blankToNull(customer.taxCode()));
	}

}
