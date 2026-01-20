package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.event.EventMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventMediaRepository extends JpaRepository<EventMedia, Long> {

    List<EventMedia> findByEventId(Long eventId);
}
