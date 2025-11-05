package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class CreateUsedReceiptDTO {
	@NotNull(message = "Date is mandatory")
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;		// Data della ricevuta, già formatta: "22/09/1996"
	@Size(max = 255, message = "Notes must be at most 255 characters")
	private String notes;		// Eventuali note
}