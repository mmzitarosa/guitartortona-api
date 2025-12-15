package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerDTO;
import it.mmzitarosa.guitartortona.mapper.LedgerEntityMapper;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service public class PrintService {

	private final LedgerRepository ledgerRepository;
	private final LedgerEntityMapper ledgerEntityMapper;

	public PrintService(LedgerRepository ledgerRepository, LedgerEntityMapper ledgerEntityMapper) {
		this.ledgerRepository = ledgerRepository;
		this.ledgerEntityMapper = ledgerEntityMapper;
	}

	public PrintableLedgerDTO readLedger(String from, String to, String datePattern, Sort sort) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);
		return ledgerEntityMapper.toPrintableDto(ledgerRepository.findAllByArchivedFalseAndDateBetween(LocalDate.parse(from, formatter), LocalDate.parse(to, formatter), sort));

	}

}
