package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

import static it.mmzitarosa.guitartortona.utils.Constant.*;

@Entity @Getter @Setter
@Table(name = "ledger") @ToString(callSuper = true)
public class LedgerEntryEntity extends StatusEntity {

	private LocalDate date;
	@Column(name = "invoice_number") private String invoiceNumber;
	@Column(name = "invoice_date") private LocalDate invoiceDate;
	private String description;
	private String reason;
	@Enumerated(EnumType.ORDINAL) @Column(name = "payment_method") private PaymentMethod paymentMethod;
	@ManyToOne @JoinColumn(name = "bank_id") private BankEntity bank;
	@Enumerated(EnumType.ORDINAL) @Column(name = "payment_type") private PaymentType paymentType;
	@Column(name = "receipt_number") private String receiptNumber;
	@Enumerated(EnumType.ORDINAL) @Column(name = "movement_type") private MovementType movementType;
	private Double amount;
	private String notes;

}
