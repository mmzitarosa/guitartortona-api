package it.mmzitarosa.guitartortona.dto.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Differenza tra una ricevuta dell'usato registrata e la riga di brogliaccio che ne è nata.
 * L'API la segnala soltanto: sta all'admin scegliere se allineare il brogliaccio o ignorarla.
 */
public record LedgerAlignmentDTO(Long ledgerEntryId, Action action, Values current, Values proposed) {

	/** UPDATE: la riga va aggiornata ai valori proposti. ARCHIVE: la ricevuta è eliminata, la riga va eliminata (proposed nullo). */
	public enum Action { UPDATE, ARCHIVE }

	public record Values(@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date, String reason, Double amount) {}

}
