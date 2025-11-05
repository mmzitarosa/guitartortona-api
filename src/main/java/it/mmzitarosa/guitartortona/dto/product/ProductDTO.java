package it.mmzitarosa.guitartortona.dto.product;

import it.mmzitarosa.guitartortona.dto.BrandDTO;
import it.mmzitarosa.guitartortona.dto.CategoryDTO;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class ProductDTO {

	private Long id;					// Id del prodotto
	private String code;				// SKU
	private String internalCode;		// Codice Interno
	private CategoryDTO category;		// Categoria
	private BrandDTO brand;				// Marca
	private String description;			// Descrizione
	private ProductCondition condition;	// Nuovo/Usato
	private Double price;				// Prezzo di vendita
	private Integer reorderPoint;		// Soglia quantità sotto al quale allertare
	private String notes;				// Eventuali note

}
