package it.mmzitarosa.guitartortona.security;

import it.mmzitarosa.guitartortona.controller.LedgerController;
import it.mmzitarosa.guitartortona.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica le regole di autorizzazione (senza database). */
@SpringJUnitWebConfig(SecurityConfigTest.TestConfig.class)
@TestPropertySource(properties = {
		"app.security.enabled=true",
		"app.cors.allowed-origins=https://vue.guitar.lab"
})
class SecurityConfigTest {

	@Configuration @EnableWebMvc @EnableSpringDataWebSupport
	@org.springframework.context.annotation.Import({SecurityConfig.class, LedgerController.class})
	static class TestConfig {
		@Bean LedgerService ledgerService() { return Mockito.mock(LedgerService.class); }
		@Bean JwtDecoder jwtDecoder() { return Mockito.mock(JwtDecoder.class); }
	}

	@Autowired WebApplicationContext context;
	MockMvc mvc;

	@BeforeEach void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(SecurityMockMvcConfigurers.springSecurity()).build();
	}

	private RequestPostProcessor withRole(String role) {
		return SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.claim("roles", List.of(role)))
				.authorities(new KeycloakRolesConverter().convert(
						org.springframework.security.oauth2.jwt.Jwt.withTokenValue("t").header("alg", "none").claim("roles", List.of(role)).build()).getAuthorities());
	}

	@Test void withoutTokenIs401() throws Exception {
		mvc.perform(get("/api/v1/ledger").param("year", "2025")).andExpect(status().isUnauthorized());
	}

	@Test void viewerCanRead() throws Exception {
		mvc.perform(get("/api/v1/ledger").param("year", "2025").with(withRole(Roles.VIEWER))).andExpect(status().isOk());
	}

	@Test void viewerCannotDelete() throws Exception {
		mvc.perform(delete("/api/v1/ledgerEntry/1").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
	}

	@Test void viewerCannotWrite() throws Exception {
		mvc.perform(post("/api/v1/ledgerEntry").contentType("application/json").content("{}").with(withRole(Roles.VIEWER)))
				.andExpect(status().isForbidden());
	}

	@Test void userCannotDelete() throws Exception {
		mvc.perform(delete("/api/v1/ledgerEntry/1").with(withRole(Roles.USER))).andExpect(status().isForbidden());
	}

	@Test void adminCanDelete() throws Exception {
		mvc.perform(delete("/api/v1/ledgerEntry/1").with(withRole(Roles.ADMIN))).andExpect(status().isOk());
	}

	@Test void unknownRoleIsForbidden() throws Exception {
		mvc.perform(get("/api/v1/ledger").param("year", "2025").with(withRole("guest"))).andExpect(status().isForbidden());
	}

	@Test void otherPathsAreDenied() throws Exception {
		mvc.perform(get("/anything").with(withRole(Roles.ADMIN))).andExpect(status().isForbidden());
	}

	@Test void corsOnlyForConfiguredOrigin() throws Exception {
		mvc.perform(options("/api/v1/ledger").header("Origin", "https://vue.guitar.lab").header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isOk());
		mvc.perform(options("/api/v1/ledger").header("Origin", "https://evil.example").header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden());
	}

}
