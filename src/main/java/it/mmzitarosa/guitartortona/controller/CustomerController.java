package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.customer.CustomerDTO;
import it.mmzitarosa.guitartortona.dto.customer.CustomerInput;
import it.mmzitarosa.guitartortona.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController @RequestMapping("/api/v1")
public class CustomerController {

	private final CustomerService service;

	@GetMapping("/customers")
	public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
		return ResponseEntity.ok(service.getAllCustomers(false));
	}

	@GetMapping("/archive/customers")
	public ResponseEntity<List<CustomerDTO>> getArchivedCustomers() {
		return ResponseEntity.ok(service.getAllCustomers(true));
	}

	@GetMapping("/customer/{id}")
	public ResponseEntity<CustomerDTO> getCustomer(@PathVariable long id) {
		return ResponseEntity.ok(service.getCustomerDetail(id));
	}

	@PutMapping("/customer/{id}")
	public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable long id, @Valid @RequestBody CustomerInput request) {
		return ResponseEntity.ok(service.updateCustomer(id, request));
	}

	@PatchMapping("/customer/{id}/archive")
	public void archiveCustomer(@PathVariable long id) {
		service.archiveCustomer(id);
	}

	@PatchMapping("/customer/{id}/restore")
	public ResponseEntity<CustomerDTO> restoreCustomer(@PathVariable long id) {
		return ResponseEntity.ok(service.restoreCustomer(id));
	}

	@DeleteMapping("/customer/{id}")
	public void deleteCustomer(@PathVariable long id) {
		service.deleteCustomer(id);
	}

}
