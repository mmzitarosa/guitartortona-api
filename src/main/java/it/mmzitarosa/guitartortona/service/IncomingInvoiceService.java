package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.exception.ConflictException;

import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceDetailDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceInput;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceListDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service @Transactional
public class IncomingInvoiceService {

	private final IncomingInvoiceRepository repository;
	private final SupplierService supplierService;
	private final ProductService productService;
	private final it.mmzitarosa.guitartortona.security.CurrentUser currentUser;

	/**
	 * GET /incomingInvoices
	 * GET /archived/incomingInvoices
	 */
	@Transactional(readOnly = true) public List<IncomingInvoiceListDTO> getAllIncomingInvoices(boolean archived, Sort sort) {
		if (archived && !currentUser.isAdmin())
			return List.of();
		return repository.findAllByArchived(archived, sort).stream()
				.map(IncomingInvoiceListDTO::of)
				.toList();
	}

	/**
	 * GET /incomingInvoice/{id}
	 */
	@Transactional(readOnly = true) public IncomingInvoiceDetailDTO getIncomingInvoiceDetail(Long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		return IncomingInvoiceDetailDTO.of(incomingInvoice);
	}

	/**
	 * POST /incomingInvoice
	 */
	public IncomingInvoiceDetailDTO createIncomingInvoice(IncomingInvoiceInput input) {
		// Trova o crea supplier
		SupplierEntity supplier = supplierService.findOrCreateSupplier(input.supplier());

		// Verifica univocità numero fattura per supplier
		if (repository.existsBySupplierAndNumber(supplier, input.number())) {
			throw new ConflictException("INVOICE_NUMBER_ALREADY_EXISTS", "Invoice number already exists for this supplier: " + input.number());
		}

		// Crea purchase
		IncomingInvoiceEntity incomingInvoice = new IncomingInvoiceEntity();
		incomingInvoice.setSupplier(supplier);
		incomingInvoice.setDate(input.date());
		incomingInvoice.setNumber(input.number());
		incomingInvoice.setAmount(input.amount());
		incomingInvoice.setNotes(input.notes());
		incomingInvoice.setStatus(Status.DRAFT);
		incomingInvoice.setArchived(false);
		incomingInvoice = repository.save(incomingInvoice);

		return getIncomingInvoiceDetail(incomingInvoice.getId());
	}

	/**
	 * PUT /incomingInvoice/{id}
	 */
	public IncomingInvoiceDetailDTO updateIncomingInvoice(Long id, IncomingInvoiceInput input) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		// Trova o crea supplier
		SupplierEntity supplier = supplierService.findOrCreateSupplier(input.supplier());

		// Verifica univocità numero fattura (escludi se stessa)
		if (!incomingInvoice.getNumber().equals(input.number()) &&
				repository.existsBySupplierAndNumber(supplier, input.number())) {
			throw new ConflictException("INVOICE_NUMBER_ALREADY_EXISTS", "Invoice number already exists for this supplier: " + input.number());
		}

		// Aggiorna invoice
		incomingInvoice.setSupplier(supplier);
		incomingInvoice.setDate(input.date());
		incomingInvoice.setNumber(input.number());
		incomingInvoice.setAmount(input.amount());
		incomingInvoice.setNotes(input.notes());
		repository.save(incomingInvoice);

		return getIncomingInvoiceDetail(id);
	}


	/**
	 * PATCH /incomingInvoice/{id}/complete
	 */
	public IncomingInvoiceDetailDTO completeIncomingInvoice(Long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		if (incomingInvoice.isArchived())
			throw new ConflictException("INVOICE_ARCHIVED", "Archived invoice cannot be completed: " + id);

		if (!incomingInvoice.isCompleted()) {
			// Completa purchase
			incomingInvoice.setStatus(Status.COMPLETED);
			repository.save(incomingInvoice);
		}

		return getIncomingInvoiceDetail(id);
	}

	/**
	 * PATCH /incomingInvoice/{id}/archive
	 * Eliminazione apparente: lo stato non cambia, quindi il ripristino riporta la fattura com'era.
	 * Gli utenti possono archiviare solo le bozze, gli admin qualsiasi fattura.
	 */
	public void archiveIncomingInvoice(Long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		if (incomingInvoice.isArchived())
			return;
		if (!incomingInvoice.isDraft() && !currentUser.isAdmin())
			throw new ConflictException("INVOICE_NOT_ARCHIVABLE", "Only draft invoices can be archived: " + id);

		// L'archiviazione toglie dalla giacenza le quantità acquistate: non possono essere già state vendute
		if (incomingInvoice.isCompleted())
			incomingInvoice.getItems().forEach(item -> productService.assertStockNotNegative(item.getProduct(), -item.getQuantity()));

		incomingInvoice.setArchived(true);
		repository.save(incomingInvoice);
	}

	/**
	 * PATCH /incomingInvoice/{id}/restore (solo admin)
	 */
	public IncomingInvoiceDetailDTO restoreIncomingInvoice(Long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		if (incomingInvoice.isArchived()) {
			incomingInvoice.setArchived(false);
			repository.save(incomingInvoice);
		}

		return getIncomingInvoiceDetail(id);
	}

	/**
	 * DELETE /incomingInvoice/{id} (solo admin)
	 * Cancellazione fisica, ammessa solo per fatture già archiviate.
	 */
	public void deleteIncomingInvoice(Long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		if (!incomingInvoice.isArchived())
			throw new ConflictException("INVOICE_NOT_ARCHIVED", "Only archived invoices can be permanently deleted: " + id);

		repository.delete(incomingInvoice);
	}

	/**
	 * Helper: Recupera incomingInvoice modificabile (non archiviata; per gli utenti solo bozze, gli admin possono modificare anche le confermate)
	 */
	protected IncomingInvoiceEntity getEditableIncomingInvoice(long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);
		if (incomingInvoice.isArchived() || (!incomingInvoice.isDraft() && !currentUser.isAdmin()))
			throw new ConflictException("INVOICE_NOT_EDITABLE", "Invoice is not editable: " + id);
		return incomingInvoice;
	}

	/**
	 * Helper: Recupera incomingInvoice. Le archiviate sono visibili solo agli admin.
	 */
	protected IncomingInvoiceEntity getIncomingInvoice(long id) {
		return repository.findById(id)
				.filter(i -> !i.isArchived() || currentUser.isAdmin())
				.orElseThrow(() -> new NotFoundException("INVOICE_NOT_FOUND", "Invoice not found: " + id));
	}

}
