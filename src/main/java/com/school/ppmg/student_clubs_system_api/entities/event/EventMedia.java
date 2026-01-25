package com.school.ppmg.student_clubs_system_api.entities.event;

import com.school.ppmg.student_clubs_system_api.entities.base.AuditableEntity;
import com.school.ppmg.student_clubs_system_api.enums.MediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(
        name = "event_media",
        indexes = {
                @Index(name = "ix_event_media_event", columnList = "event_id"),
                @Index(name = "ix_event_media_deleted_at", columnList = "deleted_at"),
                @Index(name = "ix_event_media_event_sort", columnList = "event_id, sort_order")
        }
)
@SQLDelete(sql = "UPDATE event_media SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class EventMedia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_media_event")
    )
    private Event event;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType type;

    @NotBlank
    @Size(max = 2048)
    @Column(nullable = false, length = 2048)
    private String url;

    @Size(max = 255)
    private String caption;

    @NotNull
    @Min(0)
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
