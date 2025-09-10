package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter 
@Table(name = "bank")
public class BankEntity {

	@Id private Long id;
	@Column(length = 50) private String name;

}
