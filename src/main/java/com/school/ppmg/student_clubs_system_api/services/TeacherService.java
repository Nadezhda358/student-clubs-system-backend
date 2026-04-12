package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.auth.UserDto;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserDto> getAllTeachers(String q, Pageable pageable) {
        Page<User> page = userRepository.findAll(
                teachersSpecification(normalizeQuery(q)),
                withDefaultSort(pageable, Sort.by(Sort.Direction.ASC, "firstName", "lastName", "id"))
        );

        return page.map(this::toUserDto);
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    private Specification<User> teachersSpecification(String q) {
        return (root, query, cb) -> {
            var rolePredicate = cb.equal(root.get("role"), UserRole.TEACHER);
            if (q == null) {
                return rolePredicate;
            }

            String like = "%" + q.toLowerCase() + "%";
            return cb.and(
                    rolePredicate,
                    cb.or(
                            cb.like(cb.lower(root.get("email")), like),
                            cb.like(cb.lower(root.get("firstName")), like),
                            cb.like(cb.lower(root.get("lastName")), like),
                            cb.like(
                                    cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                                    like
                            )
                    )
            );
        };
    }

    private Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable == null || pageable.isUnpaged() || pageable.getSort().isSorted()) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }

    private String normalizeQuery(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }
}
