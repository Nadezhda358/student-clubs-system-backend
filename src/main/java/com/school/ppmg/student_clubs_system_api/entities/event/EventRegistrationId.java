package com.school.ppmg.student_clubs_system_api.entities.event;

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
public class EventRegistrationId implements Serializable {

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "student_user_id", nullable = false)
    private Long studentUserId;

    public EventRegistrationId(Long eventId, Long studentUserId) {
        this.eventId = eventId;
        this.studentUserId = studentUserId;
    }
}
