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
public class ClubMembershipId implements Serializable {

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "student_user_id", nullable = false)
    private Long studentUserId;

    public ClubMembershipId(Long clubId, Long studentUserId) {
        this.clubId = clubId;
        this.studentUserId = studentUserId;
    }
}
