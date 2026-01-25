package com.school.ppmg.student_clubs_system_api.entities.club;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ClubTeacherId implements Serializable {

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "teacher_user_id", nullable = false)
    private Long teacherUserId;

    public ClubTeacherId(Long clubId, Long teacherUserId) {
        this.clubId = clubId;
        this.teacherUserId = teacherUserId;
    }
}
