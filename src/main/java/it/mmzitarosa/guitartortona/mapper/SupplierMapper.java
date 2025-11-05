package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.dto.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.incominginvoice.IncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring") public interface SupplierMapper {
	
	SupplierDTO toDTO(SupplierEntity entity);

	SupplierEntity toEntity(SupplierDTO dto);
	
	List<SupplierDTO> toDTOList(List<SupplierEntity> entities);

}
