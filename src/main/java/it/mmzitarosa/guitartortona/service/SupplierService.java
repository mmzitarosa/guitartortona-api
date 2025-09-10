package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.mapper.SupplierMapper;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service public class SupplierService {

	private final SupplierRepository supplierRepository;
	private final SupplierMapper supplierMapper;

	public SupplierService(SupplierRepository supplierRepository,
						   SupplierMapper supplierMapper) {
		this.supplierRepository = supplierRepository;
		this.supplierMapper = supplierMapper;
	}
	
	public List<SupplierDTO> readSuppliers() {
		return supplierMapper.toDTOList(supplierRepository.findAll());
	}

}
