package com.emenu.features.notification.service.impl;

import com.emenu.features.auth.models.User;
import com.emenu.features.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.notifications.email.from:noreply@emenu-platform.com}")
    private String fromEmail;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailEnabled;

    @Override
    public void sendWelcomeEmail(User user) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", user.getFirstName());
            variables.put("lastName", user.getLastName());
            variables.put("userType", user.getUserType().getDescription());
            variables.put("companyName", "E-Menu Platform");

            sendCustomEmail(user.getEmail(), "Welcome to E-Menu Platform!", "welcome", variables);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", user.getFirstName());
            variables.put("verificationUrl", "http://localhost:8080/api/v1/auth/verify-email?token=" + verificationToken);
            variables.put("expiryHours", "24");

            sendCustomEmail(user.getEmail(), "Verify Your Email Address", "email-verification", variables);
            log.info("Email verification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", user.getFirstName());
            variables.put("resetUrl", "http://localhost:8080/api/v1/auth/reset-password?token=" + resetToken);
            variables.put("expiryHours", "1");

            sendCustomEmail(user.getEmail(), "Reset Your Password", "password-reset", variables);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendTierUpgradeEmail(User user, String oldTier, String newTier) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", user.getFirstName());
            variables.put("oldTier", oldTier);
            variables.put("newTier", newTier);
            variables.put("loyaltyPoints", user.getLoyaltyPoints());
            variables.put("benefits", getTierBenefits(newTier));

            sendCustomEmail(user.getEmail(), "Tier Upgrade Congratulations!", "tier-upgrade", variables);
            log.info("Tier upgrade email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send tier upgrade email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendSubscriptionWelcomeEmail(User user, String planName) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", user.getFirstName());
            variables.put("planName", planName);
            variables.put("companyName", "E-Menu Platform");

            sendCustomEmail(user.getEmail(), "Welcome to " + planName + "!", "subscription-welcome", variables);
            log.info("Subscription welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send subscription welcome email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendSubscriptionExpirationEmail(User user, String planName, String expirationDate) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", user.getFirstName());
            variables.put("planName", planName);
            variables.put("expirationDate", expirationDate);

            sendCustomEmail(user.getEmail(), "Your Subscription is Expiring", "subscription-expiration", variables);
            log.info("Subscription expiration email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send subscription expiration email to: {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendCustomEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("context", variables);
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendPlainEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.debug("Plain email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send plain email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String getTierBenefits(String tier) {
        return switch (tier.toUpperCase()) {
            case "SILVER" -> "2% discount on orders, 1.05x points multiplier";
            case "GOLD" -> "5% discount on orders, 1.1x points multiplier";
            case "PLATINUM" -> "8% discount on orders, 1.15x points multiplier";
            case "VIP" -> "10% discount on orders, 1.2x points multiplier, priority support";
            default -> "Standard benefits";
        };
    }
}