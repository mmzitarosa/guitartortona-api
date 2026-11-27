package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.product.ProductDetailDTO;
import it.mmzitarosa.guitartortona.dto.stock.StockAdjustmentInput;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.StockAdjustmentEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.repository.StockAdjustmentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service @Transactional
public class StockAdjustmentService {

	private final StockAdjustmentRepository repository;
	private final ProductService productService;
	private final EntityManager entityManager;

	/**
	 * POST /product/{id}/stockAdjustment
	 * Registra la rettifica come movimento: la giacenza è aggiornata dal trigger, non modificata direttamente.
	 * La giacenza in attesa (fatture e ricevute non ancora confermate) non viene toccata.
	 */
	public ProductDetailDTO adjustStock(long productId, StockAdjustmentInput input) {
		ProductEntity product = productService.getProduct(productId);

		int current = product.getStock();
		if (input.countedStock() == current) {
			throw new ConflictException("STOCK_ADJUSTMENT_NO_CHANGE", "Counted stock equals current stock: " + current);
		}

		StockAdjustmentEntity adjustment = new StockAdjustmentEntity();
		adjustment.setProduct(product);
		adjustment.setPreviousStock(current);
		adjustment.setCountedStock(input.countedStock());
		adjustment.setQuantity(input.countedStock() - current);
		adjustment.setReason(input.reason().trim());
		repository.save(adjustment);

		entityManager.flush();
		entityManager.clear();

		return productService.getProductDetail(productId);
	}

}
