package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.announcement.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByClubId(Long clubId);

    List<Announcement> findByAuthor(Long userId);

    List<Announcement> findByClubIdOrderByPublishedAt(Long clubId);
}
