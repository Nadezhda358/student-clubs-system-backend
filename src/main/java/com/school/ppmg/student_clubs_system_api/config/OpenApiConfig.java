package com.school.ppmg.student_clubs_system_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";
    private static final String AUTH_PATH_PREFIX = "/api/auth";
    private static final String CLUBS_PATH_PREFIX = "/api/clubs";
    private static final String EVENTS_PATH_PREFIX = "/api/events";
    private static final String ANNOUNCEMENTS_PATH_PREFIX = "/api/announcements";

    @Bean
    public OpenAPI studentClubsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Clubs System API")
                        .description("API documentation for managing student clubs and events.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }

    @Bean
    public OpenApiCustomizer protectedOperationsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) -> {
                if (pathItem == null) {
                    return;
                }

                pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    if (!requiresAuthentication(path, httpMethod)) {
                        return;
                    }

                    if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
                        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
                    }
                });
            });
        };
    }

    private boolean requiresAuthentication(String path, PathItem.HttpMethod httpMethod) {
        if (path.startsWith(AUTH_PATH_PREFIX)) {
            return false;
        }

        return httpMethod != PathItem.HttpMethod.GET
                || (!path.startsWith(CLUBS_PATH_PREFIX)
                && !path.startsWith(EVENTS_PATH_PREFIX)
                && !path.startsWith(ANNOUNCEMENTS_PATH_PREFIX));
    }
}
