package it.mmzitarosa.guitartortona.utils;

/** Lettere delle righe di una ricevuta: A..Z, AA, AB, ... (indice 1-based) */
public final class ReceiptLetters {

	private ReceiptLetters() {}

	public static String fromIndex(int index) {
		if (index < 1) throw new IllegalArgumentException("Index must be positive: " + index);
		StringBuilder sb = new StringBuilder();
		for (int n = index; n > 0; n = (n - 1) / 26)
			sb.insert(0, (char) ('A' + (n - 1) % 26));
		return sb.toString();
	}

	public static int toIndex(String letter) {
		int index = 0;
		for (char c : letter.toUpperCase().toCharArray())
			index = index * 26 + (c - 'A' + 1);
		return index;
	}

}
