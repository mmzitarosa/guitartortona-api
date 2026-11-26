package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ultimo numero di ricevuta assegnato per anno. Non e' un IdEntity: non finisce nel registro di audit. */
@Entity @Table(name = "used_receipt_counter")
@Getter @Setter @NoArgsConstructor
public class UsedReceiptCounterEntity {

	@Id private Integer year;
	@Column(name = "last_number") private int lastNumber;

}
