package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.ToString.Exclude;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity @Table(name = "purchase")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PurchaseEntity extends StatusEntity {

	private LocalDate date;
	private Double amount;
	private String notes;

	@Exclude @OneToMany(mappedBy = "purchase") private List<PurchaseItemEntity> items = new ArrayList<>();

	//TODO: Aggiungere documento allegato
}
