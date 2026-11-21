package it.mmzitarosa.guitartortona.service;

import it.mmzitarosa.guitartortona.dto.PageDTO;
import it.mmzitarosa.guitartortona.dto.audit.AuditLogDTO;
import it.mmzitarosa.guitartortona.entity.AuditLogEntity;
import it.mmzitarosa.guitartortona.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository repository;

	/** Consultazione per l'admin, con filtri opzionali. */
	@Transactional(readOnly = true)
	public PageDTO<AuditLogDTO> search(String username, String entityType, Long entityId, String operation, LocalDate from, LocalDate to, int page, int size) {
		Specification<AuditLogEntity> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (username != null && !username.isBlank()) predicates.add(cb.equal(root.get("username"), username));
			if (entityType != null && !entityType.isBlank()) predicates.add(cb.equal(root.get("entityType"), entityType));
			if (entityId != null) predicates.add(cb.equal(root.get("entityId"), entityId));
			if (operation != null && !operation.isBlank()) predicates.add(cb.equal(root.get("operation"), operation));
			if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), from.atStartOfDay()));
			if (to != null) predicates.add(cb.lessThan(root.get("createdDate"), to.plusDays(1).atStartOfDay()));
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
		return PageDTO.of(repository.findAll(spec, pageable).map(this::toDto));
	}

	private AuditLogDTO toDto(AuditLogEntity log) {
		return new AuditLogDTO(log.getId(), log.getCreatedDate(), log.getUsername(), log.getEntityType(), log.getEntityId(),
				log.getOperation(), log.getField(), log.getOldValue(), log.getNewValue());
	}

}
