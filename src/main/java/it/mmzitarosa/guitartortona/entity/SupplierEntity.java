package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity @Getter @Setter
@Table(name = "supplier")
@NoArgsConstructor @RequiredArgsConstructor
public class SupplierEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@NonNull @Column(nullable = false, unique = true, length = 150) private String name; //TODO Controllo lunghezza a FE
	//private Bank bank; //TODO: Seconda Wave?

}
