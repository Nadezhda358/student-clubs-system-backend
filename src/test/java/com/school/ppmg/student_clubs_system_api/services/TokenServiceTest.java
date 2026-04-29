package com.school.ppmg.student_clubs_system_api.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    void generateTokenReturnsUrlSafeRandomValue() {
        String first = tokenService.generateToken();
        String second = tokenService.generateToken();

        assertThat(first)
                .hasSize(43)
                .matches("^[A-Za-z0-9_-]+$");
        assertThat(second)
                .hasSize(43)
                .matches("^[A-Za-z0-9_-]+$");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void hashTokenUsesSha256() {
        assertThat(tokenService.hashToken("abc123"))
                .isEqualTo("6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090");
    }
}
