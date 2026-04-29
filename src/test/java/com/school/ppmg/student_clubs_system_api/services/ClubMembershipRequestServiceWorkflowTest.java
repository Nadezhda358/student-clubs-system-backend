package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.CreateMembershipApplicationRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.MembershipApplicationDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembership;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ClubMembershipRequestServiceWorkflowTest {

    @Autowired
    private ClubMembershipRequestService clubMembershipRequestService;

    @Autowired
    private ClubMembershipRepository clubMembershipRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminApprovalOfPendingApplicationCreatesActiveMembership() {
        User admin = createUser("Администратор", "Тестов", "admin-" + UUID.randomUUID() + "@example.com", UserRole.ADMIN);
        User student = createUser("Иван", "Колев", "student-" + UUID.randomUUID() + "@example.com", UserRole.STUDENT);
        Club club = createClub("Клуб-" + UUID.randomUUID(), admin);

        authenticate(student);
        MembershipApplicationDto created = clubMembershipRequestService.apply(
                club.getId(),
                new CreateMembershipApplicationRequest("Искам да участвам редовно в дейностите на клуба.")
        );

        assertThat(created.status()).isEqualTo(MembershipRequestStatus.PENDING);

        authenticate(admin);
        MembershipApplicationDto approved = clubMembershipRequestService.adminUpdateStatus(
                created.id(),
                MembershipRequestStatus.APPROVED
        );

        assertThat(approved.status()).isEqualTo(MembershipRequestStatus.APPROVED);

        ClubMembership membership = clubMembershipRepository.findByStudent_IdAndClub_Id(student.getId(), club.getId())
                .orElseThrow();
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.getJoinedAt()).isNotNull();
        assertThat(membership.getLeftAt()).isNull();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())
        );
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

    private Club createClub(String name, User createdBy) {
        Club club = new Club();
        club.setName(name);
        club.setDescription("Описание за " + name);
        club.setIsActive(true);
        club.setCreatedBy(createdBy);
        return clubRepository.saveAndFlush(club);
    }
}
