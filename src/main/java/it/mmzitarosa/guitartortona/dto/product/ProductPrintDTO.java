package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.entity.ProductEntity;

public record ProductPrintDTO(
		String internalCode,
		String brand,
		String category,
		String description
) {
	public static ProductPrintDTO of(ProductEntity entity) {
		return new ProductPrintDTO(
				entity.getInternalCode(),
				entity.getBrand() == null ? null : entity.getBrand().getName(),
				entity.getBrand() == null ? null : entity.getCategory().getName(),
				entity.getDescription()
		);
	}
}
