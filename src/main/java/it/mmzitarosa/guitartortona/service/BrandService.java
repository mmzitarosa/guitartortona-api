package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.brand.BrandDTO;
import it.mmzitarosa.guitartortona.dto.brand.BrandInput;
import it.mmzitarosa.guitartortona.entity.BrandEntity;
import it.mmzitarosa.guitartortona.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service @Transactional(readOnly = true)
public class BrandService {

	private final BrandRepository repository;

	/**
	 * GET /brands
	 */
	public List<BrandDTO> getAllBrands() {
		return repository.findAll().stream()
				.map(BrandDTO::of)
				.toList();
	}

	/**
	 * Helper: Trova o crea brand
	 */
	@Transactional protected BrandEntity findOrCreateBrand(BrandInput brand) {
		if (brand.id() != null) {
			return repository.findById(brand.id())
					.orElseThrow(() -> new RuntimeException("Brand not found: " + brand.id()));
		}

		// Cerca per nome
		return repository.findByNameIgnoreCase(brand.name())
				.orElseGet(() -> {
					BrandEntity newBrand = new BrandEntity();
					newBrand.setName(brand.name());
					return repository.save(newBrand);
				});
	}

}
