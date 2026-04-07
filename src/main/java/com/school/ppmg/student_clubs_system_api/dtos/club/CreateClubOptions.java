package com.school.ppmg.student_clubs_system_api.dtos.club;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreateClubOptions(
        MultipartFile mainImage,
        List<MultipartFile> mediaFiles
) {}
