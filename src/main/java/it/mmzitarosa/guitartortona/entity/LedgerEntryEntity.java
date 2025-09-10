package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

import static it.mmzitarosa.guitartortona.utils.Constant.*;

@Entity @Getter @Setter
@Table(name = "ledger")
public class LedgerEntryEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Temporal(TemporalType.DATE) @Column(nullable = false) private Date date;
	@Column(length = 50) private String invoiceNumber;
	@Temporal(TemporalType.DATE) @Column(nullable = false) private Date invoiceDate;
	@Column(nullable = false) private String description;
	@Column(length = 100) private String reason;
	@Enumerated(EnumType.ORDINAL) @Column(length = 2) private PaymentMethod paymentMethod;
	@ManyToOne @JoinColumn(name = "bank_id") private BankEntity bank;
	@Enumerated(EnumType.ORDINAL) @Column(length = 2) private PaymentType paymentType;
	@Column(length = 5) private String receiptNumber;
	@Enumerated(EnumType.ORDINAL) @Column(length = 2) private MovementType movementType;
	private Double amount;

	private String notes;
	@Temporal(TemporalType.DATE) @Column(name = "created_date", nullable = false, updatable = false) private Date createdDate = new Date();


	public Date getDate() { return date; }
	public void setDate(Date date) { this.date = date; }

	public Date getInvoiceDate() { return invoiceDate; }
	public void setInvoiceDate(Date invoiceDate) { this.invoiceDate = invoiceDate; }
}
