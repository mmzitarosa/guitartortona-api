package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.ledger.CreateLedgerEntryDTO;
import it.mmzitarosa.guitartortona.dto.ledger.LedgerEntryDTO;
import it.mmzitarosa.guitartortona.entity.LedgerEntryEntity;
import it.mmzitarosa.guitartortona.mapper.LedgerEntityMapper;
import it.mmzitarosa.guitartortona.repository.LedgerRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

@Service public class LedgerService {

	/* == CONSTANTS == */
	private final LedgerRepository repository;
	private final LedgerEntityMapper mapper;
	private final BankService bankService;

	/* == CONSTRUCTOR == */
	public LedgerService(LedgerRepository repository, LedgerEntityMapper mapper, BankService bankService) {
		this.repository = repository;
		this.mapper = mapper;
		this.bankService = bankService;
	}

	public LedgerEntryDTO createLedgerEntry(CreateLedgerEntryDTO ledgerEntryDTO) {
		// Converto il DTO in oggetto
		LedgerEntryEntity ledgerEntry = mapper.toEntity(ledgerEntryDTO, ledgerEntryDTO.getBankId() != null ? bankService.getBank(ledgerEntryDTO.getBankId()) : null);
		// Anche se primo inserimento la salvo già come COMPLETED, //TODO capire se necessaria validazione e salvarlo come DRAFT
		ledgerEntry.setStatus(Status.COMPLETED);
		// Salvo record su DB
		ledgerEntry = repository.save(ledgerEntry);
		// Converto l'entity del record in DTO
		return mapper.toDto(ledgerEntry);
	}

	public List<LedgerEntryDTO> readLedger(Status status) {
		return mapper.toDto(repository.findAllByStatus(status));
	}

	public Page<LedgerEntryDTO> readLedger(Pageable pageable) {
		return mapper.toDto(repository.findAllByStatus(Status.COMPLETED, pageable));
	}

	@SneakyThrows public Page<LedgerEntryDTO> readLedgerBetweenDates(String from, String to, String datePattern, Pageable pageable) {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		return mapper.toDto(repository.findAllByStatusAndDateBetweenOrderByDateAsc(Status.COMPLETED, sdf.parse(from), sdf.parse(to), pageable));
	}

	public LedgerEntryDTO readLedgerEntry(long id) {
		return mapper.toDto(repository.findById(id).orElseThrow(() -> new IllegalArgumentException("LedgerEntry not found")));
	}

	public LedgerEntryDTO updateLedgerEntry(long id, CreateLedgerEntryDTO ledgerEntryDTO) {
		LedgerEntryEntity ledgerEntry = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("LedgerEntry not found"));
		// Converto il DTO in oggetto
		ledgerEntry = mapper.toEntity(ledgerEntry, ledgerEntryDTO, ledgerEntryDTO.getBankId() != null ? bankService.getBank(ledgerEntryDTO.getBankId()) : null);
		// Salvo record su DB
		ledgerEntry = repository.save(ledgerEntry);
		// Converto l'entity del record in DTO
		return mapper.toDto(ledgerEntry);
	}

	public void deleteLedgerEntry(long id) {
		repository.deleteById(id);
	}

}
