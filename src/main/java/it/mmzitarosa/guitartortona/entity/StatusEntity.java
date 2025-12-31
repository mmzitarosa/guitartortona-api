package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

@Data @MappedSuperclass
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class StatusEntity extends DateEntity {

	@Column(name = "completed_date") protected LocalDateTime completedDate;
	@Column(name = "archived_date") protected LocalDateTime archivedDate;

	@Enumerated(EnumType.ORDINAL) protected Status status; // DRAFT, PENDING COMPLETED
	private boolean archived;

	@PreUpdate protected void onUpdate() {
		super.onUpdate();
		if (status == Status.COMPLETED && completedDate == null)
			completedDate = updatedDate;
		if (archived && archivedDate == null)
			archivedDate = updatedDate;
		else if (archivedDate != null)
			archivedDate = null;
	}

	@Transient public Integer getDaysLeft() {
		if (!archived) return null;
		return (int) ChronoUnit.DAYS.between(LocalDateTime.now(), updatedDate.plusDays(30));
	}

	@Transient public boolean isDraft() { return isStatus(Status.DRAFT); }
	@Transient public boolean isPending() { return isStatus(Status.PENDING); }
	@Transient public boolean isCompleted() { return isStatus(Status.COMPLETED); }
	@Transient private boolean isStatus(Status status) { return this.status != null && this.status == status; }

}
