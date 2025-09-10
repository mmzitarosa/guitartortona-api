package it.mmzitarosa.guitartortona.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class BankDTO {
	
	private Long id;
	@NotEmpty(message = "Bank name is mandatory")
	@Size(max = 50, message = "Bank name must be at most 50 characters")
	private String name;
	
}
