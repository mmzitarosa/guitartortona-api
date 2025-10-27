package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class SupplierMapper {

	/* == PUBLIC METHODS == */
	public SupplierDTO toDto(SupplierEntity entity) {
		SupplierDTO dto = new SupplierDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		return dto;
	}

	public List<SupplierDTO> toDto(List<SupplierEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

}
