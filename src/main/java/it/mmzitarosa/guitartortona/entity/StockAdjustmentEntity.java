package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Rettifica inventariale: {@code quantity} è la differenza (con segno) tra la quantità contata e la giacenza precedente. */
@Entity @Table(name = "stock_adjustment")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentEntity extends DateEntity {

	@Column(name = "previous_stock") private Integer previousStock;
	@Column(name = "counted_stock") private Integer countedStock;
	private Integer quantity;
	private String reason;

	@ManyToOne @JoinColumn(name = "product_id") private ProductEntity product;

}
