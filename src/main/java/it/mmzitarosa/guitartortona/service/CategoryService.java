package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.CategoryDTO;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.mapper.CategoryMapper;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service public class CategoryService {

	/* == CONSTANTS == */
	private final CategoryRepository repository;
	private final CategoryMapper mapper;

	/* == SERVICE FIELDS == */
	private List<CategoryDTO> categories;
	private Map<Long, CategoryEntity> categoriesMap;

	/* == CONSTRUCTOR == */
	public CategoryService(CategoryRepository repository, CategoryMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	/* == POST CONSTRUCTOR == */
	/*	  Load categories into memory at startup */
	@PostConstruct protected void loadCategories() {
		List<CategoryEntity> categories = repository.findAll().stream().filter(c -> c.getParentCategory() == null).toList();
		categoriesMap = categories.stream().collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));
		this.categories = mapper.toDto(categories);
	}

	/* == PUBLIC METHODS == */
	public List<CategoryDTO> readCategories() {
		return categories;
	}

	/* == PACKAGE METHODS == */
	CategoryEntity getCategory(long id) {
		return categoriesMap.get(id);
	}

}
