package it.mmzitarosa.guitartortona.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dati del venditore. Con id valorizzato si riferisce a un cliente dell'anagrafica. */
public record CustomerInput(
		Long id,
		@NotBlank(message = "First name is mandatory") @Size(max = 100, message = "First name must be at most 100 characters") String firstName,
		@NotBlank(message = "Last name is mandatory") @Size(max = 100, message = "Last name must be at most 100 characters") String lastName,
		@NotBlank(message = "Address is mandatory") @Size(max = 255, message = "Address must be at most 255 characters") String address,
		@NotBlank(message = "Zip code is mandatory") @Size(max = 10, message = "Zip code must be at most 10 characters") String zipCode,
		@NotBlank(message = "City is mandatory") @Size(max = 100, message = "City must be at most 100 characters") String city,
		@NotBlank(message = "Province is mandatory") @Size(max = 50, message = "Province must be at most 50 characters") String province,
		@Size(max = 50, message = "Document type must be at most 50 characters") String documentType,
		@Size(max = 50, message = "Document number must be at most 50 characters") String documentNumber,
		@Size(max = 20, message = "Tax code must be at most 20 characters") String taxCode
) {
}
