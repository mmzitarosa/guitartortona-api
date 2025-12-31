package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Data @MappedSuperclass
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class DateEntity extends IdEntity {

	@Column(name = "created_date", updatable = false) protected LocalDateTime createdDate;
	@Column(name = "updated_date") protected LocalDateTime updatedDate;

	@PrePersist protected void onCreate() {
		createdDate = LocalDateTime.now();
		updatedDate = createdDate;
	}
	@PreUpdate protected void onUpdate() {
		updatedDate = LocalDateTime.now();
	}

}
