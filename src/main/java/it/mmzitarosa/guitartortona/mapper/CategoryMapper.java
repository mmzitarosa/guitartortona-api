package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.CategoryDTO;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class CategoryMapper {

	/* == PUBLIC METHODS == */
	public CategoryDTO toDto(CategoryEntity entity) {
		CategoryDTO dto = new CategoryDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSubCategories(entity.getSubCategories() == null || entity.getSubCategories().isEmpty() ? null : toDto(entity.getSubCategories()));
		return dto;
	}

	public List<CategoryDTO> toDto(List<CategoryEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

}
