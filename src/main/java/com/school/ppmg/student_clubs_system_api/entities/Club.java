package com.school.ppmg.student_clubs_system_api.entities;

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
        name = "clubs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_clubs_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "ix_clubs_created_by", columnList = "created_by"),
                @Index(name = "ix_clubs_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE clubs SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Club extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String name;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;

    // free text schedule (e.g. "Mon/Wed 16:00-18:00")
    @Size(max = 2000)
    @Column(name = "schedule_text", length = 2000)
    private String scheduleText;

    @Size(max = 80)
    @Column(length = 80)
    private String room;

    @Email
    @Size(max = 255)
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Size(max = 40)
    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Who created the club (teacher/admin). We’ll enforce role in service layer.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubs_created_by_users")
    )
    private User createdBy;

    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    private java.util.Set<ClubTeacher> teachers = new java.util.HashSet<>();

}
