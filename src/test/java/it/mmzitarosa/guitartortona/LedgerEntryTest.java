package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.exception.ConflictException;
import it.mmzitarosa.guitartortona.exception.NotFoundException;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import it.mmzitarosa.guitartortona.service.BankService;
import it.mmzitarosa.guitartortona.service.LedgerService;
import it.mmzitarosa.guitartortona.utils.Constant.MovementType;
import it.mmzitarosa.guitartortona.utils.Constant.PaymentMethod;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MariaDBContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Brogliaccio: archiviazione (stesse regole delle ricevute dell'usato: archivia o+a, ripristina e cancella solo a) e conferma delle righe PENDING.
 */
@DataJpaTest(properties = {
		"spring.flyway.enabled=true",
		"spring.flyway.baseline-on-migrate=true",
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LedgerService.class, BankService.class})
class LedgerEntryTest {

	static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11").withInitScript("db/migration/init.sql");

	static {
		MARIADB.start();
	}

	@DynamicPropertySource static void props(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MARIADB::getJdbcUrl);
		registry.add("spring.datasource.username", MARIADB::getUsername);
		registry.add("spring.datasource.password", MARIADB::getPassword);
	}

	@MockitoBean CurrentUser currentUser;
	@Autowired LedgerService service;
	@Autowired LedgerRepository repository;

	private final Sort sort = Sort.by("date", "id");

	@BeforeEach void asAdmin() {
		when(currentUser.isAdmin()).thenReturn(true);
	}

	private long newEntry() {
		CreateLedgerEntryDTO input = new CreateLedgerEntryDTO();
		input.setDate(LocalDate.of(2026, 3, 1));
		input.setDescription("Riga di prova");
		input.setPaymentMethod(PaymentMethod.CASH);
		input.setMovementType(MovementType.EXPENSE);
		input.setAmount(10.0);
		return service.createLedgerEntry(input).getId();
	}

	@Test void archivedEntryLeavesTheListAndAppearsOnlyInTheArchive() {
		long id = newEntry();
		service.archiveLedgerEntry(id);

		assertTrue(service.readLedger(2026, false, null, sort).stream().noneMatch(e -> e.getId() == id));
		assertTrue(service.readLedger(2026, true, null, sort).stream().anyMatch(e -> e.getId() == id && e.isArchived()));
	}

	@Test void archiveIsAllowedToOperatorsOnAnyRow() {
		long id = newEntry();
		when(currentUser.isAdmin()).thenReturn(false);

		service.archiveLedgerEntry(id);

		when(currentUser.isAdmin()).thenReturn(true);
		assertTrue(service.readLedgerEntry(id).isArchived());
	}

	@Test void operatorsDoNotSeeArchivedEntries() {
		long id = newEntry();
		service.archiveLedgerEntry(id);
		when(currentUser.isAdmin()).thenReturn(false);

		assertTrue(service.readLedger(2026, true, null, sort).isEmpty());
		assertThrows(NotFoundException.class, () -> service.readLedgerEntry(id));
	}

	@Test void archivedEntryIsReadOnly() {
		long id = newEntry();
		service.archiveLedgerEntry(id);

		CreateLedgerEntryDTO input = new CreateLedgerEntryDTO();
		input.setDate(LocalDate.of(2026, 3, 1));
		input.setDescription("Modificata");
		ConflictException e = assertThrows(ConflictException.class, () -> service.updateLedgerEntry(id, input));
		assertEquals("LEDGER_ENTRY_NOT_EDITABLE", e.getCode());
	}

	@Test void restoreBringsTheEntryBackAndKeepsItsData() {
		long id = newEntry();
		service.archiveLedgerEntry(id);

		LedgerEntryDTO restored = service.restoreLedgerEntry(id);

		assertFalse(restored.isArchived());
		assertEquals("Riga di prova", restored.getDescription());
		assertTrue(service.readLedger(2026, false, null, sort).stream().anyMatch(en -> en.getId() == id));
	}

	@Test void hardDeleteRequiresAnArchivedEntry() {
		long id = newEntry();

		ConflictException e = assertThrows(ConflictException.class, () -> service.deleteLedgerEntry(id));
		assertEquals("LEDGER_ENTRY_NOT_ARCHIVED", e.getCode());

		service.archiveLedgerEntry(id);
		service.deleteLedgerEntry(id);
		assertFalse(repository.existsById(id));
	}

	private long newPendingEntry() {
		LedgerEntryEntity entry = new LedgerEntryEntity();
		entry.setDate(LocalDate.of(2026, 3, 1));
		entry.setDescription("Pagato materiale usato");
		entry.setPaymentMethod(PaymentMethod.CASH);
		entry.setMovementType(MovementType.EXPENSE);
		entry.setAmount(10.0);
		entry.setStatus(Status.PENDING);
		return repository.saveAndFlush(entry).getId();
	}

	@Test void confirmMovesAPendingEntryToCompleted() {
		long id = newPendingEntry();
		assertEquals(Status.PENDING, service.readLedgerEntry(id).getStatus());

		assertEquals(Status.COMPLETED, service.confirmLedgerEntry(id).getStatus());
		assertEquals(Status.COMPLETED, service.readLedgerEntry(id).getStatus());
	}

	@Test void onlyPendingNotArchivedEntriesCanBeConfirmed() {
		long completed = newEntry();
		assertEquals("LEDGER_ENTRY_NOT_PENDING", assertThrows(ConflictException.class, () -> service.confirmLedgerEntry(completed)).getCode());

		long archived = newPendingEntry();
		service.archiveLedgerEntry(archived);
		assertEquals("LEDGER_ENTRY_NOT_PENDING", assertThrows(ConflictException.class, () -> service.confirmLedgerEntry(archived)).getCode());
	}

	@Test void ledgerCanBeFilteredByStatus() {
		long completed = newEntry();
		long pending = newPendingEntry();

		var onlyPending = service.readLedger(2026, false, Status.PENDING, sort);
		assertTrue(onlyPending.stream().anyMatch(e -> e.getId() == pending));
		assertTrue(onlyPending.stream().noneMatch(e -> e.getId() == completed));
		assertTrue(service.readLedger(2026, false, null, sort).stream().anyMatch(e -> e.getId() == completed));
	}

}
