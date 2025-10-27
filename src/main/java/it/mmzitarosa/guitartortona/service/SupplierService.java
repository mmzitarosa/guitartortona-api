package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.mapper.SupplierMapper;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class SupplierService {

	/* == CONSTANTS == */
	private final SupplierRepository repository;
	private final SupplierMapper mapper;

	/* == CONSTRUCTOR == */
	public SupplierService(SupplierRepository repository, SupplierMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	/* == PUBLIC METHODS == */
	public List<SupplierDTO> readSuppliers() {
		return mapper.toDto(repository.findAll());
	}

	/* == PACKAGE METHODS == */
	SupplierEntity getOrInsertSupplier(Long supplierId, String supplierName) {
		// Se ho l'id del fornitore, lo recupero da DB
		// Altrimenti cerco fornitore per nome (evito duplicati) ed eventualmente, se non esistente, lo credo
		SupplierEntity supplier;
		if (supplierId != null) {
			supplier = repository.findById(supplierId).orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
		} else if (supplierName != null && !supplierName.isEmpty()) {
			supplier = repository.findByNameIgnoreCase(supplierName).orElseGet(() -> repository.save(new SupplierEntity(supplierName)));
		} else {
			throw new IllegalArgumentException("Supplier id or name must be provided");
		}

		return supplier;
	}

}
