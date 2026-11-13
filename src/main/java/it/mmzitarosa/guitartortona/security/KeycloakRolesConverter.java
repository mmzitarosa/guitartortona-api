package it.mmzitarosa.guitartortona.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converte i realm role di Keycloak in authority Spring ("ROLE_&lt;nome&gt;").
 * Legge il claim "roles" (mapper dedicato sul client) e, in alternativa, "realm_access.roles" (claim standard di Keycloak).
 */
public class KeycloakRolesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();

	public KeycloakRolesConverter() {
		delegate.setJwtGrantedAuthoritiesConverter(KeycloakRolesConverter::extractAuthorities);
		delegate.setPrincipalClaimName("preferred_username");
	}

	@Override public AbstractAuthenticationToken convert(Jwt jwt) {
		return delegate.convert(jwt);
	}

	private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Set<String> roles = new LinkedHashSet<>(asStringList(jwt.getClaim("roles")));

		Object realmAccess = jwt.getClaim("realm_access");
		if (realmAccess instanceof Map<?, ?> map) roles.addAll(asStringList(map.get("roles")));

		return roles.stream().<GrantedAuthority>map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
	}

	private static List<String> asStringList(Object value) {
		if (value instanceof Collection<?> c) return c.stream().filter(String.class::isInstance).map(String.class::cast).toList();
		return List.of();
	}

}
