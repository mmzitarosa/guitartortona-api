package it.mmzitarosa.guitartortona.dto.supplier;

import it.mmzitarosa.guitartortona.entity.SupplierEntity;


public record SupplierDTO(Long id,
						  String name) {
	public static SupplierDTO of(SupplierEntity entity) {
		return new SupplierDTO(entity.getId(), entity.getName());
	}
}
