package com.school.ppmg.student_clubs_system_api.entities;

import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
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
        name = "club_membership_requests",
        uniqueConstraints = {
                // MariaDB няма partial unique index, затова: 1 заявка за даден статус
                @UniqueConstraint(
                        name = "uq_cmr_club_student_status",
                        columnNames = {"club_id", "student_user_id", "status"}
                )
        },
        indexes = {
                @Index(name = "ix_cmr_club_status", columnList = "club_id, status"),
                @Index(name = "ix_cmr_student_status", columnList = "student_user_id, status"),
                @Index(name = "ix_cmr_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE club_membership_requests SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClubMembershipRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "club_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cmr_club")
    )
    private Club club;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cmr_student_user")
    )
    private User student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipRequestStatus status = MembershipRequestStatus.PENDING;

    @Size(max = 2000)
    @Column(length = 2000)
    private String message;

    // кой е одобрил/отказал (teacher/admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "decided_by",
            foreignKey = @ForeignKey(name = "fk_cmr_decided_by_user")
    )
    private User decidedBy;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @Size(max = 2000)
    @Column(name = "decision_note", length = 2000)
    private String decisionNote;
}
