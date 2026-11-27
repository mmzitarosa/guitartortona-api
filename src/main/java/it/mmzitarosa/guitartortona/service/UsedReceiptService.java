package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptListDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptPrintDTO;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptCounterEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Ciclo di vita: DRAFT -> (conferma) PENDING -> (registra) COMPLETED.
 * Le lettere dei prodotti nascono alla conferma, il numero alla registra: nessuno dei due cambia più.
 */
@RequiredArgsConstructor
@Service @Transactional
public class UsedReceiptService {

	private final UsedReceiptRepository repository;
	private final UsedReceiptCounterRepository counterRepository;
	private final PurchaseItemRepository purchaseItemRepository;
	private final SaleRepository saleRepository;
	private final CustomerService customerService;
	private final LedgerService ledgerService;
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
		return UsedReceiptDetailDTO.of(getUsedReceipt(id));
	}

	/**
	 * GET /usedReceipt?code={codice interno}
	 */
	@Transactional(readOnly = true) public UsedReceiptDetailDTO getUsedReceiptDetailByCode(String code) {
		return repository.findByInternalCodeIgnoreCase(code.trim())
				.filter(r -> !r.isArchived() || currentUser.isAdmin())
				.map(UsedReceiptDetailDTO::of)
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
				receipt.getCustomerFullName() == null ? "" : receipt.getCustomerFullName(), PaymentMethod.CASH, MovementType.EXPENSE, receipt.getAmount());

		return UsedReceiptDetailDTO.of(receipt);
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

		if (receipt.isCompleted() && hasSoldItems(receipt))
			throw new ConflictException("USED_RECEIPT_HAS_SALES", "Receipt has products already sold: " + id);

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
	 * DELETE /usedReceipt/{id} (solo admin)
	 * Cancellazione fisica, ammessa solo per ricevute già archiviate e senza vendite sui prodotti.
	 */
	public void deleteUsedReceipt(Long id) {
		UsedReceiptEntity receipt = getUsedReceipt(id);

		if (!receipt.isArchived())
			throw new ConflictException("USED_RECEIPT_NOT_ARCHIVED", "Only archived receipts can be permanently deleted: " + id);
		if (hasSoldItems(receipt))
			throw new ConflictException("USED_RECEIPT_HAS_SALES", "Receipt has products already sold: " + id);

		// I trigger sullo stock partono solo se le righe vengono cancellate esplicitamente (non dal cascade del DB)
		purchaseItemRepository.deleteAll(new ArrayList<>(receipt.getItems()));
		purchaseItemRepository.flush();
		repository.delete(receipt);
	}

	private boolean hasSoldItems(UsedReceiptEntity receipt) {
		return receipt.getItems().stream().anyMatch(item -> saleRepository.existsByProductId(item.getProduct().getId()));
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
