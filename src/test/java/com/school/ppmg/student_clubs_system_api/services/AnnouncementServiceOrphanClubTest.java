package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.announcement.AnnouncementDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AnnouncementServiceOrphanClubTest {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getAdminAnnouncementsSkipsAnnouncementsWhoseClubIsMissing() {
        User admin = createAdmin();
        Club club = createActiveClub(admin);
        String validTitle = "valid-announcement-" + UUID.randomUUID();
        String orphanTitle = "orphan-announcement-" + UUID.randomUUID();

        insertAnnouncement(club.getId(), admin.getId(), validTitle);
        insertOrphanAnnouncement(club.getId() + 1_000_000L, admin.getId(), orphanTitle);
        entityManager.flush();
        entityManager.clear();

        try {
            authenticateAs(admin);

            Page<AnnouncementDto> page = announcementService.getAdminAnnouncements(
                    null,
                    null,
                    null,
                    null,
                    null,
                    PageRequest.of(0, 20)
            );

            assertThat(page.getContent()).extracting(AnnouncementDto::title)
                    .contains(validTitle)
                    .doesNotContain(orphanTitle);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void insertAnnouncement(Long clubId, Long authorId, String title) {
        entityManager.createNativeQuery("""
                insert into announcements
                    (club_id, title, body, is_published, published_at, author_id, created_at, updated_at, deleted_at)
                values
                    (:clubId, :title, 'Regression test announcement', true, CURRENT_TIMESTAMP(6), :authorId, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), null)
                """)
                .setParameter("clubId", clubId)
                .setParameter("title", title)
                .setParameter("authorId", authorId)
                .executeUpdate();
    }

    private void insertOrphanAnnouncement(Long clubId, Long authorId, String title) {
        entityManager.createNativeQuery("set foreign_key_checks = 0").executeUpdate();
        try {
            insertAnnouncement(clubId, authorId, title);
        } finally {
            entityManager.createNativeQuery("set foreign_key_checks = 1").executeUpdate();
        }
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())
        );
    }

    private Club createActiveClub(User creator) {
        Club club = new Club();
        club.setName("Announcement test club " + UUID.randomUUID());
        club.setDescription("Announcement orphan club regression test");
        club.setIsActive(true);
        club.setCreatedBy(creator);
        return clubRepository.saveAndFlush(club);
    }

    private User createAdmin() {
        String suffix = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("announcement-admin-" + suffix + "@example.com");
        user.setPasswordHash("password-hash");
        user.setFirstName("Announcement");
        user.setLastName("Admin");
        user.setRole(UserRole.ADMIN);

        return userRepository.saveAndFlush(user);
    }
}
