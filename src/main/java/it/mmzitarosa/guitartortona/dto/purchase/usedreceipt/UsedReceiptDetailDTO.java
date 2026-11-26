package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.dto.customer.CustomerDTO;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;

import java.time.LocalDate;
import java.util.List;

public record UsedReceiptDetailDTO(
		Long id,
		String internalCode,
		String number,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		Double amount,
		String notes,
		Status status,
		boolean archived,
		CustomerDTO customer,
		List<UsedReceiptItemDTO> items
) {
	public static UsedReceiptDetailDTO of(UsedReceiptEntity entity) {
		return new UsedReceiptDetailDTO(
				entity.getId(),
				entity.getInternalCode(),
				entity.getReference(),
				entity.getDate(),
				entity.getAmount(),
				entity.getNotes(),
				entity.getStatus(),
				entity.isArchived(),
				CustomerDTO.of(entity),
				entity.getItems().stream().map(UsedReceiptItemDTO::of).toList());
	}
}
