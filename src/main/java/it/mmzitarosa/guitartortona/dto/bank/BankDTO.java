package it.mmzitarosa.guitartortona.dto.bank;

import it.mmzitarosa.guitartortona.entity.BankEntity;

public record BankDTO(Long id,
					  String name) {
	public static BankDTO of(BankEntity entity) {
		return new BankDTO(entity.getId(), entity.getName());
	}
}