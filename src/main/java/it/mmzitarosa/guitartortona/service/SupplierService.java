package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.supplier.SupplierDTO;
import it.mmzitarosa.guitartortona.dto.supplier.SupplierInput;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service @Transactional(readOnly = true)
public class SupplierService {

	private final SupplierRepository repository;

	/**
	 * GET /suppliers
	 */
	public List<SupplierDTO> getAllSuppliers() {
		return repository.findAll().stream()
				.map(SupplierDTO::of)
				.toList();
	}

	/**
	 * Helper: Trova o crea supplier
	 */
	@Transactional protected SupplierEntity findOrCreateSupplier(SupplierInput supplier) {
		if (supplier.id() != null) {
			return repository.findById(supplier.id())
					.orElseThrow(() -> new RuntimeException("Supplier not found: " + supplier.id()));
		}

		// Cerca per nome
		return repository.findByNameIgnoreCase(supplier.name())
				.orElseGet(() -> {
					SupplierEntity newSupplier = new SupplierEntity();
					newSupplier.setName(supplier.name());
					return repository.save(newSupplier);
				});
	}

}
