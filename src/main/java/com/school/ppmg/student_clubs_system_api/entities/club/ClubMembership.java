package com.school.ppmg.student_clubs_system_api.entities.club;

import com.school.ppmg.student_clubs_system_api.entities.base.AuditableEntity;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
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
        name = "club_memberships",
        indexes = {
                @Index(name = "ix_cm_student_status", columnList = "student_user_id, status"),
                @Index(name = "ix_cm_club_status", columnList = "club_id, status"),
                @Index(name = "ix_cm_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE club_memberships SET deleted_at = CURRENT_TIMESTAMP(6) WHERE club_id = ? AND student_user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClubMembership extends AuditableEntity {

    @EmbeddedId
    private ClubMembershipId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("clubId")
    @JoinColumn(
            name = "club_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cm_club")
    )
    private Club club;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentUserId")
    @JoinColumn(
            name = "student_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cm_student_user")
    )
    private User student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    @Column(name = "left_at")
    private OffsetDateTime leftAt;
}
