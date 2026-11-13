package it.mmzitarosa.guitartortona.security;

/** Realm roles definiti in Keycloak (realm "guitarlab"). */
public final class Roles {

	/** Amministratore applicazione: lettura, scrittura e cancellazione. */
	public static final String ADMIN = "app-admin";
	/** Utente applicazione: lettura e scrittura (no cancellazione). */
	public static final String USER = "app-user";
	/** Sola lettura. */
	public static final String VIEWER = "guitarlab-viewer";

	private Roles() {}

}
