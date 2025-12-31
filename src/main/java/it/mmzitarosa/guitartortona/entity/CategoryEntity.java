package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

import static lombok.ToString.Exclude;

@Entity @Table(name = "category")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryEntity extends IdEntity {

	private String name;
	@ManyToOne @JoinColumn(name = "parent_category_id") private CategoryEntity parentCategory;

	@Exclude @OneToMany(mappedBy = "parentCategory", fetch = FetchType.EAGER) private List<CategoryEntity> subCategories;
	@Exclude @OneToMany(mappedBy = "category") private List<ProductEntity> products;

}
