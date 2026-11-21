package it.mmzitarosa.guitartortona.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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

	/** Gli admin possono fare tutto; senza sicurezza attiva (sviluppo locale) lo sono tutti. */
	public boolean isAdmin() {
		if (!securityEnabled) return true;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Roles.ADMIN));
	}

	/** Nome utente per il registro di audit; senza sicurezza attiva (sviluppo locale) è "local". */
	public String username() {
		if (!securityEnabled) return "local";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) return "system";
		if (auth.getPrincipal() instanceof Jwt jwt) {
			String preferred = jwt.getClaimAsString("preferred_username");
			return preferred != null ? preferred : jwt.getSubject();
		}
		return auth.getName();
	}
}
