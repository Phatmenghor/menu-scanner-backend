package com.emenu.features.setting.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("emailHealthIndicator")
@RequiredArgsConstructor
@Slf4j
public class EmailHealthIndicator implements HealthIndicator {

    private final JavaMailSender mailSender;

    @Override
    public Health health() {
        try {
            // Test email connection
            mailSender.createMimeMessage();
            return Health.up()
                    .withDetail("email", "Email service is available")
                    .build();
        } catch (Exception e) {
            log.error("Email health check failed", e);
            return Health.down()
                    .withDetail("email", "Email service is unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}