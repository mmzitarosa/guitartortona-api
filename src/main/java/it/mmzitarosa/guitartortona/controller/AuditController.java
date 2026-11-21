package it.mmzitarosa.guitartortona.controller;

import it.mmzitarosa.guitartortona.dto.PageDTO;
import it.mmzitarosa.guitartortona.dto.audit.AuditLogDTO;
import it.mmzitarosa.guitartortona.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Consultazione del registro di audit: riservata all'admin (vedi SecurityConfig). */
@RequiredArgsConstructor
@RestController @RequestMapping("/api/v1")
public class AuditController {

	private final AuditService service;

	@GetMapping("/audit") public ResponseEntity<PageDTO<AuditLogDTO>> search(
			@RequestParam(required = false) String username,
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) Long entityId,
			@RequestParam(required = false) String operation,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(service.search(username, entityType, entityId, operation, from, to, page, size));
	}

}
