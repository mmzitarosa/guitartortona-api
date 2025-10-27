package it.mmzitarosa.guitartortona.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class SupplierDTO implements IdName {
	
	private Long id;
	@NotEmpty(message = "Supplier name is mandatory")
	@Size(max = 150, message = "Supplier name must be at most 150 characters")
	private String name;

}
