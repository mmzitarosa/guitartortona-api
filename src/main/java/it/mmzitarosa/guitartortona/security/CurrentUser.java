package it.mmzitarosa.guitartortona.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Informazioni sull'utente corrente, per adattare i dati restituiti al suo ruolo. */
@Component
public class CurrentUser {

	@Value("${app.security.enabled:true}") private boolean securityEnabled;

	/** I viewer (sola lettura) non possono vedere costi e dati di fatture/fornitori. */
	public boolean canSeeCosts() {
		if (!securityEnabled) return true;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_" + Roles.USER) || a.getAuthority().equals("ROLE_" + Roles.ADMIN));
	}
}
