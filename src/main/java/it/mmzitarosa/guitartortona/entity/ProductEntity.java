package it.mmzitarosa.guitartortona.entity;

import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import it.mmzitarosa.guitartortona.utils.InternalCodeGenerator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.ToString.Exclude;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "product")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProductEntity extends DateEntity {

	private String code;																								// SKU
	@Column(name = "internal_code") private String internalCode;
	@ManyToOne @JoinColumn(name = "brand_id") private BrandEntity brand;
	@ManyToOne @JoinColumn(name = "category_id") private CategoryEntity category;
	private String description;
	@Enumerated(EnumType.ORDINAL) @Column(name = "condition_id") private ProductCondition condition;					// NEW, USED
	private Double price;
	@Column(name = "reorder_point") private Integer reorderPoint;
	private String notes;

	// Aggiornate mediante trigger
	private Integer stock = 0;
	private Integer stockPending = 0;

	@Exclude @OneToMany(mappedBy = "product") private List<PurchaseItemEntity> purchaseItems = new ArrayList<>();
	@Exclude @OneToMany(mappedBy = "product") private List<SaleEntity> sales = new ArrayList<>();

	@PostPersist private void generateCode() {
		if (internalCode == null)
			internalCode = InternalCodeGenerator.composeCode(id, condition.getCodeType());
	}

	@Transient public boolean isLowStock() {
		return reorderPoint != null && stock < reorderPoint;
	}

}
