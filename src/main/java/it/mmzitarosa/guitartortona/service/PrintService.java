package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerDTO;
import it.mmzitarosa.guitartortona.mapper.LedgerEntityMapper;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service public class PrintService {

	private final LedgerRepository ledgerRepository;
	private final LedgerEntityMapper ledgerEntityMapper;

	public PrintService(LedgerRepository ledgerRepository, LedgerEntityMapper ledgerEntityMapper) {
		this.ledgerRepository = ledgerRepository;
		this.ledgerEntityMapper = ledgerEntityMapper;
	}
	
	@SneakyThrows public PrintableLedgerDTO readLedgerBetweenDates(String from, String to, String datePattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		return ledgerEntityMapper.toPrintableDto(ledgerRepository.findAllByStatusAndDateBetweenOrderByDateAsc(Status.COMPLETED, sdf.parse(from), sdf.parse(to)));

	}

}
