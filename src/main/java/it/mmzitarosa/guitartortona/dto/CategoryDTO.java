package it.mmzitarosa.guitartortona.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter public class CategoryDTO implements IdName {
	
	private Long id;					// Id categoria
	@NotEmpty(message = "Category name is mandatory")
	@Size(max = 50, message = "Category name must be at most 50 characters")
	private String name;				// Nome categoria
	private List<CategoryDTO> subCategories;	// Lista delle categorie figlie

}
