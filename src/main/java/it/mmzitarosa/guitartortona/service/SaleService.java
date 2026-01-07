package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.sale.SaleInput;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.repository.SaleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
			throw new RuntimeException("Product stock is not enough: " + product.getStock());
		}

		// Crea purchase
		SaleEntity sale = new SaleEntity();
		sale.setProduct(product);
		sale.setDate(input.date());
		sale.setQuantity(input.quantity());
		sale.setVat(input.vat());
		sale.setSalePrice(input.salePrice());
		sale.setNotes(input.notes());
		repository.save(sale);

		entityManager.flush();
		entityManager.clear();

		return productService.getProductDetail(productId);
	}

}