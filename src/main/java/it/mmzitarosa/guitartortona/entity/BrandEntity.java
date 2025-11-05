package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity @Getter @Setter
@Table(name = "brand") @ToString
@NoArgsConstructor @RequiredArgsConstructor
public class BrandEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@NonNull private String name;

	@ToString.Exclude @OneToMany(mappedBy = "brand") private List<ProductEntity> products;

}
