package it.mmzitarosa.guitartortona.dto.purchase;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.entity.PurchaseEntity;

import java.time.LocalDate;

public record PurchaseSummaryDTO(
		Long id,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date
) {
	public static PurchaseSummaryDTO of(PurchaseEntity entity) {
		return new PurchaseSummaryDTO(entity.getId(), entity.getDate());
	}

}
