package com.emenu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.name:E-Menu SaaS Platform}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Simple E-Menu Platform for Restaurant Management}")
    private String appDescription;

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    // Read default token from application.yml
    @Value("${swagger.default-token:}")
    private String defaultToken;

    @Bean
    public OpenAPI customOpenAPI() {
        String securityDescription = "JWT authentication token";
        
        // Add hint about default token if it exists
        if (!defaultToken.isEmpty()) {
            securityDescription += "\n\n🔥 **DEV MODE**: Token is auto-filled! Just click 'Authorize' → 'Authorize'";
        }

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
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(securityDescription)));
    }
}