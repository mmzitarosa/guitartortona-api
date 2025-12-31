package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.ToString.Exclude;

import java.util.List;

@Entity @Table(name = "brand")
@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BrandEntity extends IdEntity {

	private String name;

	@Exclude @OneToMany(mappedBy = "brand") private List<ProductEntity> products;

}
