package it.mmzitarosa.guitartortona.dto.purchase;

import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;

public record PurchaseItemDTO(
		Long id,
		PurchaseSummaryDTO purchase,
		Integer quantity,
		Double vat,
		Double purchasePrice
) {
	public static PurchaseItemDTO of(PurchaseItemEntity entity) {
		return new PurchaseItemDTO(
				entity.getId(),
				PurchaseSummaryDTO.of(entity.getPurchase()),
				entity.getQuantity(),
				entity.getVat(),
				entity.getPurchasePrice()
		);
	}
}
