package it.mmzitarosa.guitartortona.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class CreateProductDTO {

	@Size(max = 50, message = "Code must be at most 50 characters")
	private String code;				// SKU
	@NotNull(message = "Category is mandatory")
	private Long categoryId;			// Id della categoria
	private Long brandId;				// Id della marca se già esistente, altrimenti null
	@Size(max = 50, message = "Brand name must be at most 50 characters")
	private String brandName;			// Da valorizzare per creare nuova marca, alternativa al brandId
	@NotNull(message = "Description is mandatory")
	@Size(max = 255, message = "Description must be at most 255 characters")
	private String description;			// Descrizione
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
	private Double price;				// Prezzo di vendita
	private Integer reorderPoint;		// Soglia quantità sotto al quale allertare
	@Size(max = 255, message = "Notes must be at most 255 characters")
	private String notes;				// Eventuali note

}
