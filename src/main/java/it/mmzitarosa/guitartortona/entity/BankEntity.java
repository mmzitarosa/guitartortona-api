package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity @Getter @Setter 
@Table(name = "bank") @ToString
public class BankEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	private String name;

	@ToString.Exclude @OneToMany(mappedBy = "bank") private List<LedgerEntryEntity> ledgerEntries;

}
