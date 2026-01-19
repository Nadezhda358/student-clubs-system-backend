package com.school.ppmg.student_clubs_system_api.entities.club;

import com.school.ppmg.student_clubs_system_api.entities.base.AuditableEntity;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(
        name = "club_teachers",
        indexes = {
                @Index(name = "ix_club_teachers_teacher", columnList = "teacher_user_id"),
                @Index(name = "ix_club_teachers_deleted_at", columnList = "deleted_at")
        }
)
@SQLDelete(sql = "UPDATE club_teachers SET deleted_at = CURRENT_TIMESTAMP(6) WHERE club_id = ? AND teacher_user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClubTeacher extends AuditableEntity {

    @EmbeddedId
    private ClubTeacherId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("clubId")
    @JoinColumn(
            name = "club_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_club_teachers_club")
    )
    private Club club;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("teacherUserId")
    @JoinColumn(
            name = "teacher_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_club_teachers_teacher_user")
    )
    private User teacher;

    @NotNull
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = true;
}
