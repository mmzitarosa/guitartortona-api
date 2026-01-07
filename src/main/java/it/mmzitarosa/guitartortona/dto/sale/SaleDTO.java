package it.mmzitarosa.guitartortona.dto.sale;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.entity.SaleEntity;

import java.time.LocalDate;

public record SaleDTO(
		Long id,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		Integer quantity,
		Double vat,
		String receiptNumber,
		Double salePrice
) {
	public static SaleDTO of(SaleEntity entity) {
		return new SaleDTO(
				entity.getId(),
				entity.getDate(),
				entity.getQuantity(),
				entity.getVat(),
				entity.getReceiptNumber(),
				entity.getSalePrice()
		);
	}
}
