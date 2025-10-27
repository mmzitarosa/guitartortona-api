package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity @Getter @Setter
@Table(name = "supplier") @ToString
@NoArgsConstructor @RequiredArgsConstructor
public class SupplierEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@NonNull private String name;
	// private Bank bank;

	@ToString.Exclude @OneToMany(mappedBy = "supplier") private List<IncomingInvoiceEntity> incomingInvoices;

}
