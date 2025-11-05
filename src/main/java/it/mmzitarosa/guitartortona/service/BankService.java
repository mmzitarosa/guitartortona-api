package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.mapper.BankMapper;
import it.mmzitarosa.guitartortona.repository.BankRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service public class BankService {

	/* == CONSTANTS == */
	private final BankRepository repository;
	private final BankMapper mapper;

	/* == SERVICE FIELDS == */
	private List<BankDTO> banksDTO;
	private Map<Long, BankEntity> banksMap;

	/* == CONSTRUCTOR == */
	public BankService(BankRepository repository, BankMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	/* == POST CONSTRUCTOR == */
	/*	  Load banks into memory at startup */
	@PostConstruct protected void loadBanks() {
		List<BankEntity> banks = repository.findAll();
		banksMap = banks.stream().collect(Collectors.toMap(BankEntity::getId, Function.identity()));
		banksDTO = mapper.toDto(banks);
	}

	/* == PUBLIC METHODS == */
	public List<BankDTO> readBanks() {
		return banksDTO;
	}

	/* == PACKAGE METHODS == */
	BankEntity getBank(long id) {
		return banksMap.get(id);
	}

}
