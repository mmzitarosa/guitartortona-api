package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity @Table(name = "sale")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SaleEntity extends DateEntity {

	private LocalDate date;
//	private String receiptNumber;
	private Double vat;
	@Column(name = "sale_price") private Double salePrice;
	private Integer quantity;
	private String notes;

	@ManyToOne @JoinColumn(name = "product_id") private ProductEntity product;

}
