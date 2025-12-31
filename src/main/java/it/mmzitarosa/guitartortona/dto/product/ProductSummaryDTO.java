package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.entity.ProductEntity;

public record ProductSummaryDTO(
		Long id,
		String code,
		String internalCode,
		Long brandId,
		Long categoryId,
		String description,
		Double price,
		String notes
		) {
	public static ProductSummaryDTO of(ProductEntity entity) {
		return new ProductSummaryDTO(
				entity.getId(),
				entity.getCode(),
				entity.getInternalCode(),
				entity.getBrand().getId(),
				entity.getCategory().getId(),
				entity.getDescription(),
				entity.getPrice(),
				entity.getNotes()
		);
	}
}
