package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.text.ParseException;
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
