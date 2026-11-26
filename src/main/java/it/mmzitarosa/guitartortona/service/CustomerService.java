package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.customer.CustomerDTO;
import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.entity.CustomerEntity;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service @Transactional
public class CustomerService {

	private final CustomerRepository repository;

	/**
	 * GET /customers
	 */
	@Transactional(readOnly = true) public List<CustomerDTO> getAllCustomers() {
		return repository.findAll(Sort.by("lastName", "firstName", "id")).stream().map(CustomerDTO::of).toList();
	}

	/**
	 * Helper: anagrafica collegata ai dati inseriti in una ricevuta.
	 * Senza id crea un nuovo cliente; con id aggiorna l'anagrafica solo se richiesto (la ricevuta tiene comunque una copia dei dati).
	 */
	protected CustomerEntity resolveCustomer(CustomerInput input, boolean updateExisting) {
		if (input.id() == null)
			return repository.save(apply(new CustomerEntity(), input));

		CustomerEntity customer = repository.findById(input.id())
				.orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found: " + input.id()));
		return updateExisting ? repository.save(apply(customer, input)) : customer;
	}

	private CustomerEntity apply(CustomerEntity customer, CustomerInput input) {
		customer.setFirstName(input.firstName().trim());
		customer.setLastName(input.lastName().trim());
		customer.setAddress(input.address().trim());
		customer.setZipCode(input.zipCode().trim());
		customer.setCity(input.city().trim());
		customer.setProvince(input.province().trim());
		customer.setDocumentType(blankToNull(input.documentType()));
		customer.setDocumentNumber(blankToNull(input.documentNumber()));
		customer.setTaxCode(blankToNull(input.taxCode()));
		return customer;
	}

	protected static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
