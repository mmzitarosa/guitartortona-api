package it.mmzitarosa.guitartortona.dto.category;

import it.mmzitarosa.guitartortona.entity.CategoryEntity;

import java.util.List;

public record CategoryDTO(Long id,
						  String name,
						  List<CategoryDTO> subCategories) {

	public static CategoryDTO of(CategoryEntity entity) {
		return new CategoryDTO(
				entity.getId(),
				entity.getName(),
				entity.getSubCategories() != null
						? entity.getSubCategories().stream().map(CategoryDTO::of).toList()
						: null
		);

	}
}
