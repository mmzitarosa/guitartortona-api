package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity @Getter @Setter
@Table(name = "category") @ToString
public class CategoryEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	private String name;
	@ManyToOne @JoinColumn(name = "parent_category_id") private CategoryEntity parentCategory;

	@ToString.Exclude @OneToMany(mappedBy = "parentCategory", fetch = FetchType.EAGER) private List<CategoryEntity> subCategories;
	@ToString.Exclude @OneToMany(mappedBy = "category") private List<ProductEntity> products;

}
