package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.BrandDTO;
import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.mapper.BrandMapper;
import it.mmzitarosa.guitartortona.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class BrandService {

	/* == CONSTANTS == */
	private final BrandRepository repository;
	private final BrandMapper mapper;

	/* == CONSTRUCTOR == */
	public BrandService(BrandRepository repository, BrandMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	/* == PUBLIC METHODS == */
	public List<BrandDTO> readBrands() {
		return mapper.toDto(repository.findAll());
	}

	/* == PACKAGE METHODS == */
	BrandEntity getOrInsertBrand(Long brandId, String brandName) {
		// Se ho l'id del fornitore, lo recupero da DB
		// Altrimenti cerco fornitore per nome (evito duplicati) ed eventualmente, se non esistente, lo credo
		BrandEntity brand;
		if (brandId != null) {
			brand = repository.findById(brandId).orElseThrow(() -> new IllegalArgumentException("Brand not found"));
		} else if (brandName != null && !brandName.isEmpty()) {
			brand = repository.findByNameIgnoreCase(brandName).orElseGet(() -> repository.save(new BrandEntity(brandName)));
		} else {
			throw new IllegalArgumentException("Brand id or name must be provided");
		}

		return brand;
	}

}
