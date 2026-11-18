package it.mmzitarosa.guitartortona.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Evento della storia di un prodotto: acquisto (riga di fattura) o vendita.
 * Per gli acquisti {@code price} è il costo unitario imponibile, per le vendite il prezzo unitario di vendita.
 */
public record ProductEventDTO(
		String type,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		Integer quantity,
		Double price,
		Double vat,
		String notes,
		Long invoiceId,
		String invoiceNumber,
		String supplier,
		String status
) {
	public static final String PURCHASE = "PURCHASE";
	public static final String SALE = "SALE";
}
