package it.mmzitarosa.guitartortona.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * API stateless protetta da JWT emessi da Keycloak (resource server).
 * <ul>
 *   <li>GET /api/v1/**: app-user, app-admin, guitarlab-viewer</li>
 *   <li>POST/PUT/PATCH /api/v1/**: app-user, app-admin</li>
 *   <li>DELETE /api/v1/**: app-admin</li>
 *   <li>actuator: sulla porta di management separata (non esposta) e' libero per probe/Prometheus; sulla porta dell'API solo health e' pubblico, il resto richiede app-admin</li>
 *   <li>tutto il resto: negato</li>
 * </ul>
 * Con {@code app.security.enabled=false} (SOLO sviluppo locale) tutte le richieste sono consentite.
 */
@Slf4j
@Configuration @EnableWebSecurity
public class SecurityConfig {

	@Value("${app.security.enabled:true}") private boolean securityEnabled;
	@Value("${app.cors.allowed-origins:}") private List<String> allowedOrigins;
	// Porta actuator separata (es. 8081): raggiungibile solo dentro il cluster, non esposta dall'Ingress. -1 = stessa porta dell'API
	@Value("${management.server.port:-1}") private int managementPort;

	@Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable) // API stateless con Bearer token: nessun cookie di sessione
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(Customizer.withDefaults());

		if (!securityEnabled) {
			log.warn("!!! app.security.enabled=false: AUTENTICAZIONE DISABILITATA. Usare solo in sviluppo locale !!!");
			return http.authorizeHttpRequests(a -> a.anyRequest().permitAll()).build();
		}

		return http
				.authorizeHttpRequests(a -> a
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(onManagementPort()).permitAll()
						.requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
						.requestMatchers(HttpMethod.GET, "/fonts/**", "/images/**").permitAll()
						.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(Roles.ADMIN)
						.requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole(Roles.VIEWER, Roles.USER, Roles.ADMIN)
						.requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole(Roles.ADMIN)
						.requestMatchers(HttpMethod.POST, "/api/v1/**").hasAnyRole(Roles.USER, Roles.ADMIN)
						.requestMatchers(HttpMethod.PUT, "/api/v1/**").hasAnyRole(Roles.USER, Roles.ADMIN)
						.requestMatchers(HttpMethod.PATCH, "/api/v1/**").hasAnyRole(Roles.USER, Roles.ADMIN)
						.anyRequest().denyAll())
				.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(new KeycloakRolesConverter())))
				.build();
	}

	private RequestMatcher onManagementPort() {
		RequestMatcher port = request -> managementPort > 0 && request.getLocalPort() == managementPort;
		return new AndRequestMatcher(EndpointRequest.toAnyEndpoint(), port);
	}

	@Bean public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins.stream().filter(o -> !o.isBlank()).toList());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
