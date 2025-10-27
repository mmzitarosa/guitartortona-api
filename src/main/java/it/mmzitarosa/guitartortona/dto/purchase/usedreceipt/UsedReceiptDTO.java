package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class UsedReceiptDTO {

	private Long id;
	private Integer number;
	private String internalCode;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;
	private Double amount;
	private String notes;

}