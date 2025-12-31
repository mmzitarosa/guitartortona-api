package it.mmzitarosa.guitartortona.dto.purchase;

import it.mmzitarosa.guitartortona.dto.product.ProductSummaryDTO;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;

public record ProductPurchaseItemDTO(
		Long id,
		ProductSummaryDTO product,
		Integer quantity,
		Double vat,
		Double purchasePrice
) {
	public static ProductPurchaseItemDTO of(PurchaseItemEntity entity) {
		return new ProductPurchaseItemDTO(
				entity.getId(),
				ProductSummaryDTO.of(entity.getProduct()),
				entity.getQuantity(),
				entity.getVat(),
				entity.getPurchasePrice()
		);
	}
}
