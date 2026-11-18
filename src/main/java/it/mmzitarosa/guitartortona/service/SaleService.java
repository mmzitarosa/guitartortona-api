package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.exception.ConflictException;

import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.sale.CartSaleInput;
import it.mmzitarosa.guitartortona.dto.sale.SaleInput;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.repository.SaleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service @Transactional
public class SaleService {

	private final SaleRepository repository;
	private final ProductService productService;

	private final EntityManager entityManager;

	/**
	 * POST /product/{id}/sale
	 */
	public ProductDetailDTO addProductSale(long productId, SaleInput input) {
		ProductEntity product = productService.getProduct(productId);

		// Verifica disponibilità prodotto
		if (product.getStock() < input.quantity()) {
			throw new ConflictException("PRODUCT_STOCK_NOT_ENOUGH", "Product stock is not enough: " + product.getStock());
		}

		// Crea purchase
		SaleEntity sale = new SaleEntity();
		sale.setProduct(product);
		sale.setDate(input.date());
		sale.setQuantity(input.quantity());
		sale.setVat(input.vat());
		sale.setSalePrice(input.salePrice());
		sale.setReceiptNumber(input.receiptNumber());
		sale.setNotes(input.notes());
		repository.save(sale);

		entityManager.flush();
		entityManager.clear();

		return productService.getProductDetail(productId);
	}

	/**
	 * POST /sales
	 * Registra in un'unica transazione le vendite di un carrello: se un prodotto non ha giacenza sufficiente non viene salvato nulla.
	 */
	public void addCartSale(CartSaleInput input) {
		// La giacenza è verificata sul totale richiesto per prodotto
		Map<Long, Integer> requested = new HashMap<>();
		input.items().forEach(item -> requested.merge(item.productId(), item.quantity(), Integer::sum));

		Map<Long, ProductEntity> products = new HashMap<>();
		requested.forEach((productId, quantity) -> {
			ProductEntity product = productService.getProduct(productId);
			if (product.getStock() < quantity) {
				throw new ConflictException("PRODUCT_STOCK_NOT_ENOUGH", "Product stock is not enough: " + product.getStock());
			}
			products.put(productId, product);
		});

		input.items().forEach(item -> {
			SaleEntity sale = new SaleEntity();
			sale.setProduct(products.get(item.productId()));
			sale.setDate(input.date());
			sale.setReceiptNumber(input.receiptNumber());
			sale.setQuantity(item.quantity());
			sale.setVat(item.vat());
			sale.setSalePrice(item.salePrice());
			sale.setNotes(item.notes());
			repository.save(sale);
		});
	}

}
