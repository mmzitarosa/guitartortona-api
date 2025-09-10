package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.incominginvoice.IncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.text.SimpleDateFormat;
import java.util.Date;

@Mapper(componentModel = "spring", imports = {SimpleDateFormat.class})
 public interface IncomingInvoiceMapper {

	@Mapping(target = "date", source = "date", dateFormat = "dd/MM/yyyy")
	IncomingInvoiceDTO toDTO(IncomingInvoiceEntity entity);
	
	@Mapping(target = "date", expression = "java(parseDate(dto.getDate()))")
	IncomingInvoiceEntity toEntity(CreateIncomingInvoiceDTO dto);

	@Mapping(target = "date", expression = "java(parseDate(dto.getDate()))")
	IncomingInvoiceEntity toEntity(IncomingInvoiceDTO dto);

	default Date parseDate(String date) {
		try {
			return new SimpleDateFormat("dd/MM/yyyy").parse(date);
		} catch (Exception e) {
			throw new RuntimeException("Invalid date format: " + date);
		}
	}

}
