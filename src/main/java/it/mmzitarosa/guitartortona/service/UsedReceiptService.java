package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.CreateUsedReceiptDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptProductsDTO;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.mapper.UsedReceiptMapper;
import it.mmzitarosa.guitartortona.repository.UsedReceiptRepository;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class UsedReceiptService {

	/* == CONSTANTS == */
	private final UsedReceiptRepository repository;
	private final UsedReceiptMapper mapper;
	private final ProductService productService;

	/* == CONSTRUCTOR == */
	public UsedReceiptService(UsedReceiptRepository repository, UsedReceiptMapper mapper, ProductService productService) {
		this.repository = repository;
		this.mapper = mapper;
		this.productService = productService;
	}

	/* == PUBLIC METHODS == */
	public UsedReceiptDTO createUsedReceipt(CreateUsedReceiptDTO dto) {
		// Converto il DTO in oggetto
		UsedReceiptEntity entity = mapper.toEntity(new UsedReceiptEntity(), dto);
		// Essendo primo inserimento la salvo come bozza
		entity.setStatus(Status.DRAFT);
		// Salvo ricevuta su DB
		entity = repository.save(entity);
		// Converto l'entity della ricevuta in DTO
		return mapper.toDto(entity);
	}

	public UsedReceiptProductsDTO readUsedReceipt(long id) {
		return mapper.toProductsDto(getUsedReceipt(id));
	}

	public List<UsedReceiptDTO> readUsedReceipts(Status status) {
		return mapper.toDto(repository.findAllByStatus(status));
	}

	public Page<UsedReceiptDTO> readUsedReceipts(Pageable pageable) {
		return mapper.toDto(repository.findAllByStatus(Status.COMPLETED, pageable));
	}


	public UsedReceiptDTO updateUsedReceipt(long id, CreateUsedReceiptDTO dto) {
		UsedReceiptEntity usedReceipt = getUsedReceipt(id);
		// Converto il DTO in oggetto
		usedReceipt = mapper.toEntity(usedReceipt, dto);
		// Salvo ricevuta su DB
		usedReceipt = repository.save(usedReceipt);
		// Converto l'entity della ricevuta in DTO
		return mapper.toDto(usedReceipt);
	}

	public void completeUsedReceipt(long id) {
		UsedReceiptEntity usedReceipt = getUsedReceipt(id);
		// Aggiorno lo stato della ricevuta
		usedReceipt.setStatus(Status.COMPLETED);
		// Per ogni item aggiorno lo stato della relazione e prodotto associato
		for (PurchaseItemEntity item : usedReceipt.getItems()) {
			// Aggiorno lo stato della relazione
			item.setStatus(Status.COMPLETED);
			// Aggiorno lo stato del prodotto, potrebbe già essere completed
			item.getProduct().setStatus(Status.COMPLETED);
		}
		// Salvo il tutto su DB
		repository.save(usedReceipt);
	}

	public void deleteUsedReceipt(long id) {
		UsedReceiptEntity usedReceipt = getUsedReceipt(id);
		// Doppio comportamento, si basa su stato precedente
		// Se già ARCHIVED da cambio di stato precedente, viene eliminato
		if (usedReceipt.getStatus() == Status.ARCHIVED) {
			// Elimino da ricevuta
			// L'eliminazione della ricevuta elimina in cascata anche la relazione.
			// A differenza delle fatture, qui devo eliminare anche il prodotto //TODO
			repository.deleteById(id);
			for (PurchaseItemEntity item : usedReceipt.getItems()) {
				productService.deleteProduct(item.getId());
			}
		} else {
			// Aggiorno lo stato della ricevuta
			usedReceipt.setStatus(Status.ARCHIVED);
			// Per ogni item aggiorno lo stato della relazione
			for (PurchaseItemEntity item : usedReceipt.getItems()) {
				// Aggiorno lo stato della relazione
				// A differenza delle fatture, qui devo aggiornare anche il prodotto
				item.setStatus(Status.ARCHIVED);
				item.getProduct().setStatus(Status.ARCHIVED);
			}
			// Salvo il tutto su DB
			repository.save(usedReceipt);
		}
	}

	/* == PACKAGE METHODS == */
	UsedReceiptEntity getUsedReceipt(long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("UsedReceipt not found"));
	}
}
