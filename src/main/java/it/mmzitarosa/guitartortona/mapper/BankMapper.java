package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class BankMapper {

	public BankDTO toDto(BankEntity entity) {
		BankDTO dto = new BankDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		return dto;
	}

	public List<BankDTO> toDto(List<BankEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

	public BankEntity toEntity(BankDTO dto) {
		BankEntity entity = new BankEntity();
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		return entity;
	}

}
