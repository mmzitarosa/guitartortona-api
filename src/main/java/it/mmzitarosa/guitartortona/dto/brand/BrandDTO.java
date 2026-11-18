package it.mmzitarosa.guitartortona.dto.brand;

import it.mmzitarosa.guitartortona.entity.BrandEntity;

public record BrandDTO(Long id,
					   String name) {
	public static BrandDTO of(BrandEntity entity) {
		if (entity == null) return null;
		return new BrandDTO(entity.getId(), entity.getName());
	}
}
