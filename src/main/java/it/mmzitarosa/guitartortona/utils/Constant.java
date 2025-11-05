package it.mmzitarosa.guitartortona.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static it.mmzitarosa.guitartortona.utils.InternalCodeGenerator.CodeType;

public class Constant {

	public enum PaymentMethod {BANK, CASH}

	public enum MovementType {INCOME, EXPENSE}

	public enum PaymentType {DEPOSIT, BALANCE}

	public enum Status {DRAFT, PENDING, COMPLETED, ARCHIVED}

	@AllArgsConstructor @Getter
	public enum ProductCondition {NEW(CodeType.NEW_PRODUCT), USED(CodeType.USED_PRODUCT); private final CodeType codeType; }

}