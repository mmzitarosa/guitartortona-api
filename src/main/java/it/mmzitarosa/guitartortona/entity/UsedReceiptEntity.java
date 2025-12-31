package it.mmzitarosa.guitartortona.entity;

import it.mmzitarosa.guitartortona.utils.InternalCodeGenerator;
import it.mmzitarosa.guitartortona.utils.InternalCodeGenerator.CodeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity @Table(name = "used_receipt") @Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UsedReceiptEntity extends PurchaseEntity {

	private Integer number;
	@Column(name = "internal_code") private String internalCode;
	// private UserEntity user;

	@PostPersist private void generateCode() {
		if (internalCode == null)
			internalCode = InternalCodeGenerator.composeCode(id, CodeType.USED_RECEIPT);
	}

}
