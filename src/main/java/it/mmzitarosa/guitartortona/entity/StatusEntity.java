package it.mmzitarosa.guitartortona.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static it.mmzitarosa.guitartortona.utils.Constant.Status;

@Getter @Setter @ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class StatusEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) protected Long id;

	@CreatedDate @Column(name = "created_date", updatable = false) protected LocalDateTime createdDate;
	@LastModifiedDate @Column(name = "updated_date") protected LocalDateTime updatedDate;
	@Enumerated(EnumType.ORDINAL) protected Status status; // DRAFT, PENDING COMPLETED, ARCHIVED

	public Long getDaysLeft() {
		if (status != Status.ARCHIVED) return null;
		return ChronoUnit.DAYS.between(LocalDateTime.now(), updatedDate.plusDays(30));
	}

}
