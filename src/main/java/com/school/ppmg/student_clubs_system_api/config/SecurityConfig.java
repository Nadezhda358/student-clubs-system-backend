package com.school.ppmg.student_clubs_system_api.config;

import com.school.ppmg.student_clubs_system_api.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                ))
                .authorizeHttpRequests(auth -> auth

                        // Swagger endpoints
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/api/clubs/**",
                                "/api/events/**",
                                "/api/announcements/**"
                        ).permitAll()

                        // Club creation - ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/clubs")
                        .hasRole("ADMIN")

                        // Club update - ADMIN only
                        .requestMatchers(HttpMethod.PUT, "/api/clubs/**")
                        .hasRole("ADMIN")

                        // Club delete - ADMIN only
                        .requestMatchers(HttpMethod.DELETE, "/api/clubs/**")
                        .hasRole("ADMIN")

                        // Upload main image - ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/clubs/*/main-image")
                        .hasRole("ADMIN")

                        // Teacher endpoints
                        .requestMatchers("/api/teacher/**")
                        .hasRole("TEACHER")

                        // Admin endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
