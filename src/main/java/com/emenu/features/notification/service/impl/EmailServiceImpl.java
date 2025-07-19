package com.emenu.features.notification.service.impl;

import com.emenu.features.notification.service.EmailService;
import com.emenu.features.user_management.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.notifications.email.from}")
    private String fromEmail;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailEnabled;

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        sendEmail(to, subject, content, false);
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String content, boolean isHtml) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping email to: {}", to);
            return;
        }

        try {
            if (isHtml) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, true);
                mailSender.send(message);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
            }
            
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Object context) {
        if (!emailEnabled) {
            log.info("Email notifications disabled, skipping template email to: {}", to);
            return;
        }

        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("context", context);
            
            String htmlContent = templateEngine.process("email/" + templateName, thymeleafContext);
            sendEmail(to, subject, htmlContent, true);
        } catch (Exception e) {
            log.error("Failed to send template email to: {}", to, e);
        }
    }

    @Override
    public void sendEmailToUser(User user, String subject, String templateName, Object context) {
        if (user.canReceiveEmailNotifications()) {
            sendTemplateEmail(user.getEmail(), subject, templateName, context);
        } else {
            log.debug("User {} cannot receive email notifications", user.getEmail());
        }
    }
}
