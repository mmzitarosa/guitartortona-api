package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceProductsDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class IncomingInvoiceMapper {

	/* == CONSTANTS == */
	private final SupplierMapper supplierMapper;
	private final PurchaseItemMapper purchaseItemMapper;

	/* == CONSTRUCTOR == */
	public IncomingInvoiceMapper(SupplierMapper supplierMapper, PurchaseItemMapper purchaseItemMapper) {
		this.supplierMapper = supplierMapper;
		this.purchaseItemMapper = purchaseItemMapper;
	}

	/* == PUBLIC METHODS == */
	public IncomingInvoiceDTO toDto(IncomingInvoiceEntity entity) {
		return toDto(new IncomingInvoiceDTO(), entity);
	}

	public List<IncomingInvoiceDTO> toDto(List<IncomingInvoiceEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

	public Page<IncomingInvoiceDTO> toDto(Page<IncomingInvoiceEntity> entities) {
		return entities.map(this::toDto);
	}

	public IncomingInvoiceProductsDTO toProductsDto(IncomingInvoiceEntity entity) {
		IncomingInvoiceProductsDTO dto = toDto(new IncomingInvoiceProductsDTO(), entity);
		dto.setItems(entity.getItems().stream().map(purchaseItemMapper::toDto).toList());
		return dto;
	}

	public IncomingInvoiceEntity toEntity(CreateIncomingInvoiceDTO dto, SupplierEntity supplier) {
		return toEntity(new IncomingInvoiceEntity(), dto, supplier);
	}

	public IncomingInvoiceEntity toEntity(IncomingInvoiceEntity entity, CreateIncomingInvoiceDTO dto, SupplierEntity supplier) {
		entity.setSupplier(supplier);
		entity.setDate(dto.getDate());
		entity.setNumber(dto.getNumber());
		entity.setAmount(dto.getAmount());
		entity.setNotes(dto.getNotes());
		return entity;
	}

	/* == PRIVATE METHODS == */
	private <T extends IncomingInvoiceDTO> T toDto(T dto, IncomingInvoiceEntity entity) {
		dto.setId(entity.getId());
		dto.setSupplier(supplierMapper.toDto(entity.getSupplier()));
		dto.setDate(entity.getDate());
		dto.setNumber(entity.getNumber());
		dto.setAmount(entity.getAmount());
		dto.setNotes(entity.getNotes());
		dto.setDaysLeft(entity.getDaysLeft());
		return dto;
	}

}
