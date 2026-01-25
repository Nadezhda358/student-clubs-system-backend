package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository
        extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByStudentIdAndEventId(Long studentId, Long eventId);

    boolean existsByStudentIdAndEventId(Long studentId, Long eventId);

    List<EventRegistration> findByEventId(Long eventId);

    List<EventRegistration> findByStudentId(Long studentId);

    long countByEventId(Long eventId);
}
