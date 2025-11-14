package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceProductsDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.mapper.IncomingInvoiceMapper;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class IncomingInvoiceService {

	/* == CONSTANTS == */
	private final IncomingInvoiceRepository repository;
	private final IncomingInvoiceMapper mapper;
	private final SupplierService supplierService;

	/* == CONSTRUCTOR == */
	public IncomingInvoiceService(IncomingInvoiceRepository repository, IncomingInvoiceMapper mapper, SupplierService supplierService) {
		this.repository = repository;
		this.mapper = mapper;
		this.supplierService = supplierService;
	}

	/* == PUBLIC METHODS == */
	public IncomingInvoiceDTO createIncomingInvoice(CreateIncomingInvoiceDTO dto) {
		// Converto il DTO in oggetto, passo anche il fornitore recuperato da DB o lo creo
		IncomingInvoiceEntity incomingInvoice = mapper.toEntity(dto, supplierService.getOrInsertSupplier(dto.getSupplierId(), dto.getSupplierName()));
		// Essendo primo inserimento la salvo come bozza
		incomingInvoice.setStatus(Status.DRAFT);
		// Salvo fattura su DB
		incomingInvoice = repository.save(incomingInvoice);
		// Converto l'entity della fattura in DTO
		return mapper.toDto(incomingInvoice);
	}

	public IncomingInvoiceProductsDTO readIncomingInvoice(long id) {
		return mapper.toProductsDto(getIncomingInvoice(id));
	}

	public Page<IncomingInvoiceDTO> readIncomingInvoices(Pageable pageable, Status... statuses) {
		return mapper.toDto(repository.findAllByStatusIn(List.of(statuses), pageable));
	}

	public IncomingInvoiceDTO updateIncomingInvoice(long id, CreateIncomingInvoiceDTO dto) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);
		// Converto il DTO in oggetto, passo anche il fornitore recuperato da DB o lo creo
		incomingInvoice = mapper.toEntity(incomingInvoice, dto, supplierService.getOrInsertSupplier(dto.getSupplierId(), dto.getSupplierName()));
		// Salvo fattura su DB
		incomingInvoice = repository.save(incomingInvoice);
		// Converto l'entity della fattura in DTO
		return mapper.toDto(incomingInvoice);
	}

	public void completeIncomingInvoice(long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);
		// Aggiorno lo stato della fattura
		incomingInvoice.setStatus(Status.COMPLETED);
		// Per ogni item aggiorno lo stato della relazione e prodotto associato
		for (PurchaseItemEntity item : incomingInvoice.getItems()) {
			// Aggiorno lo stato della relazione
			item.setStatus(Status.COMPLETED);
			// Aggiorno lo stato del prodotto, potrebbe già essere completed
			item.getProduct().setStatus(Status.COMPLETED);
		}
		// Salvo il tutto su DB
		repository.save(incomingInvoice);
	}

	public void deleteIncomingInvoice(long id) {
		IncomingInvoiceEntity incomingInvoice = getIncomingInvoice(id);
		// Doppio comportamento, si basa su stato precedente
		// Se già ARCHIVED da cambio di stato precedente, viene eliminato
		if (incomingInvoice.getStatus() == Status.ARCHIVED) {
			// Elimino da fattura
			// L'eliminazione della fattura elimina in cascata anche la relazione.
			// Il prodotto rimane così come da ultimo salvataggio, potrebbe essere usato da altre relazioni
			repository.deleteById(id);
		} else {
			// Aggiorno lo stato della fattura
			incomingInvoice.setStatus(Status.ARCHIVED);
			// Per ogni item aggiorno lo stato della relazione
			for (PurchaseItemEntity item : incomingInvoice.getItems()) {
				// Aggiorno lo stato della relazione
				// Il prodotto rimane così come da ultimo salvataggio, potrebbe essere usato da altre relazioni
				item.setStatus(Status.ARCHIVED);
			}
			// Salvo il tutto su DB
			repository.save(incomingInvoice);
		}
	}

	/* == PACKAGE METHODS == */
	IncomingInvoiceEntity getIncomingInvoice(long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("IncomingInvoice not found"));
	}

}
