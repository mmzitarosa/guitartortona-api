package it.mmzitarosa.guitartortona.dto.supplier;

import jakarta.validation.constraints.Size;


public record SupplierInput(Long id,
						  @Size(max = 150, message = "Supplier name must be at most 150 characters") String name) {
}