package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.PrintableLedgerEntryDTO;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.mapper.LedgerEntityMapper;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service public class LedgerService {

	private final LedgerRepository ledgerRepository;
	private final LedgerEntityMapper ledgerEntityMapper;
	private final BankService bankService;

	public LedgerService(LedgerRepository ledgerRepository, LedgerEntityMapper ledgerEntityMapper, BankService bankService) {
		this.ledgerRepository = ledgerRepository;
		this.ledgerEntityMapper = ledgerEntityMapper;
		this.bankService = bankService;
	}

	public LedgerEntryDTO createLedgerEntry(CreateLedgerEntryDTO ledgerEntryDTO) {
		return saveLedgerEntry(null, ledgerEntryDTO);
	}

	public Page<LedgerEntryDTO> readLedger(Pageable pageable) {
		return ledgerEntityMapper.toDto(ledgerRepository.findAll(pageable));
	}

	@SneakyThrows
	public Page<LedgerEntryDTO> readLedgerBetweenDates(String from, String to, String datePattern, Pageable pageable) {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		return ledgerEntityMapper.toDto(ledgerRepository.findAllByDateBetweenOrderByDateAsc(sdf.parse(from), sdf.parse(to), pageable));
	}

	public LedgerEntryDTO readLedgerEntryById(long id) {
		return ledgerEntityMapper.toDto(ledgerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("LedgerEntry not found")));
	}

	public LedgerEntryDTO updateLedgerEntryById(long id, CreateLedgerEntryDTO ledgerEntryDTO) {
		return saveLedgerEntry(id, ledgerEntryDTO);
	}

	public void deleteLedgerEntryById(long id) {
		ledgerRepository.deleteById(id);
	}
	
	private LedgerEntryDTO saveLedgerEntry(Long id, CreateLedgerEntryDTO ledgerEntryDTO) {
		LedgerEntryEntity ledgerEntry = ledgerEntityMapper.toEntity(ledgerEntryDTO, ledgerEntryDTO.getBankId() != null ? bankService.getBank(ledgerEntryDTO.getBankId()) : null);
		ledgerEntry.setId(id);
		ledgerEntry = ledgerRepository.save(ledgerEntry);
		return ledgerEntityMapper.toDto(ledgerEntry);
	}


}
