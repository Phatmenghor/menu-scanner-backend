package com.emenu.features.notification.service;

import java.util.Map;

public interface EmailService {
    
    // Basic Email Sending
    void sendEmail(String to, String subject, String content);
    void sendEmail(String to, String subject, String content, String htmlContent);
    void sendEmailWithAttachments(String to, String subject, String content, Map<String, byte[]> attachments);
    
    // Template-based Email
    void sendTemplatedEmail(String to, String templateName, Map<String, String> variables);
    
    // Bulk Email
    void sendBulkEmail(java.util.List<String> recipients, String subject, String content);
    void sendBulkTemplatedEmail(java.util.List<String> recipients, String templateName, Map<String, String> variables);
    
    // Business Email Templates
    void sendSubscriptionExpiryEmail(String businessEmail, String businessName, int daysRemaining);
    void sendSubscriptionExpiredEmail(String businessEmail, String businessName);
    void sendPaymentConfirmationEmail(String businessEmail, String businessName, String paymentDetails);
    void sendWelcomeEmail(String userEmail, String userName);
    void sendPasswordResetEmail(String userEmail, String resetLink);
    
    // Email Verification
    boolean validateEmailAddress(String email);
    void sendEmailVerification(String email, String verificationCode);
}
