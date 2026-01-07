package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;

public record ProductListDTO(
		Long id,
		Long brandId,
		Long categoryId,
		String description,
		ProductCondition condition,
		Double price,
		Integer stock,
		Integer stockPending,
		boolean available
) {
	public static ProductListDTO of(ProductEntity entity) {
		return new ProductListDTO(
				entity.getId(),
				entity.getBrand().getId(),
				entity.getCategory().getId(),
				entity.getDescription(),
				entity.getCondition(),
				entity.getPrice(),
				entity.getStock(),
				entity.getStockPending(),
				entity.getStock() > 0 || entity.getStockPending() > 0
		);
	}
}
