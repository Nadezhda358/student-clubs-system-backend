package com.school.ppmg.student_clubs_system_api.entities;

import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "events",
        indexes = {
                @Index(name = "ix_events_club", columnList = "club_id"),
                @Index(name = "ix_events_start_at", columnList = "start_at"),
                @Index(name = "ix_events_status", columnList = "status"),
                @Index(name = "ix_events_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE events SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Event extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "club_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_club")
    )
    private Club club;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Size(max = 200)
    @Column(length = 200)
    private String location;

    // NULL => unlimited
    @Min(0)
    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "registration_deadline")
    private OffsetDateTime registrationDeadline;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_created_by_users")
    )
    private User createdBy;

    @AssertTrue(message = "endAt must be after startAt")
    public boolean isEndAfterStart() {
        return endAt == null || startAt == null || !endAt.isBefore(startAt);
    }

    @AssertTrue(message = "registrationDeadline must be on/before startAt")
    public boolean isDeadlineValid() {
        return registrationDeadline == null || startAt == null || !registrationDeadline.isAfter(startAt);
    }
}
