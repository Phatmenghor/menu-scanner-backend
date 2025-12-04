package com.emenu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    @Value("${swagger.default-token:}")
    private String defaultToken;

    @PostConstruct
    public void init() {
        if (!defaultToken.isEmpty()) {
            System.out.println("🔥 Swagger UI: Default JWT token loaded for development");
            System.out.println("   Token: " + defaultToken.substring(0, 20) + "...");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Custom Swagger UI with pre-filled token
        if (!defaultToken.isEmpty()) {
            registry.addResourceHandler("/swagger-ui/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        }
    }
}