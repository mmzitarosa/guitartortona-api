package it.mmzitarosa.guitartortona.entity;

import it.mmzitarosa.guitartortona.utils.InternalCodeGenerator;
import it.mmzitarosa.guitartortona.utils.InternalCodeGenerator.CodeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity @Table(name = "used_receipt") @Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UsedReceiptEntity extends PurchaseEntity {

	/** Numero progressivo per anno della data ricevuta: assegnato alla Registra e poi non cambia piu' */
	private Integer number;
	/** Anno a quattro cifre della serie di numerazione */
	private Integer year;
	@Column(name = "internal_code") private String internalCode;
	/** Indice (1 = A) dell'ultima lettera assegnata alla conferma: le lettere di righe rimosse non vengono riutilizzate */
	@Column(name = "last_letter_index") private int lastLetterIndex;

	/** Riferimento all'anagrafica; i dati sotto sono la copia usata dalla ricevuta */
	@ManyToOne @JoinColumn(name = "customer_id") private CustomerEntity customer;
	@Column(name = "customer_first_name") private String customerFirstName;
	@Column(name = "customer_last_name") private String customerLastName;
	@Column(name = "customer_address") private String customerAddress;
	@Column(name = "customer_zip_code") private String customerZipCode;
	@Column(name = "customer_city") private String customerCity;
	@Column(name = "customer_province") private String customerProvince;
	@Column(name = "customer_document_type") private String customerDocumentType;
	@Column(name = "customer_document_number") private String customerDocumentNumber;
	@Column(name = "customer_tax_code") private String customerTaxCode;

	@PostPersist private void generateCode() {
		if (internalCode == null)
			internalCode = InternalCodeGenerator.composeCode(id, CodeType.USED_RECEIPT);
	}

	@Transient public boolean hasCustomerData() {
		return customerLastName != null || customerFirstName != null;
	}

	/** "Cognome Nome", null se la ricevuta non ha dati cliente */
	@Transient public String getCustomerFullName() {
		if (!hasCustomerData()) return null;
		return ((customerLastName == null ? "" : customerLastName) + " " + (customerFirstName == null ? "" : customerFirstName)).trim();
	}

	/** "numero/anno" (es. 1/26), null finche' la ricevuta non e' registrata */
	@Transient public String getReference() {
		return number == null || year == null ? null : number + "/" + twoDigits(year);
	}

	/** "numero/anno lettera" (es. 1/26 A), null finche' la ricevuta non e' registrata */
	@Transient public String getItemReference(String letter) {
		String reference = getReference();
		return reference == null || letter == null ? null : reference + " " + letter.toUpperCase();
	}

	public static String twoDigits(int year) {
		return String.format("%02d", year % 100);
	}

}
