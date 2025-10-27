package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity @Getter @Setter
@Table(name = "purchase_item") @ToString(callSuper = true)
public class PurchaseItemEntity extends StatusEntity {

	@ManyToOne(optional = false) @JoinColumn(name = "purchase_id") private PurchaseEntity purchase;
	@ManyToOne(optional = false) @JoinColumn(name = "product_id") private ProductEntity product;

	private Double vat;
	@Column(name = "purchase_price") private Double purchasePrice;
	private Integer quantity;

}
