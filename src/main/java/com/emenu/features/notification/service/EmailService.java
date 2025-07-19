package com.emenu.features.notification.service;

import com.emenu.features.user_management.domain.User;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendEmail(String to, String subject, String content, boolean isHtml);
    void sendTemplateEmail(String to, String subject, String templateName, Object context);
    void sendEmailToUser(User user, String subject, String templateName, Object context);
}