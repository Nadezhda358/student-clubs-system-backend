package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistration;
import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistrationId;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRegistrationRepository
        extends JpaRepository<EventRegistration, EventRegistrationId>, JpaSpecificationExecutor<EventRegistration> {

    long countByEvent_IdAndStatus(Long eventId, RegistrationStatus status);
}
