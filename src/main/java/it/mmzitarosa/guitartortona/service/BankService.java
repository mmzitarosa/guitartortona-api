package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.bank.BankDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.repository.BankRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service @Transactional(readOnly = true)
public class BankService {

	private final BankRepository repository;

	private List<BankDTO> banksDTO;
	private Map<Long, BankEntity> banksMap;

	/**
	 * Load banks into memory at startup
	 */
	@PostConstruct protected void loadBanks() {
		List<BankEntity> banks = repository.findAll();
		banksMap = banks.stream().collect(Collectors.toMap(BankEntity::getId, Function.identity()));
		banksDTO = banks.stream().map(BankDTO::of).toList();
	}

	/**
	 * GET /banks
	 */
	public List<BankDTO> getAllBanks() {
		return banksDTO;
	}

	/**
	 * Helper: Recupera bank
	 */
	protected BankEntity getBank(long id) {
		return Optional.ofNullable(banksMap.get(id)).orElseThrow(() -> new RuntimeException("Bank not found: " + id));
	}
}
