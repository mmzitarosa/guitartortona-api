package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.AddIncomingInvoiceProductDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.UpdateIncomingInvoiceProductDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.AddUsedReceiptProductDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UpdateUsedReceiptProductDTO;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import org.springframework.stereotype.Component;

@Component public class PurchaseItemMapper {

	/* == CONSTANTS == */
	private final ProductMapper productMapper;

	/* == CONSTRUCTOR == */
	public PurchaseItemMapper(ProductMapper productMapper) {
		this.productMapper = productMapper;
	}

	/* == PUBLIC METHODS == */
	public PurchaseItemDTO toDto(PurchaseItemEntity entity) {
		PurchaseItemDTO dto = new PurchaseItemDTO();
		dto.setId(entity.getId());
		dto.setProduct(productMapper.toDto(entity.getProduct()));
		dto.setQuantity(entity.getQuantity());
		dto.setVat(entity.getVat());
		dto.setPurchasePrice(entity.getPurchasePrice());
		return dto;
	}

	public PurchaseItemEntity toEntity(AddIncomingInvoiceProductDTO dto, PurchaseEntity incomingInvoice, ProductEntity product) {
		return toEntity(new PurchaseItemEntity(), dto, incomingInvoice, product);
	}


	public PurchaseItemEntity toEntity(PurchaseItemEntity entity, AddIncomingInvoiceProductDTO dto, PurchaseEntity incomingInvoice, ProductEntity product) {
		entity.setPurchase(incomingInvoice);
		entity.setProduct(product);
		entity.setQuantity(dto.getQuantity());
		entity.setVat(dto.getVat());
		entity.setPurchasePrice(dto.getPurchasePrice());
		return entity;
	}

	public PurchaseItemEntity toEntity(AddUsedReceiptProductDTO dto, PurchaseEntity usedReceipt, ProductEntity product) {
		return toEntity(new PurchaseItemEntity(), dto, usedReceipt, product);
	}

	public PurchaseItemEntity toEntity(PurchaseItemEntity entity, AddUsedReceiptProductDTO dto, PurchaseEntity usedReceipt, ProductEntity product) {
		entity.setPurchase(usedReceipt);
		entity.setProduct(product);
		entity.setPurchasePrice(dto.getPurchasePrice());
		return entity;
	}

	public PurchaseItemEntity toEntity(PurchaseItemEntity entity, UpdateIncomingInvoiceProductDTO dto) {
		entity.setQuantity(dto.getQuantity());
		entity.setVat(dto.getVat());
		entity.setPurchasePrice(dto.getPurchasePrice());
		return entity;
	}

	public PurchaseItemEntity toEntity(PurchaseItemEntity entity, UpdateUsedReceiptProductDTO dto) {
		entity.setPurchasePrice(dto.getPurchasePrice());
		return entity;
	}

}
