package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.AddIncomingInvoiceProductDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.UpdateIncomingInvoiceProductDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.AddUsedReceiptProductDTO;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UpdateUsedReceiptProductDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.mapper.PurchaseItemMapper;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.utils.Constant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service public class PurchaseItemService {

	/* == CONSTANTS == */
	private final PurchaseItemRepository repository;
	private final PurchaseItemMapper mapper;
	private final ProductService productService;
	private final IncomingInvoiceService incomingInvoiceService;
	private final UsedReceiptService usedReceiptService;

	/* == CONSTRUCTOR == */
	public PurchaseItemService(PurchaseItemRepository repository, PurchaseItemMapper mapper, ProductService productService, IncomingInvoiceService incomingInvoiceService, UsedReceiptService usedReceiptService) {
		this.repository = repository;
		this.mapper = mapper;
		this.productService = productService;
		this.incomingInvoiceService = incomingInvoiceService;
		this.usedReceiptService = usedReceiptService;
	}

	/* == PUBLIC METHODS == */
	public PurchaseItemDTO addIncomingInvoiceProduct(long id, AddIncomingInvoiceProductDTO dto) {
		IncomingInvoiceEntity incomingInvoice = incomingInvoiceService.getIncomingInvoice(id);
		PurchaseItemEntity item = mapper.toEntity(dto, incomingInvoice, productService.getProduct(dto.getProductId()));
		item.setStatus(Constant.Status.DRAFT);
		item = repository.save(item);
		return mapper.toDto(item);
	}

	public PurchaseItemDTO addUsedReceiptProduct(long id, AddUsedReceiptProductDTO dto) {
		UsedReceiptEntity usedReceipt = usedReceiptService.getUsedReceipt(id);
		PurchaseItemEntity item = mapper.toEntity(dto, usedReceipt, productService.createProduct(dto.getProduct(), Constant.ProductCondition.USED));
		item.setStatus(Constant.Status.DRAFT);
		item = repository.save(item);
		return mapper.toDto(item);
	}

	public List<PurchaseItemDTO> listItemsByPurchaseId(long purchaseId) {
		return repository.findByPurchaseId(purchaseId).stream().map(mapper::toDto).collect(Collectors.toList());
	}

	public List<PurchaseItemDTO> listItemsByProductId(long productId) {
		return repository.findByProductId(productId).stream().map(mapper::toDto).collect(Collectors.toList());
	}

	public PurchaseItemDTO updateIncomingInvoiceProduct(long id, UpdateIncomingInvoiceProductDTO dto) {
		PurchaseItemEntity item = getPurchaseItem(id);
		// Converto il DTO in oggetto
		mapper.toEntity(item, dto);
		// Salvo item su DB
		item = repository.save(item);
		// Converto l'entity dell'item in DTO
		return mapper.toDto(item);
	}

	public PurchaseItemDTO updateUsedReceiptProduct(long id, UpdateUsedReceiptProductDTO dto) {
		PurchaseItemEntity item = getPurchaseItem(id);
		// Converto il DTO in oggetto
		mapper.toEntity(item, dto);
		// Salvo item su DB
		item = repository.save(item);
		// Converto l'entity dell'item in DTO
		return mapper.toDto(item);
	}

	public void deletePurchaseItem(long itemId) {
		repository.deleteById(itemId);
	}

	/* == PACKAGE METHODS == */
	PurchaseItemEntity getPurchaseItem(long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("PurchaseItem not found"));
	}

}