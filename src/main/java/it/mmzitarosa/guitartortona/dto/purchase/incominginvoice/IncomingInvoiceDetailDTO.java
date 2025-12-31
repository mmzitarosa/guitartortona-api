package it.mmzitarosa.guitartortona.dto.purchase.incominginvoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.dto.purchase.ProductPurchaseItemDTO;
import it.mmzitarosa.guitartortona.dto.supplier.SupplierDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;

import java.time.LocalDate;
import java.util.List;

public record IncomingInvoiceDetailDTO(
		Long id,
		String number,
		SupplierDTO supplier,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		Double amount,
		String notes,
		Status status,
		List<ProductPurchaseItemDTO> items
) {
	public static IncomingInvoiceDetailDTO of(IncomingInvoiceEntity entity) {
		return new IncomingInvoiceDetailDTO(
				entity.getId(),
				entity.getNumber(),
				SupplierDTO.of(entity.getSupplier()),
				entity.getDate(),
				entity.getAmount(),
				entity.getNotes(),
				entity.getStatus(),
				entity.getItems().stream()
						.map(ProductPurchaseItemDTO::of)
						.toList());
	}
}
