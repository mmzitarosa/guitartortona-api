package it.mmzitarosa.guitartortona.mapper;

import it.mmzitarosa.guitartortona.dto.product.CreateProductDTO;
import it.mmzitarosa.guitartortona.dto.product.ProductDTO;
import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class ProductMapper {

	/* == CONSTANTS == */
	private final CategoryMapper categoryMapper;
	private final BrandMapper brandMapper;

	/* == CONSTRUCTOR == */
	public ProductMapper(CategoryMapper categoryMapper, BrandMapper brandMapper) {
		this.categoryMapper = categoryMapper;
		this.brandMapper = brandMapper;
	}

	/* == PUBLIC METHODS == */
	public ProductDTO toDto(ProductEntity entity) {
		ProductDTO dto = new ProductDTO();
		dto.setId(entity.getId());
		dto.setCode(entity.getCode());
		dto.setInternalCode(entity.getInternalCode());
		dto.setCategory(categoryMapper.toDto(entity.getCategory()));
		dto.setBrand(brandMapper.toDto(entity.getBrand()));
		dto.setDescription(entity.getDescription());
		dto.setCondition(entity.getCondition());
		dto.setPrice(entity.getPrice());
		dto.setReorderPoint(entity.getReorderPoint());
		dto.setNotes(entity.getNotes());
		return dto;
	}

	public List<ProductDTO> toDto(List<ProductEntity> entities) {
		return entities.stream().map(this::toDto).toList();
	}

	public Page<ProductDTO> toDto(Page<ProductEntity> entities) {
		return entities.map(this::toDto);
	}

	public ProductEntity toEntity(CreateProductDTO dto, CategoryEntity category, BrandEntity brand) {
		return toEntity(new ProductEntity(), dto, category, brand);
	}

	public ProductEntity toEntity(ProductEntity entity, CreateProductDTO dto, CategoryEntity category, BrandEntity brand) {
		entity.setCode(dto.getCode());
		entity.setCategory(category);
		entity.setBrand(brand);
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setReorderPoint(dto.getReorderPoint());
		entity.setNotes(dto.getNotes());
		return entity;
	}

}
