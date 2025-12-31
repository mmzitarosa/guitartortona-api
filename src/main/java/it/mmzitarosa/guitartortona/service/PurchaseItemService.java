package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.purchase.ProductPurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.ProductIncomingInvoiceInput;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.repository.PurchaseItemRepository;
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

	/**
	 * POST /incomingInvoice/{id}/product
	 */
	public ProductPurchaseItemDTO addProductToIncomingInvoice(Long incomingInvoiceId, ProductIncomingInvoiceInput input) {
		IncomingInvoiceEntity incomingInvoice = incomingInvoiceService.getIncomingInvoice(incomingInvoiceId);

		// Trova o crea prodotto
		ProductEntity product = productService.findOrCreateProduct(input.product(), ProductCondition.NEW);

		// Verifica che non esista già questo prodotto nella fattura
		if (repository.existsByPurchaseAndProduct(
				incomingInvoiceId, product.getId())) {
			throw new RuntimeException("Product already exists in this invoice");
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
		PurchaseItemEntity item = getPurchaseItem(purchaseItemId);

		// Verifica che l'item appartenga a questa fattura
		if (!item.getPurchase().getId().equals(incomingInvoiceId)) {
			throw new RuntimeException("Purchase item does not belong to this invoice");
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
		PurchaseItemEntity item = getPurchaseItem(purchaseItemId);

		// Verifica che l'item appartenga a questa fattura
		if (!item.getPurchase().getId().equals(incomingInvoiceId)) {
			throw new RuntimeException("Purchase item does not belong to this invoice");
		}

		repository.deleteById(purchaseItemId);
	}

	/**
	 * Helper: Recupera purchaseItem
	 */
	protected PurchaseItemEntity getPurchaseItem(long id) {
		return repository.findById(id).orElseThrow(() -> new RuntimeException("Purchase item not found: " + id));
	}

}