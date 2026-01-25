package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByClubId(Long clubId);

    List<Event> findByStartAtAfter(LocalDateTime dateTime);

    List<Event> findByClubIdAndStartAtAfter(Long clubId, LocalDateTime dateTime);
}
