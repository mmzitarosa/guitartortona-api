package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.dto.brand.BrandInput;
import it.mmzitarosa.guitartortona.dto.category.CategoryInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductInput(
		Long id,
		@Size(max = 50, message = "Code must be at most 50 characters") String code,
		@NotNull(message = "Category is mandatory") CategoryInput category,
		BrandInput brand,
		@NotNull(message = "Description is mandatory")
		@Size(max = 255, message = "Description must be at most 255 characters") String description,
		@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero") Double price,
		Integer reorderPoint,
		@Size(max = 255, message = "Notes must be at most 255 characters") String notes) {
}