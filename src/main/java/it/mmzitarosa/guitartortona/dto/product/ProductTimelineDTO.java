package it.mmzitarosa.guitartortona.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Storia di un prodotto già pronta da mostrare, dalla voce più recente. Costi, fatture e utenti sono valorizzati
 * solo per chi può vederli.
 */
public record ProductTimelineDTO(int purchased, int sold, List<Entry> entries) {

	public static final String SALE = "SALE";
	public static final String PURCHASE = "PURCHASE";
	public static final String PURCHASE_CHANGE = "PURCHASE_CHANGE";
	public static final String PRODUCT_CHANGE = "PRODUCT_CHANGE";
	public static final String PRODUCT_CREATED = "PRODUCT_CREATED";

	/**
	 * Una voce. {@code price} è il costo unitario imponibile per gli acquisti e il prezzo di vendita per le vendite;
	 * {@code previousPrice}/{@code previousVat} sono i valori prima di una modifica (solo se cambiati);
	 * {@code superseded} elenca i campi poi sovrascritti da una modifica successiva.
	 */
	@Builder
	public record Entry(
			String type,
			@JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime at,
			String users,
			Integer quantity,
			Double price,
			Double vat,
			Double previousPrice,
			Double previousVat,
			String notes,
			String status,
			Long invoiceId,
			String invoiceNumber,
			String supplier,
			Set<String> superseded,
			List<FieldChange> changes
	) {}

	public record FieldChange(String field, String oldValue, String newValue) {}

}
