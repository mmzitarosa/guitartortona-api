package it.mmzitarosa.guitartortona.dto.customer;

import it.mmzitarosa.guitartortona.entity.CustomerEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;

public record CustomerDTO(
		Long id,
		String firstName,
		String lastName,
		String address,
		String zipCode,
		String city,
		String province,
		String documentType,
		String documentNumber,
		String taxCode
) {
	public static CustomerDTO of(CustomerEntity entity) {
		return new CustomerDTO(entity.getId(), entity.getFirstName(), entity.getLastName(), entity.getAddress(), entity.getZipCode(),
				entity.getCity(), entity.getProvince(), entity.getDocumentType(), entity.getDocumentNumber(), entity.getTaxCode());
	}

	/** Dati cliente copiati sulla ricevuta (id = anagrafica di origine, se presente); null se la ricevuta non ha dati cliente */
	public static CustomerDTO of(UsedReceiptEntity receipt) {
		if (!receipt.hasCustomerData()) return null;
		return new CustomerDTO(receipt.getCustomer() == null ? null : receipt.getCustomer().getId(), receipt.getCustomerFirstName(),
				receipt.getCustomerLastName(), receipt.getCustomerAddress(), receipt.getCustomerZipCode(), receipt.getCustomerCity(),
				receipt.getCustomerProvince(), receipt.getCustomerDocumentType(), receipt.getCustomerDocumentNumber(), receipt.getCustomerTaxCode());
	}
}
