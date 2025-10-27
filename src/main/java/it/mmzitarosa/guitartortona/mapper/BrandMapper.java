package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.BrandDTO;
import it.mmzitarosa.guitartortona.entity.BrandEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class BrandMapper {

	/* == PUBLIC METHODS == */
	public BrandDTO toDto(BrandEntity entity) {
		BrandDTO dto = new BrandDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		return dto;
	}

	public List<BrandDTO> toDto(List<BrandEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

}
