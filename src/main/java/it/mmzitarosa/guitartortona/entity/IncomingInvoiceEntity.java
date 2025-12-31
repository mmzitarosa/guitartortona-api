package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity @Table(name = "incoming_invoice")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IncomingInvoiceEntity extends PurchaseEntity {

	private String number;

	@ManyToOne @JoinColumn(name = "supplier_id") private SupplierEntity supplier;

	//TODO: Aggiungere scadenze
}
