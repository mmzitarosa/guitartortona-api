package it.mmzitarosa.guitartortona.entity;

import it.mmzitarosa.guitartortona.utils.Constant.ProductCondition;
import it.mmzitarosa.guitartortona.utils.InternalCodeGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity @Getter @Setter
@Table(name = "product") @ToString(callSuper = true)
public class ProductEntity extends StatusEntity {

	private String code;	// SKU
	@Column(name = "internal_code") private String internalCode;
	@ManyToOne @JoinColumn(name = "category_id") private CategoryEntity category;
	@ManyToOne @JoinColumn(name = "brand_id") private BrandEntity brand;
	private String description;
	@Enumerated(EnumType.ORDINAL) @Column(name = "condition_id") private ProductCondition condition; 	// NEW, USED
	private Double price;
	@Column(name = "reorder_point") private Integer reorderPoint;	// Se impostate, è necessario avvisare in caso di quantità sotto soglia
	private String notes;

	@ToString.Exclude @OneToMany(mappedBy = "product") private List<PurchaseItemEntity> purchaseItems = new ArrayList<>();

	@PostPersist private void generateCode() {
		if (internalCode == null)
			internalCode = InternalCodeGenerator.composeCode(id, condition.getCodeType());
	}

}
