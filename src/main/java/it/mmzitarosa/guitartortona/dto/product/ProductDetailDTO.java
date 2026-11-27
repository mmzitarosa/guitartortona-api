package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.dto.brand.BrandDTO;
import it.mmzitarosa.guitartortona.dto.category.CategoryDTO;
import it.mmzitarosa.guitartortona.dto.sale.SaleDTO;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;

import java.util.List;

public record ProductDetailDTO(
		Long id,
		String code,
		String internalCode,
		String usedReference,
		Long usedReceiptId,
		BrandDTO brand,
		CategoryDTO category,
		String description,
		ProductCondition condition,
		Double price,
		String notes,
		Integer stock,
		Integer pendingStock,
		List<SaleDTO> sales
) {
	/**
	 * {@code usedReference}: "numero/anno lettera" del prodotto usato registrato, null altrimenti.
	 * {@code usedReceiptId}: ricevuta dell'usato da cui il prodotto proviene, null per i prodotti nuovi.
	 */
	public static ProductDetailDTO of(ProductEntity entity, String usedReference, Long usedReceiptId) {
		return new ProductDetailDTO(
				entity.getId(),
				entity.getCode(),
				entity.getInternalCode(),
				usedReference,
				usedReceiptId,
				BrandDTO.of(entity.getBrand()),
				CategoryDTO.of(entity.getCategory()),
				entity.getDescription(),
				entity.getCondition(),
				entity.getPrice(),
				entity.getNotes(),
				entity.getStock(),
				entity.getStockPending(),
				entity.getSales().stream().map(SaleDTO::of).toList()
		);
	}
}
