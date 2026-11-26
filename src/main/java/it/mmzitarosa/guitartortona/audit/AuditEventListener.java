package it.mmzitarosa.guitartortona.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.mmzitarosa.guitartortona.entity.AuditLogEntity;
import it.mmzitarosa.guitartortona.entity.IdEntity;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.security.CurrentUser;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Registra creazioni, modifiche (un record per campo) ed eliminazioni di tutte le entità, nella stessa transazione
 * dell'operazione. La scrittura avviene via JDBC sulla stessa connessione, per non interferire con il flush di Hibernate.
 * Le variazioni fatte dai trigger del database (giacenze) non passano da Hibernate e non sono registrate.
 */
@Component @RequiredArgsConstructor
public class AuditEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	public static final String CREATE = "CREATE";
	public static final String UPDATE = "UPDATE";
	public static final String DELETE = "DELETE";

	// Campi gestiti automaticamente o dai trigger: non sono modifiche dell'utente
	private static final Set<String> IGNORED_FIELDS = Set.of("id", "createdDate", "updatedDate", "stock", "stockPending", "lastLetterIndex",
			// Dati identificativi del venditore (anagrafica e copia sulla ricevuta): non vanno duplicati nel registro
			"documentType", "documentNumber", "taxCode", "customerDocumentType", "customerDocumentNumber", "customerTaxCode");
	// Codice interno generato dopo l'inserimento: non è una modifica dell'utente
	private static final Set<String> GENERATED_FIELDS = Set.of("internalCode");

	private final EntityManagerFactory entityManagerFactory;
	private final JdbcTemplate jdbc;
	private final CurrentUser currentUser;
	private final ObjectMapper mapper;

	@PostConstruct void register() {
		EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
				.getServiceRegistry().getService(EventListenerRegistry.class);
		registry.appendListeners(EventType.POST_INSERT, this);
		registry.appendListeners(EventType.POST_UPDATE, this);
		registry.appendListeners(EventType.POST_DELETE, this);
	}

	@Override public void onPostInsert(PostInsertEvent event) {
		if (!isAudited(event.getEntity())) return;
		write(event.getEntity(), event.getPersister(), event.getId(), CREATE, null, null, snapshot(event.getPersister(), event.getState()));
	}

	@Override public void onPostUpdate(PostUpdateEvent event) {
		if (!isAudited(event.getEntity())) return;
		EntityPersister persister = event.getPersister();
		String[] names = persister.getPropertyNames();
		Object[] oldState = event.getOldState();
		Object[] state = event.getState();
		if (oldState == null) return;
		int[] dirty = event.getDirtyProperties();
		List<Integer> indexes = new ArrayList<>();
		if (dirty == null) for (int i = 0; i < names.length; i++) indexes.add(i);
		else for (int i : dirty) indexes.add(i);

		for (int i : indexes) {
			if (!isTracked(persister, i)) continue;
			String oldValue = format(oldState[i]);
			String newValue = format(state[i]);
			if (Objects.equals(oldValue, newValue)) continue;
			if (oldValue == null && GENERATED_FIELDS.contains(names[i])) continue;
			write(event.getEntity(), persister, event.getId(), UPDATE, names[i], oldValue, newValue);
		}
	}

	@Override public void onPostDelete(PostDeleteEvent event) {
		if (!isAudited(event.getEntity())) return;
		write(event.getEntity(), event.getPersister(), event.getId(), DELETE, null, snapshot(event.getPersister(), event.getDeletedState()), null);
	}

	@Override public boolean requiresPostCommitHandling(EntityPersister persister) {
		return false;
	}

	private boolean isAudited(Object entity) {
		return entity instanceof IdEntity && !(entity instanceof AuditLogEntity);
	}

	private boolean isTracked(EntityPersister persister, int index) {
		return !IGNORED_FIELDS.contains(persister.getPropertyNames()[index]) && !persister.getPropertyTypes()[index].isCollectionType();
	}

	private String snapshot(EntityPersister persister, Object[] state) {
		if (state == null) return null;
		Map<String, String> values = new LinkedHashMap<>();
		String[] names = persister.getPropertyNames();
		for (int i = 0; i < names.length; i++) {
			if (!isTracked(persister, i)) continue;
			String value = format(state[i]);
			if (value != null) values.put(names[i], value);
		}
		try {
			return mapper.writeValueAsString(values);
		} catch (JsonProcessingException e) {
			return values.toString();
		}
	}

	private String format(Object value) {
		if (value == null) return null;
		if (value instanceof IdEntity entity) return String.valueOf(entity.getId());
		return String.valueOf(value);
	}

	private void write(Object entity, EntityPersister persister, Object id, String operation, String field, String oldValue, String newValue) {
		jdbc.update("INSERT INTO audit_log (created_date, username, entity_type, entity_id, product_id, operation, field, old_value, new_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				Timestamp.valueOf(LocalDateTime.now()), currentUser.username(), entityType(persister), ((Number) id).longValue(), productId(entity, id),
				operation, field, oldValue, newValue);
	}

	/** Prodotto a cui si riferisce la riga, così la storia di un prodotto comprende anche acquisti e vendite. */
	private Long productId(Object entity, Object id) {
		if (entity instanceof ProductEntity) return ((Number) id).longValue();
		ProductEntity product = entity instanceof PurchaseItemEntity item ? item.getProduct() : entity instanceof SaleEntity sale ? sale.getProduct() : null;
		return product == null ? null : product.getId();
	}

	/** Nome dell'entità senza il suffisso "Entity" (es. Product, Sale, IncomingInvoice). */
	private String entityType(EntityPersister persister) {
		String name = persister.getEntityName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name.endsWith("Entity") ? name.substring(0, name.length() - "Entity".length()) : name;
	}

}
