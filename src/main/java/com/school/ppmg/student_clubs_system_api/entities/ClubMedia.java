package com.school.ppmg.student_clubs_system_api.entities;

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
        name = "club_media",
        indexes = {
                @Index(name = "ix_club_media_club", columnList = "club_id"),
                @Index(name = "ix_club_media_deleted_at", columnList = "deleted_at"),
                @Index(name = "ix_club_media_club_sort", columnList = "club_id, sort_order")
        }
)
@SQLDelete(sql = "UPDATE club_media SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClubMedia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "club_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_club_media_club")
    )
    private Club club;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType type;

    // URL or storage path (e.g. https://... or /uploads/...)
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
