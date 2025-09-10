package it.mmzitarosa.guitartortona.entity;

import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity @Getter @Setter
@Table(name = "incoming_invoice", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"number", "supplier_id"})
}) public class IncomingInvoiceEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(nullable = false, length = 50) private String number;
	@ManyToOne(optional = false) @JoinColumn(name = "supplier_id", nullable = false) private SupplierEntity supplier;
	@Temporal(TemporalType.DATE) @Column(nullable = false) private Date date;
	@Column(nullable = false) private Double amount;
	private String notes;

	public Date getDate() { return date; }

	public void setDate(Date date) { this.date = date; }
}
