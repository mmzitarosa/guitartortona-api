package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.dto.brand.BrandDTO;
import it.mmzitarosa.guitartortona.dto.category.CategoryDTO;
import it.mmzitarosa.guitartortona.dto.purchase.PurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.sale.SaleDTO;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;

import java.util.List;

public record ProductDetailDTO(
		Long id,
		String code,
		String internalCode,
		BrandDTO brand,
		CategoryDTO category,
		String description,
		ProductCondition condition,
		Double price,
		String notes,
		Integer stock,
		Integer pendingStock,
		List<PurchaseItemDTO> purchases,
		List<SaleDTO> sales
) {
	public static ProductDetailDTO of(ProductEntity entity) {
		return new ProductDetailDTO(
				entity.getId(),
				entity.getCode(),
				entity.getInternalCode(),
				BrandDTO.of(entity.getBrand()),
				CategoryDTO.of(entity.getCategory()),
				entity.getDescription(),
				entity.getCondition(),
				entity.getPrice(),
				entity.getNotes(),
				entity.getStock(),
				entity.getStockPending(),
				entity.getPurchaseItems().stream().filter(pi -> !pi.getPurchase().isArchived()).map(PurchaseItemDTO::of).toList(),
				entity.getSales().stream().map(SaleDTO::of).toList()
		);
	}
}
