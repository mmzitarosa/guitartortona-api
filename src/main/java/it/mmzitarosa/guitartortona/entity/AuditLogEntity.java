package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Riga del registro di audit. Le modifiche ai campi le scrive {@code AuditEventListener} (JDBC); le scelte esplicite dell'utente (ALIGN/IGNORE) si salvano da qui. */
@Entity @Table(name = "audit_log")
@Getter @Setter
public class AuditLogEntity extends IdEntity {

	public static final String CREATE = "CREATE";
	public static final String UPDATE = "UPDATE";
	public static final String DELETE = "DELETE";
	public static final String ALIGN = "ALIGN";
	public static final String IGNORE = "IGNORE";

	@Column(name = "created_date") private LocalDateTime createdDate;
	private String username;
	@Column(name = "entity_type") private String entityType;
	@Column(name = "entity_id") private Long entityId;
	@Column(name = "product_id") private Long productId;
	private String operation;
	private String field;
	@Column(name = "old_value") private String oldValue;
	@Column(name = "new_value") private String newValue;

}
