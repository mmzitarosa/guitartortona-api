package it.mmzitarosa.guitartortona.service;

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

	/**
	 * GET /incomingInvoices
	 * GET /archived/incomingInvoices
	 */
	@Transactional(readOnly = true) public List<IncomingInvoiceListDTO> getAllIncomingInvoices(boolean archived, Sort sort) {
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
			throw new RuntimeException("Invoice number already exists for this supplier: " + input.number());
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
			throw new RuntimeException("Invoice number already exists for this supplier: " + input.number());
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

		if (!incomingInvoice.isCompleted()) {
			// Completa purchase
			incomingInvoice.setStatus(Status.COMPLETED);
			repository.save(incomingInvoice);
		}

		return getIncomingInvoiceDetail(id);
	}

	/**
	 * PATCH /incomingInvoice/{id}/restore
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
	 * DELETE /incomingInvoice/{id}
	 */
	public void deleteIncomingInvoice(Long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);

		if (incomingInvoice.isArchived()) {
			// Già archiviata -> elimina
			repository.delete(incomingInvoice);
		} else {
			// Prima volta -> archivia
			incomingInvoice.setArchived(true);
			repository.save(incomingInvoice);
		}
	}

	/**
	 * Helper: Recupera incomingInvoice
	 */
	protected IncomingInvoiceEntity getIncomingInvoice(long id) {
		return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
	}

}
