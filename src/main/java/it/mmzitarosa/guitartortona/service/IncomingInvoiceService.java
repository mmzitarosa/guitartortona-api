package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.incominginvoice.IncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.entity.SupplierEntity;
import it.mmzitarosa.guitartortona.mapper.IncomingInvoiceMapper;
import it.mmzitarosa.guitartortona.repository.IncomingInvoiceRepository;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service public class IncomingInvoiceService {

	private final IncomingInvoiceRepository incomingInvoiceRepository;
	private final SupplierRepository supplierRepository;
	private final IncomingInvoiceMapper incomingInvoiceMapper;

	public IncomingInvoiceService(IncomingInvoiceRepository incomingInvoiceRepository, 
								  SupplierRepository supplierRepository, 
								  IncomingInvoiceMapper incomingInvoiceMapper) {
		this.incomingInvoiceRepository = incomingInvoiceRepository;
		this.supplierRepository = supplierRepository;
		this.incomingInvoiceMapper = incomingInvoiceMapper;
	}
	
	public IncomingInvoiceDTO readIncomingInvoiceById(long id) {
		return incomingInvoiceMapper.toDTO(incomingInvoiceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("IncomingInvoice not found")));
	}

	@Transactional public IncomingInvoiceDTO createIncomingInvoice(CreateIncomingInvoiceDTO incomingInvoiceDTO) {

		SupplierEntity supplier;
		if (incomingInvoiceDTO.getSupplierId() != null) {
			supplier = supplierRepository.findById(incomingInvoiceDTO.getSupplierId()).orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
		} else if (incomingInvoiceDTO.getSupplierName() != null  && !incomingInvoiceDTO.getSupplierName().isEmpty()) {
			supplier = supplierRepository.findByNameIgnoreCase(incomingInvoiceDTO.getSupplierName()).orElseGet(() -> {
				SupplierEntity s = new SupplierEntity(incomingInvoiceDTO.getSupplierName());
				return supplierRepository.save(s);
			});
		} else {
			throw new IllegalArgumentException("Supplier id or name must be provided");
		}

		IncomingInvoiceEntity incomingInvoice = incomingInvoiceMapper.toEntity(incomingInvoiceDTO);
		incomingInvoice.setSupplier(supplier);
		incomingInvoice = incomingInvoiceRepository.save(incomingInvoice);
		return incomingInvoiceMapper.toDTO(incomingInvoice);
	}

}
