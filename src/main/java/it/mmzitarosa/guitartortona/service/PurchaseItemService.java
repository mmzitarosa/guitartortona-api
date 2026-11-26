package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.exception.ConflictException;

import it.mmzitarosa.guitartortona.dto.purchase.ProductPurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.ProductIncomingInvoiceInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.ProductUsedReceiptInput;
import it.mmzitarosa.guitartortona.dto.purchase.usedreceipt.UsedReceiptItemDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
import it.mmzitarosa.guitartortona.repository.SaleRepository;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Transactional
public class PurchaseItemService {

	/* == CONSTRUCTOR ARGS == */
	private final PurchaseItemRepository repository;
	private final ProductService productService;
	private final IncomingInvoiceService incomingInvoiceService;
	private final UsedReceiptService usedReceiptService;
	private final SaleRepository saleRepository;

	/**
	 * POST /incomingInvoice/{id}/product
	 */
	public ProductPurchaseItemDTO addProductToIncomingInvoice(Long incomingInvoiceId, ProductIncomingInvoiceInput input) {
		IncomingInvoiceEntity incomingInvoice = incomingInvoiceService.getEditableIncomingInvoice(incomingInvoiceId);

		// Trova o crea prodotto
		ProductEntity product = productService.findOrCreateProduct(input.product(), ProductCondition.NEW);

		// Verifica che non esista già questo prodotto nella fattura
		if (repository.existsByPurchaseAndProduct(
				incomingInvoiceId, product.getId())) {
			throw new ConflictException("PRODUCT_ALREADY_IN_INVOICE", "Product already exists in this invoice");
		}

		// Crea purchase item
		PurchaseItemEntity item = new PurchaseItemEntity();
		item.setPurchase(incomingInvoice);
		item.setProduct(product);
		item.setVat(input.vat());
		item.setPurchasePrice(input.purchasePrice());
		item.setQuantity(input.quantity());

		item = repository.save(item);

		return ProductPurchaseItemDTO.of(item);
	}

	/**
	 * PUT /incomingInvoice/{id}/product/{productId}
	 */
	public ProductPurchaseItemDTO updateProductInIncomingInvoice(Long incomingInvoiceId, Long purchaseItemId, ProductIncomingInvoiceInput dto) {
		incomingInvoiceService.getEditableIncomingInvoice(incomingInvoiceId);
		PurchaseItemEntity item = getPurchaseItem(purchaseItemId);

		// Verifica che l'item appartenga a questa fattura
		if (!item.getPurchase().getId().equals(incomingInvoiceId)) {
			throw new ConflictException("PURCHASE_ITEM_NOT_IN_INVOICE", "Purchase item does not belong to this invoice");
		}

		ProductEntity product = productService.updateProduct(item.getProduct(), dto.product());

		item.setProduct(product);
		item.setVat(dto.vat());
		item.setPurchasePrice(dto.purchasePrice());
		item.setQuantity(dto.quantity());
		repository.save(item);

		return ProductPurchaseItemDTO.of(item);
	}

	/**
	 * DELETE /incomingInvoice/{id}/product/{productId}
	 */
	public void deleteProductFromIncomingInvoice(Long incomingInvoiceId, Long purchaseItemId) {
		incomingInvoiceService.getEditableIncomingInvoice(incomingInvoiceId);
		PurchaseItemEntity item = getPurchaseItem(purchaseItemId);

		// Verifica che l'item appartenga a questa fattura
		if (!item.getPurchase().getId().equals(incomingInvoiceId)) {
			throw new ConflictException("PURCHASE_ITEM_NOT_IN_INVOICE", "Purchase item does not belong to this invoice");
		}

		repository.deleteById(purchaseItemId);
	}

	/**
	 * POST /usedReceipt/{id}/product
	 * Quantità sempre 1, IVA facoltativa (default 0). Su una ricevuta già confermata (solo admin) la riga prende subito la lettera successiva.
	 */
	public UsedReceiptItemDTO addProductToUsedReceipt(Long usedReceiptId, ProductUsedReceiptInput input) {
		UsedReceiptEntity receipt = usedReceiptService.getEditableUsedReceipt(usedReceiptId);

		PurchaseItemEntity item = new PurchaseItemEntity();
		item.setPurchase(receipt);
		item.setProduct(productService.createUsedProduct(input.product()));
		item.setVat(input.vat() == null ? 0.0 : input.vat());
		item.setPurchasePrice(input.purchasePrice());
		item.setQuantity(1);
		if (!receipt.isDraft())
			usedReceiptService.assignLetter(receipt, item);
		item = repository.save(item);
		usedReceiptService.recalculateAmount(receipt);

		return UsedReceiptItemDTO.of(item);
	}

	/**
	 * PUT /usedReceipt/{id}/product/{purchaseItemId}
	 */
	public UsedReceiptItemDTO updateProductInUsedReceipt(Long usedReceiptId, Long purchaseItemId, ProductUsedReceiptInput input) {
		UsedReceiptEntity receipt = usedReceiptService.getEditableUsedReceipt(usedReceiptId);
		PurchaseItemEntity item = getUsedReceiptItem(receipt, purchaseItemId);

		item.setProduct(productService.updateUsedProduct(item.getProduct(), input.product()));
		item.setVat(input.vat() == null ? 0.0 : input.vat());
		item.setPurchasePrice(input.purchasePrice());
		repository.save(item);
		usedReceiptService.recalculateAmount(receipt);

		return UsedReceiptItemDTO.of(item);
	}

	/**
	 * DELETE /usedReceipt/{id}/product/{purchaseItemId}
	 * La lettera della riga rimossa non viene riassegnata. Il prodotto usato viene eliminato dal trigger sul DB.
	 */
	public void deleteProductFromUsedReceipt(Long usedReceiptId, Long purchaseItemId) {
		UsedReceiptEntity receipt = usedReceiptService.getEditableUsedReceipt(usedReceiptId);
		PurchaseItemEntity item = getUsedReceiptItem(receipt, purchaseItemId);

		if (saleRepository.existsByProductId(item.getProduct().getId()))
			throw new ConflictException("PRODUCT_ALREADY_SOLD", "Product has already been sold");

		receipt.getItems().remove(item);
		repository.delete(item);
		repository.flush();
		usedReceiptService.recalculateAmount(receipt);
	}

	private PurchaseItemEntity getUsedReceiptItem(UsedReceiptEntity receipt, Long purchaseItemId) {
		PurchaseItemEntity item = getPurchaseItem(purchaseItemId);
		if (!item.getPurchase().getId().equals(receipt.getId()))
			throw new ConflictException("PURCHASE_ITEM_NOT_IN_RECEIPT", "Purchase item does not belong to this receipt");
		return item;
	}

	/**
	 * Helper: Recupera purchaseItem
	 */
	protected PurchaseItemEntity getPurchaseItem(long id) {
		return repository.findById(id).orElseThrow(() -> new NotFoundException("PURCHASE_ITEM_NOT_FOUND", "Purchase item not found: " + id));
	}

}