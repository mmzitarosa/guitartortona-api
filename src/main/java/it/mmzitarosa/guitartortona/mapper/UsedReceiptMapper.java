package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.CreateUsedReceiptDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptLightDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptProductsDTO;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsedReceiptMapper {

	/* == CONSTANTS == */
	private final PurchaseItemMapper purchaseItemMapper;

	/* == CONSTRUCTOR == */
	public UsedReceiptMapper(PurchaseItemMapper purchaseItemMapper) {
		this.purchaseItemMapper = purchaseItemMapper;
	}


	/* == PUBLIC METHODS == */
	public UsedReceiptDTO toDto(UsedReceiptEntity entity) {
		return toDto(new UsedReceiptDTO(), entity);
	}

	private UsedReceiptLightDTO toLightDto(UsedReceiptEntity entity) {
		UsedReceiptLightDTO dto = new UsedReceiptLightDTO();
		dto.setId(entity.getId());
		dto.setNumber(entity.getNumber());
		dto.setDate(entity.getDate());
		dto.setAmount(entity.getAmount());
		dto.setQuantity(-1); //TODO Quantity
		return dto;
	}

	public List<UsedReceiptDTO> toDto(List<UsedReceiptEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

	public List<UsedReceiptLightDTO> toLightDto(List<UsedReceiptEntity> entities) {
		return entities.stream().map(this::toLightDto).toList();
	}

	public UsedReceiptProductsDTO toProductsDto(UsedReceiptEntity entity) {
		UsedReceiptProductsDTO dto = toDto(new UsedReceiptProductsDTO(), entity);
		dto.setItems(entity.getItems().stream().map(purchaseItemMapper::toDto).toList());
		return dto;
	}

	public UsedReceiptEntity toEntity(CreateUsedReceiptDTO dto) {
		return toEntity(new UsedReceiptEntity(), dto);
	}

	public UsedReceiptEntity toEntity(UsedReceiptEntity entity, CreateUsedReceiptDTO dto) {
		entity.setDate(dto.getDate());
		entity.setNotes(dto.getNotes());
		return entity;
	}

	/* == PRIVATE METHODS == */
	private <T extends UsedReceiptDTO> T toDto(T dto, UsedReceiptEntity entity) {
		dto.setId(entity.getId());
		dto.setNumber(entity.getNumber());
		dto.setInternalCode(entity.getInternalCode());
		dto.setDate(entity.getDate());
		dto.setAmount(entity.getAmount());
		dto.setNotes(entity.getNotes());
		return dto;
	}

}