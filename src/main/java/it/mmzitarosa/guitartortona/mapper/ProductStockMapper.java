package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.product.ProductLightDTO;
import it.mmzitarosa.guitartortona.entity.view.ProductStockView;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class ProductStockMapper {

	/* == PUBLIC METHODS == */
	public ProductLightDTO toDto(ProductStockView view) {
		ProductLightDTO dto = new ProductLightDTO();
		dto.setId(view.getId());
		dto.setCategoryId(view.getCategoryId());
		dto.setBrandId(view.getBrandId());
		dto.setDescription(view.getDescription());
		dto.setCondition(view.getCondition());
		dto.setPrice(view.getPrice());
		dto.setQuantity(view.getAvailableStock()); //TODO Quantity

		return dto;
	}

	public List<ProductLightDTO> toDto(List<ProductStockView> views) {
		return views.stream().map(this::toDto).toList();
	}


}
