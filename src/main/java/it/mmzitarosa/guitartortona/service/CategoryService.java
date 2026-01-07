package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.category.CategoryDTO;
import it.mmzitarosa.guitartortona.entity.CategoryEntity;
import it.mmzitarosa.guitartortona.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service @Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository repository;

	private List<CategoryDTO> categories;
	private Map<Long, CategoryEntity> categoriesMap;

	/**
	 * Load categories into memory at startup
	 */
	@PostConstruct protected void loadCategories() {
		List<CategoryEntity> categories = repository.findAll();
		categoriesMap = categories.stream().collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));
		this.categories = categories.stream().filter(c -> c.getParentCategory() == null).map(CategoryDTO::of).toList();
	}

	/**
	 * GET /categories
	 */
	public List<CategoryDTO> getAllCategories() {
		return categories;
	}

	/**
	 * Helper: Recupera category
	 */
	protected CategoryEntity getCategory(long id) {
		return Optional.ofNullable(categoriesMap.get(id)).orElseThrow(() -> new RuntimeException("Category not found: " + id));
	}

}
