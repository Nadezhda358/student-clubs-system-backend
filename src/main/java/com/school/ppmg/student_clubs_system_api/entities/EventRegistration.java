package com.school.ppmg.student_clubs_system_api.entities;

import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "event_registrations",
        indexes = {
                @Index(name = "ix_er_student_status", columnList = "student_user_id, status"),
                @Index(name = "ix_er_event_status", columnList = "event_id, status"),
                @Index(name = "ix_er_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE event_registrations SET deleted_at = CURRENT_TIMESTAMP(6) WHERE event_id = ? AND student_user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class EventRegistration extends AuditableEntity {

    @EmbeddedId
    private EventRegistrationId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("eventId")
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_er_event")
    )
    private Event event;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentUserId")
    @JoinColumn(
            name = "student_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_er_student_user")
    )
    private User student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @NotNull
    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;
}

