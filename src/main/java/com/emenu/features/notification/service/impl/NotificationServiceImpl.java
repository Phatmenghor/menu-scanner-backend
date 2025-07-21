package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.NotificationChannel;
import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.dto.filter.NotificationFilterRequest;
import com.emenu.features.notification.dto.request.BulkNotificationRequest;
import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.dto.response.NotificationResponse;
import com.emenu.features.notification.mapper.NotificationMapper;
import com.emenu.features.notification.models.Notification;
import com.emenu.features.notification.repository.NotificationRepository;
import com.emenu.features.notification.service.EmailService;
import com.emenu.features.notification.service.MessageTemplateService;
import com.emenu.features.notification.service.NotificationService;
import com.emenu.features.notification.service.TelegramService;
import com.emenu.features.notification.specification.NotificationSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final TelegramService telegramService;
    private final MessageTemplateService templateService;
    private final SecurityUtils securityUtils;

    @Override
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        log.info("Creating notification for recipient: {}", request.getRecipientId());

        Notification notification = notificationMapper.toEntity(request);
        notification.setSenderId(securityUtils.getCurrentUserId());

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully: {}", savedNotification.getId());

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public NotificationResponse scheduleNotification(NotificationCreateRequest request) {
        log.info("Scheduling notification for: {}", request.getScheduledAt());

        Notification notification = notificationMapper.toEntity(request);
        notification.setSenderId(securityUtils.getCurrentUserId());

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification scheduled successfully: {}", savedNotification.getId());

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public void sendNotification(UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndIsDeletedFalse(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getIsSent()) {
            log.warn("Notification already sent: {}", notificationId);
            return;
        }

        try {
            switch (notification.getChannel()) {
                case EMAIL -> sendEmailNotification(notification);
                case TELEGRAM -> sendTelegramNotification(notification);
                case IN_APP -> handleInAppNotification(notification);
                default -> log.warn("Unsupported notification channel: {}", notification.getChannel());
            }

            notification.markAsSent();
            notificationRepository.save(notification);
            log.info("Notification sent successfully: {}", notificationId);

        } catch (Exception e) {
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            log.error("Failed to send notification: {}", notificationId, e);
        }
    }

    @Override
    public void sendPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications(LocalDateTime.now());
        
        for (Notification notification : pendingNotifications) {
            try {
                sendNotification(notification.getId());
            } catch (Exception e) {
                log.error("Failed to send pending notification: {}", notification.getId(), e);
            }
        }
        
        log.info("Processed {} pending notifications", pendingNotifications.size());
    }

    @Override
    public List<NotificationResponse> createBulkNotification(BulkNotificationRequest request) {
        log.info("Creating bulk notification");

        List<Notification> notifications = new ArrayList<>();
        List<UUID> recipientIds = determineRecipients(request);

        for (UUID recipientId : recipientIds) {
            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setSenderId(securityUtils.getCurrentUserId());
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            notification.setHtmlContent(request.getHtmlContent());
            notification.setChannel(request.getChannel());
            notification.setAlertType(request.getAlertType());
            notification.setScheduledAt(request.getScheduledAt());

            notifications.add(notification);
        }

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        log.info("Created {} bulk notifications", savedNotifications.size());

        return notificationMapper.toResponseList(savedNotifications);
    }

    @Override
    public void sendBulkNotifications(List<UUID> notificationIds) {
        for (UUID notificationId : notificationIds) {
            try {
                sendNotification(notificationId);
            } catch (Exception e) {
                log.error("Failed to send bulk notification: {}", notificationId, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<NotificationResponse> getNotifications(NotificationFilterRequest filter) {
        Specification<Notification> spec = NotificationSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Notification> notificationPage = notificationRepository.findAll(spec, pageable);
        return notificationMapper.toPaginationResponse(notificationPage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndIsDeletedFalse(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly) {
        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findByRecipientIdAndIsReadAndIsDeletedFalse(userId, false);
        } else {
            notifications = notificationRepository.findByRecipientIdAndIsDeletedFalse(userId, null).getContent();
        }
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(UUID userId) {
        return notificationRepository.countUnreadByRecipient(userId);
    }

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndIsDeletedFalse(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsRead();
        notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadAndIsDeletedFalse(userId, false);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }

        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for user: {}", userId);
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndIsDeletedFalse(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.softDelete();
        notificationRepository.save(notification);
        log.info("Notification deleted: {}", notificationId);
    }

    @Override
    public NotificationResponse sendTemplatedNotification(UUID recipientId, String templateName,
                                                          Map<String, String> variables, NotificationChannel channel) {
        try {
            TemplateName template = TemplateName.valueOf(templateName.toUpperCase());
            String processedContent = templateService.processTemplate(template, variables);
            String processedHtmlContent = templateService.processHtmlTemplate(template, variables);

            NotificationCreateRequest request = new NotificationCreateRequest();
            request.setRecipientId(recipientId);
            request.setTitle("System Notification");
            request.setContent(processedContent);
            request.setHtmlContent(processedHtmlContent);
            request.setChannel(channel);

            NotificationResponse notification = createNotification(request);
            sendNotification(notification.getId());

            return notification;
        } catch (Exception e) {
            log.error("Failed to send templated notification", e);
            throw new RuntimeException("Failed to send templated notification", e);
        }
    }

    @Override
    public void sendSubscriptionExpiryAlert(UUID businessId, int daysRemaining) {
        // Implementation for subscription expiry alert
        log.info("Sending subscription expiry alert for business: {}, days remaining: {}", businessId, daysRemaining);
        // Create and send notification
    }

    @Override
    public void sendSubscriptionExpiredAlert(UUID businessId) {
        // Implementation for subscription expired alert
        log.info("Sending subscription expired alert for business: {}", businessId);
        // Create and send notification
    }

    @Override
    public void sendPaymentReminderAlert(UUID businessId) {
        // Implementation for payment reminder
        log.info("Sending payment reminder alert for business: {}", businessId);
        // Create and send notification
    }

    @Override
    public void sendWelcomeNotification(UUID userId) {
        // Implementation for welcome notification
        log.info("Sending welcome notification for user: {}", userId);
        // Create and send notification
    }

    @Override
    public void sendPasswordResetNotification(UUID userId, String resetToken) {
        // Implementation for password reset notification
        log.info("Sending password reset notification for user: {}", userId);
        // Create and send notification
    }

    @Override
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotifications();
        
        for (Notification notification : failedNotifications) {
            try {
                sendNotification(notification.getId());
            } catch (Exception e) {
                log.error("Retry failed for notification: {}", notification.getId(), e);
            }
        }
        
        log.info("Retried {} failed notifications", failedNotifications.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotifications();
        return notificationMapper.toResponseList(failedNotifications);
    }

    // Private helper methods
    private void sendEmailNotification(Notification notification) {
        // Get recipient email (would fetch from user repository)
        String recipientEmail = "recipient@example.com"; // Placeholder
        
        emailService.sendEmail(recipientEmail, notification.getTitle(), 
                              notification.getContent(), notification.getHtmlContent());
    }

    private void sendTelegramNotification(Notification notification) {
        // Get recipient Telegram chat ID (would fetch from user repository)
        String chatId = "123456789"; // Placeholder
        
        telegramService.sendTelegramMessage(chatId, notification.getContent());
    }

    private void handleInAppNotification(Notification notification) {
        // In-app notifications are handled by just marking as sent
        // The frontend will query for unread notifications
        log.info("In-app notification created: {}", notification.getId());
    }

    private List<UUID> determineRecipients(BulkNotificationRequest request) {
        List<UUID> recipientIds = new ArrayList<>();

        // Add specific user IDs
        if (request.getSpecificUserIds() != null) {
            recipientIds.addAll(request.getSpecificUserIds());
        }

        // Add users by type (would query user repository)
        // This is simplified - in real implementation you'd query the database
        if (request.getUserTypes() != null) {
            // Query users by type and add their IDs
        }

        // Add users by business IDs
        if (request.getBusinessIds() != null) {
            // Query users by business and add their IDs
        }

        return recipientIds;
    }
}