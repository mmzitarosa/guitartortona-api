package it.mmzitarosa.guitartortona.utils;

import lombok.AllArgsConstructor;

public class InternalCodeGenerator {

	@AllArgsConstructor
	public enum CodeType {
		NEW_PRODUCT("N", 7), USED_PRODUCT("U", 7), USED_RECEIPT("R", 9);
		private final String prefix = "G";
		private final String suffix;
		private final int numberLength;
	}

	/* == PUBLIC METHODS == */
	public static String composeCode(long id, CodeType codeType) {
		return String.format("%s%0" + codeType.numberLength + "d%s", codeType.prefix, id, codeType.suffix);
	}

}
