package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity @Getter @Setter
@Table(name = "incoming_invoice") @ToString(callSuper = true)
public class IncomingInvoiceEntity extends PurchaseEntity {

	private String number;
	@ManyToOne @JoinColumn(name = "supplier_id") private SupplierEntity supplier;

	//TODO: Aggiungere scadenze

}
