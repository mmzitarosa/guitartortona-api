package it.mmzitarosa.guitartortona.dto.brand;

import jakarta.validation.constraints.Size;

public record BrandInput(Long id,
						 @Size(max = 50, message = "Brand name must be at most 50 characters") String name) {
}
