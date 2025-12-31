package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;

import java.time.LocalDate;

public record IncomingInvoiceListDTO(
		Long id,
		Long supplierId,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		String number,
		Double amount,
		Status status,
		Integer daysLeft
) {
	public static IncomingInvoiceListDTO of(IncomingInvoiceEntity entity) {
		return new IncomingInvoiceListDTO(
				entity.getId(),
				entity.getSupplier().getId(),
				entity.getDate(),
				entity.getNumber(),
				entity.getAmount(),
				entity.getStatus(),
				entity.getDaysLeft()
		);
	}
}
