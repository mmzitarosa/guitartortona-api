package it.mmzitarosa.guitartortona.entity.view;

import it.mmzitarosa.guitartortona.entity.StatusEntity;
import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity @Getter @Setter
@Table(name = "product_stock_view") @ToString(callSuper = true)
public class ProductStockView extends StatusEntity {

	@Column(name = "code")				private String code;	// SKU
	@Column(name = "internal_code")		private String internalCode;
	@Column(name = "category_id")		private Long categoryId;
	@Column(name = "category_name")		private String categoryName;
	@Column(name = "brand_id")			private Long brandId;
	@Column(name = "brand_name")		private String brandName;
	@Column(name = "description")		private String description;
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "condition_id")		private ProductCondition condition; 	// NEW, USED
	@Column(name = "price")				private Double price;
	@Column(name = "reorder_point") 	private Integer reorderPoint;
	@Column(name = "notes")				private String notes;
	@Column(name = "available_stock")	private Integer availableStock;
	@Column(name = "pending_stock") 	private Integer pendingStock;

	public Integer getTotalStock() {
		return (availableStock != null ? availableStock : 0) + (pendingStock != null ? pendingStock : 0);
	}
	public boolean isLowStock() {
		return reorderPoint != null && availableStock != null && availableStock <= reorderPoint;
	}
	public boolean isOutOfStock() {
		return availableStock == null || availableStock == 0;
	}
}
