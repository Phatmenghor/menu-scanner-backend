package com.emenu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.name:E-Menu SaaS Platform}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Simple E-Menu Platform for Restaurant Management}")
    private String appDescription;

    // ✅ FIXED: Support both localhost and production URLs
    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appName + " API")
                        .description(appDescription)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("E-Menu Platform Support")
                                .email("support@emenu-platform.com")
                                .url("https://emenu-platform.com/support"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://emenu-platform.com/license")))
                // ✅ FIXED: Add multiple servers for different environments
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("http://152.42.219.13:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://152.42.219.13:" + serverPort)
                                .description("Development Server (HTTPS)")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT authentication token")));
    }
}