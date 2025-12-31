package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.repository.UsedReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service @Transactional
public class UsedReceiptService {

	/* == CONSTANTS == */
	private final UsedReceiptRepository repository;
	private final ProductService productService;

	/* == PUBLIC METHODS == */
/*	public UsedReceiptDTO createUsedReceipt(CreateUsedReceiptDTO dto) {
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

	public List<UsedReceiptLightDTO> readUsedReceipts(boolean archived, Sort sort) {
		return mapper.toLightDto(repository.findAllByArchived(archived, sort));
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
		// Salvo il tutto su DB
		repository.save(usedReceipt);
	}

	public void deleteUsedReceipt(long id) {
		UsedReceiptEntity usedReceipt = getUsedReceipt(id);
		// Doppio comportamento, si basa su stato precedente
		// Se già ARCHIVED da cambio di stato precedente, viene eliminato
		if (usedReceipt.isArchived()) {
			// Elimino da ricevuta
			// L'eliminazione della ricevuta elimina in cascata anche la relazione.
			// A differenza delle fatture, qui devo eliminare anche il prodotto //TODO
			repository.deleteById(id);
			for (PurchaseItemEntity item : usedReceipt.getItems()) {
				productService.deleteProduct(item.getId());
			}
		} else {
			// Aggiorno lo stato della ricevuta
			usedReceipt.setArchived(true);
			// Salvo il tutto su DB
			repository.save(usedReceipt);
		}
	}

	/* == PACKAGE METHODS == */
	UsedReceiptEntity getUsedReceipt(long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("UsedReceipt not found"));
	}
}
