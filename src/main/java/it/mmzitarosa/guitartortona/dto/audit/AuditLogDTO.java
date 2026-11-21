package it.mmzitarosa.guitartortona.dto.audit;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** Riga del registro di audit: per le modifiche {@code field} è il campo cambiato, per creazioni ed eliminazioni è nullo e il valore è un'istantanea JSON. */
public record AuditLogDTO(
		Long id,
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime date,
		String username,
		String entityType,
		Long entityId,
		String operation,
		String field,
		String oldValue,
		String newValue
) {}
