package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.customer.CustomerDTO;
import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.entity.CustomerEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
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

	private static final Sort SORT = Sort.by("lastName", "firstName", "id");

	/**
	 * GET /customers
	 * GET /archive/customers (solo admin)
	 */
	@Transactional(readOnly = true) public List<CustomerDTO> getAllCustomers(boolean archived) {
		return repository.findAllByArchived(archived, SORT).stream().map(CustomerDTO::of).toList();
	}

	/**
	 * GET /customer/{id} (solo admin)
	 */
	@Transactional(readOnly = true) public CustomerDTO getCustomerDetail(long id) {
		return CustomerDTO.of(getCustomer(id));
	}

	/**
	 * PUT /customer/{id} (solo admin)
	 * Rettifica dell'anagrafica: le ricevute già compilate mantengono la loro copia dei dati.
	 */
	public CustomerDTO updateCustomer(long id, CustomerInput input) {
		CustomerEntity customer = getCustomer(id);
		if (customer.isArchived())
			throw new ConflictException("CUSTOMER_NOT_EDITABLE", "Archived customer is not editable: " + id);
		return CustomerDTO.of(repository.save(apply(customer, input)));
	}

	/**
	 * PATCH /customer/{id}/archive (solo admin)
	 */
	public void archiveCustomer(long id) {
		CustomerEntity customer = getCustomer(id);
		if (customer.isArchived())
			return;
		customer.setArchived(true);
		repository.save(customer);
	}

	/**
	 * PATCH /customer/{id}/restore (solo admin)
	 */
	public CustomerDTO restoreCustomer(long id) {
		CustomerEntity customer = getCustomer(id);
		if (customer.isArchived()) {
			customer.setArchived(false);
			customer = repository.save(customer);
		}
		return CustomerDTO.of(customer);
	}

	/**
	 * DELETE /customer/{id} (solo admin)
	 * Cancellazione fisica, ammessa solo per clienti già archiviati. Le ricevute collegate perdono solo il riferimento (customer_id = NULL).
	 */
	public void deleteCustomer(long id) {
		CustomerEntity customer = getCustomer(id);
		if (!customer.isArchived())
			throw new ConflictException("CUSTOMER_NOT_ARCHIVED", "Only archived customers can be permanently deleted: " + id);
		repository.delete(customer);
	}

	protected CustomerEntity getCustomer(long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("CUSTOMER_NOT_FOUND", "Customer not found: " + id));
	}

	/**
	 * Helper: anagrafica collegata ai dati inseriti in una ricevuta.
	 * Senza id crea un nuovo cliente; con id aggiorna l'anagrafica solo se richiesto (la ricevuta tiene comunque una copia dei dati).
	 * Un cliente archiviato non si può scegliere né aggiornare; la ricevuta che lo ha già collegato può tenerlo ({@code currentCustomerId}).
	 */
	protected CustomerEntity resolveCustomer(CustomerInput input, boolean updateExisting, Long currentCustomerId) {
		if (input.id() == null)
			return repository.save(apply(new CustomerEntity(), input));

		CustomerEntity customer = getCustomer(input.id());
		if (customer.isArchived() && (updateExisting || !customer.getId().equals(currentCustomerId)))
			throw new ConflictException("CUSTOMER_ARCHIVED", "Archived customer cannot be used: " + input.id());
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
