package com.menghor.ksit.config;

import com.menghor.ksit.feature.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Run after role and user initialization
public class DefaultSurveyInitializer implements CommandLineRunner {

    private final SurveyService surveyService;

    @Override
    public void run(String... args) {
        log.info("Initializing default survey...");
        try {
            surveyService.initializeDefaultSurvey();
            log.info("Default survey initialization completed");
        } catch (Exception e) {
            log.warn("Could not initialize default survey: {}", e.getMessage());
            // Don't fail the application startup if survey creation fails
        }
    }
}