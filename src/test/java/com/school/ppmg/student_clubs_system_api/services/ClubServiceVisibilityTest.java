package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.ClubListDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ClubServiceVisibilityTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllHidesInactiveClubsFromAnonymousButShowsThemToTeachers() {
        User admin = createUser("Мария", "Николова", "admin-" + UUID.randomUUID() + "@example.com", UserRole.ADMIN);
        User teacher = createUser("Даниела", "Георгиева", "teacher-" + UUID.randomUUID() + "@example.com", UserRole.TEACHER);
        String marker = "видимост-" + UUID.randomUUID();

        Club activeClub = createClub(marker + "-активен", true, admin);
        Club inactiveClub = createClub(marker + "-неактивен", false, admin);

        SecurityContextHolder.clearContext();
        Page<ClubListDto> anonymousResult = clubService.getAll(null, marker, PageRequest.of(0, 10));

        assertThat(anonymousResult.getContent())
                .extracting(ClubListDto::id)
                .contains(activeClub.getId())
                .doesNotContain(inactiveClub.getId());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        teacher.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
                )
        );

        Page<ClubListDto> teacherResult = clubService.getAll(null, marker, PageRequest.of(0, 10));

        assertThat(teacherResult.getContent())
                .extracting(ClubListDto::id)
                .contains(activeClub.getId(), inactiveClub.getId());
    }

    private User createUser(String firstName, String lastName, String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("password-hash");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private Club createClub(String name, boolean active, User createdBy) {
        Club club = new Club();
        club.setName(name);
        club.setDescription("Описание за " + name);
        club.setIsActive(active);
        club.setCreatedBy(createdBy);
        return clubRepository.saveAndFlush(club);
    }
}
