package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerEntryDTO;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.mapper.LedgerEntityMapper;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

@Service public class PrintService {

	private final LedgerRepository ledgerRepository;
	private final LedgerEntityMapper ledgerEntityMapper;

	public PrintService(LedgerRepository ledgerRepository, LedgerEntityMapper ledgerEntityMapper) {
		this.ledgerRepository = ledgerRepository;
		this.ledgerEntityMapper = ledgerEntityMapper;
	}
	
	@SneakyThrows
	public PrintableLedgerDTO readLedgerBetweenDates(String from, String to, String datePattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		return ledgerEntityMapper.toPrintableDto(ledgerRepository.findAllByDateBetweenOrderByDateAsc(sdf.parse(from), sdf.parse(to)));

	}

}
