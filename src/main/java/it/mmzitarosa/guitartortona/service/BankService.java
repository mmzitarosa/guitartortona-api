package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.BankDTO;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.entity.BankEntity;
import it.mmzitarosa.guitartortona.mapper.BankMapper;
import it.mmzitarosa.guitartortona.mapper.SupplierMapper;
import it.mmzitarosa.guitartortona.repository.BankRepository;
import it.mmzitarosa.guitartortona.repository.SupplierRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service public class BankService {

	private final BankRepository bankRepository;
	private final BankMapper bankMapper;
	private List<BankEntity> banks;
	private Map<Long, BankEntity> banksMap;

	public BankService(BankRepository bankRepository, BankMapper bankMapper) {
		this.bankRepository = bankRepository;
		this.bankMapper = bankMapper;
	}

	@PostConstruct protected void loadBanks() {
		readBanks();
	}

	public List<BankDTO> readBanks() {
		if (banks == null || banksMap == null) {
			banks = bankRepository.findAll();
			banksMap = banks.stream().collect(Collectors.toMap(BankEntity::getId, Function.identity()));
		}
		return bankMapper.toDto(banks);
	}

	public BankEntity getBank(long id) {
		return banksMap.get(id);
	}

}
