package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity @Table(name = "purchase_item")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PurchaseItemEntity extends DateEntity {

	private Double vat;
	@Column(name = "purchase_price") private Double purchasePrice;
	private Integer quantity;

	@ManyToOne @JoinColumn(name = "purchase_id") private PurchaseEntity purchase;
	@ManyToOne @JoinColumn(name = "product_id") private ProductEntity product;

}
