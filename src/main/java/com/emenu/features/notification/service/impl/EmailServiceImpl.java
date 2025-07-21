package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.service.EmailService;
import com.emenu.features.notification.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MessageTemplateService templateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendEmail(String to, String subject, String content, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, htmlContent);

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendEmailWithAttachments(String to, String subject, String content, Map<String, byte[]> attachments) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);

            // Add attachments
            for (Map.Entry<String, byte[]> attachment : attachments.entrySet()) {
                helper.addAttachment(attachment.getKey(), () -> new java.io.ByteArrayInputStream(attachment.getValue()));
            }

            mailSender.send(mimeMessage);
            log.info("Email with attachments sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachments to: {}", to, e);
            throw new RuntimeException("Failed to send email with attachments", e);
        }
    }

    @Override
    public void sendTemplatedEmail(String to, String templateName, Map<String, String> variables) {
        try {
            TemplateName template = TemplateName.valueOf(templateName.toUpperCase());
            String content = templateService.processTemplate(template, variables);
            String htmlContent = templateService.processHtmlTemplate(template, variables);

            // Get template subject (simplified - would fetch from template)
            String subject = "Notification from " + appName;

            sendEmail(to, subject, content, htmlContent);
            log.info("Templated email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send templated email to: {}", to, e);
            throw new RuntimeException("Failed to send templated email", e);
        }
    }

    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String content) {
        for (String recipient : recipients) {
            try {
                sendEmail(recipient, subject, content);
            } catch (Exception e) {
                log.error("Failed to send bulk email to: {}", recipient, e);
            }
        }
        log.info("Bulk email sent to {} recipients", recipients.size());
    }

    @Override
    public void sendBulkTemplatedEmail(List<String> recipients, String templateName, Map<String, String> variables) {
        for (String recipient : recipients) {
            try {
                sendTemplatedEmail(recipient, templateName, variables);
            } catch (Exception e) {
                log.error("Failed to send bulk templated email to: {}", recipient, e);
            }
        }
        log.info("Bulk templated email sent to {} recipients", recipients.size());
    }

    @Override
    public void sendSubscriptionExpiryEmail(String businessEmail, String businessName, int daysRemaining) {
        String subject = "Your subscription is expiring soon - " + appName;
        String content = String.format(
                "Dear %s,\n\n" +
                        "Your subscription will expire in %d days. Please renew to continue using our services.\n\n" +
                        "Best regards,\n%s Team",
                businessName, daysRemaining, appName
        );

        sendEmail(businessEmail, subject, content);
    }

    @Override
    public void sendSubscriptionExpiredEmail(String businessEmail, String businessName) {
        String subject = "Your subscription has expired - " + appName;
        String content = String.format(
                "Dear %s,\n\n" +
                        "Your subscription has expired. Please renew immediately to restore access to our services.\n\n" +
                        "Best regards,\n%s Team",
                businessName, appName
        );

        sendEmail(businessEmail, subject, content);
    }

    @Override
    public void sendPaymentConfirmationEmail(String businessEmail, String businessName, String paymentDetails) {
        String subject = "Payment confirmation - " + appName;
        String content = String.format(
                "Dear %s,\n\n" +
                        "We have received your payment successfully.\n\n" +
                        "Payment Details:\n%s\n\n" +
                        "Thank you for your business!\n\n" +
                        "Best regards,\n%s Team",
                businessName, paymentDetails, appName
        );

        sendEmail(businessEmail, subject, content);
    }

    @Override
    public void sendWelcomeEmail(String userEmail, String userName) {
        String subject = "Welcome to " + appName;
        String content = String.format(
                "Dear %s,\n\n" +
                        "Welcome to %s! We're excited to have you on board.\n\n" +
                        "Get started by exploring our features and setting up your account.\n\n" +
                        "Best regards,\n%s Team",
                userName, appName, appName
        );

        sendEmail(userEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String userEmail, String resetLink) {
        String subject = "Password reset request - " + appName;
        String content = String.format(
                "You have requested a password reset for your %s account.\n\n" +
                        "Click the following link to reset your password:\n%s\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "Best regards,\n%s Team",
                appName, resetLink, appName
        );

        sendEmail(userEmail, subject, content);
    }

    @Override
    public boolean validateEmailAddress(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public void sendEmailVerification(String email, String verificationCode) {
        String subject = "Email verification - " + appName;
        String content = String.format(
                "Please verify your email address by using the following code:\n\n" +
                        "Verification Code: %s\n\n" +
                        "This code will expire in 24 hours.\n\n" +
                        "Best regards,\n%s Team",
                verificationCode, appName
        );

        sendEmail(email, subject, content);
    }
}