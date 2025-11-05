package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter @Setter
@Table(name = "purchase") @ToString(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PurchaseEntity extends StatusEntity {

	private LocalDate date;
	private Double amount;
	private String notes;

	@ToString.Exclude @OneToMany(mappedBy = "purchase"/*, cascade = CascadeType.ALL, orphanRemoval = true*/)
	private List<PurchaseItemEntity> items = new ArrayList<>();

	//TODO: Aggiungere documento allegato

}
