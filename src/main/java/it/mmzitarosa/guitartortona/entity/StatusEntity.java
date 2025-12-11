package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

@Getter @Setter @ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class StatusEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) protected Long id;

	@Column(name = "created_date", updatable = false) protected LocalDateTime createdDate;
	@Column(name = "updated_date") protected LocalDateTime updatedDate;
	@Column(name = "completed_date") protected LocalDateTime completedDate;
	@Column(name = "archived_date") protected LocalDateTime archivedDate;

	@Enumerated(EnumType.ORDINAL) protected Status status; // DRAFT, PENDING COMPLETED
	private boolean archived;

	public Long getDaysLeft() {
		if (!archived) return null;
		return ChronoUnit.DAYS.between(LocalDateTime.now(), updatedDate.plusDays(30));
	}

	@PrePersist protected void onCreate() {
		createdDate = LocalDateTime.now();
		updatedDate = createdDate;
	}

	@PreUpdate protected void onUpdate() {
		updatedDate = LocalDateTime.now();
		if (status == Status.COMPLETED && completedDate == null)
			completedDate = updatedDate;
		if (archived && archivedDate == null)
			archivedDate = updatedDate;
	}

	public boolean isDraft() { return isStatus(Status.DRAFT); }
	public boolean isPending() { return isStatus(Status.PENDING); }
	public boolean isCompleted() { return isStatus(Status.COMPLETED); }
	private boolean isStatus(Status status) { return this.status != null && this.status == status; }

}
