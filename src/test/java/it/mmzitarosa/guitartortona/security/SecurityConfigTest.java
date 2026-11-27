package it.mmzitarosa.guitartortona.security;

import it.mmzitarosa.guitartortona.controller.CustomerController;
import it.mmzitarosa.guitartortona.controller.IncomingInvoiceController;
import it.mmzitarosa.guitartortona.controller.LedgerController;
import it.mmzitarosa.guitartortona.controller.UsedReceiptController;
import it.mmzitarosa.guitartortona.service.CustomerService;
import it.mmzitarosa.guitartortona.service.UsedReceiptService;
import it.mmzitarosa.guitartortona.service.IncomingInvoiceService;
import it.mmzitarosa.guitartortona.service.PurchaseItemService;
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
	@org.springframework.context.annotation.Import({SecurityConfig.class, LedgerController.class, IncomingInvoiceController.class, UsedReceiptController.class, CustomerController.class})
	static class TestConfig {
		@Bean UsedReceiptService usedReceiptService() { return Mockito.mock(UsedReceiptService.class); }
		@Bean CustomerService customerService() { return Mockito.mock(CustomerService.class); }
		@Bean LedgerService ledgerService() { return Mockito.mock(LedgerService.class); }
		@Bean PurchaseItemService purchaseItemService() { return Mockito.mock(PurchaseItemService.class); }
		@Bean IncomingInvoiceService incomingInvoiceService() { return Mockito.mock(IncomingInvoiceService.class); }
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

	@Test void staticResourcesArePublic() throws Exception {
		mvc.perform(get("/fonts/Code39.ttf")).andExpect(status().isNotFound());
		mvc.perform(get("/images/logo90.png")).andExpect(status().isNotFound());
	}

	@Test void viewerCannotReadLedger() throws Exception {
		mvc.perform(get("/api/v1/ledger").param("year", "2025").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/ledgerEntry/1").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
	}

	@Test void viewerCannotReadInvoices() throws Exception {
		mvc.perform(get("/api/v1/incomingInvoices").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/incomingInvoice/1").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/archive/incomingInvoices").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
	}

	@Test void archivedInvoicesAndRestoreAreAdminOnly() throws Exception {
		mvc.perform(get("/api/v1/archive/incomingInvoices").with(withRole(Roles.USER))).andExpect(status().isForbidden());
		mvc.perform(patch("/api/v1/incomingInvoice/1/restore").with(withRole(Roles.USER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/archive/incomingInvoices").with(withRole(Roles.ADMIN))).andExpect(status().isOk());
		mvc.perform(patch("/api/v1/incomingInvoice/1/restore").with(withRole(Roles.ADMIN))).andExpect(status().is2xxSuccessful());
	}

	@Test void userCanArchiveInvoice() throws Exception {
		mvc.perform(patch("/api/v1/incomingInvoice/1/archive").with(withRole(Roles.USER))).andExpect(status().isNoContent());
		mvc.perform(patch("/api/v1/incomingInvoice/1/archive").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
	}

	@Test void viewerCannotReadReceiptsAndCustomers() throws Exception {
		mvc.perform(get("/api/v1/usedReceipts").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/usedReceipt/1").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/customers").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/customers").with(withRole(Roles.USER))).andExpect(status().isOk());
	}

	@Test void archivedReceiptsAndRestoreAreAdminOnly() throws Exception {
		mvc.perform(get("/api/v1/archive/usedReceipts").with(withRole(Roles.USER))).andExpect(status().isForbidden());
		mvc.perform(patch("/api/v1/usedReceipt/1/restore").with(withRole(Roles.USER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/archive/usedReceipts").with(withRole(Roles.ADMIN))).andExpect(status().isOk());
		mvc.perform(patch("/api/v1/usedReceipt/1/restore").with(withRole(Roles.ADMIN))).andExpect(status().is2xxSuccessful());
	}

	@Test void archivedLedgerAndRestoreAreAdminOnly() throws Exception {
		mvc.perform(get("/api/v1/archive/ledger").param("year", "2025").with(withRole(Roles.USER))).andExpect(status().isForbidden());
		mvc.perform(patch("/api/v1/ledgerEntry/1/restore").with(withRole(Roles.USER))).andExpect(status().isForbidden());
		mvc.perform(get("/api/v1/archive/ledger").param("year", "2025").with(withRole(Roles.ADMIN))).andExpect(status().isOk());
		mvc.perform(patch("/api/v1/ledgerEntry/1/restore").with(withRole(Roles.ADMIN))).andExpect(status().is2xxSuccessful());
	}

	@Test void userCanArchiveLedgerEntries() throws Exception {
		mvc.perform(patch("/api/v1/ledgerEntry/1/archive").with(withRole(Roles.USER))).andExpect(status().is2xxSuccessful());
		mvc.perform(patch("/api/v1/ledgerEntry/1/archive").with(withRole(Roles.VIEWER))).andExpect(status().isForbidden());
	}

	@Test void userCanReadLedger() throws Exception {
		mvc.perform(get("/api/v1/ledger").param("year", "2025").with(withRole(Roles.USER))).andExpect(status().isOk());
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
