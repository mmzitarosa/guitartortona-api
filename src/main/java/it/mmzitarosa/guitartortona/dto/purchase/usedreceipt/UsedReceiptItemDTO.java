package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import it.mmzitarosa.guitartortona.dto.product.ProductSummaryDTO;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import org.hibernate.Hibernate;

public record UsedReceiptItemDTO(
		Long id,
		ProductSummaryDTO product,
		Double vat,
		Double purchasePrice,
		String letter,
		String reference
) {
	public static UsedReceiptItemDTO of(PurchaseItemEntity entity) {
		UsedReceiptEntity receipt = (UsedReceiptEntity) Hibernate.unproxy(entity.getPurchase());
		String reference = receipt.getItemReference(entity.getLetter());
		return new UsedReceiptItemDTO(entity.getId(), ProductSummaryDTO.of(entity.getProduct()), entity.getVat(), entity.getPurchasePrice(),
				entity.getLetter(), reference);
	}
}
