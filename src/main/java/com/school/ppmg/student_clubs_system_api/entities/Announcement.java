package com.school.ppmg.student_clubs_system_api.entities;

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
        name = "announcements",
        indexes = {
                @Index(name = "ix_announcements_club", columnList = "club_id"),
                @Index(name = "ix_announcements_published", columnList = "club_id, is_published, published_at"),
                @Index(name = "ix_announcements_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE announcements SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Announcement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "club_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_announcements_club")
    )
    private Club club;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 8000)
    @Column(nullable = false, length = 8000)
    private String body;

    @NotNull
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_announcements_author")
    )
    private User author;

    @AssertTrue(message = "publishedAt must be set when isPublished is true")
    public boolean isPublishedAtValid() {
        return !Boolean.TRUE.equals(isPublished) || publishedAt != null;
    }
}
