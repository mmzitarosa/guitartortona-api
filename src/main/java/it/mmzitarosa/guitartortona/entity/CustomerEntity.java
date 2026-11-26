package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Anagrafica del venditore di materiale usato. Dati personali: visibili solo a user/admin. */
@Entity @Table(name = "customer")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerEntity extends DateEntity {

	@Column(name = "first_name") private String firstName;
	@Column(name = "last_name") private String lastName;
	private String address;
	@Column(name = "zip_code") private String zipCode;
	private String city;
	private String province;
	@Column(name = "document_type") private String documentType;
	@Column(name = "document_number") private String documentNumber;
	@Column(name = "tax_code") private String taxCode;

}
