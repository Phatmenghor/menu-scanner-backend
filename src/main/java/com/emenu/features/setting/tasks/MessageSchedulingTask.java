package com.emenu.features.setting.tasks;

import com.emenu.features.notification.service.AlertService;
import com.emenu.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSchedulingTask {

    private final AlertService alertService;
    private final NotificationService notificationService;

    /**
     * Check subscription expiry daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Phnom_Penh")
    public void checkSubscriptionExpiry() {
        log.info("Starting daily subscription expiry check");
        try {
            alertService.checkAndSendSubscriptionAlerts();
        } catch (Exception e) {
            log.error("Error in subscription expiry check", e);
        }
    }

    /**
     * Send payment reminders daily at 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Phnom_Penh")
    public void sendPaymentReminders() {
        log.info("Starting daily payment reminders");
        try {
            alertService.checkAndSendPaymentAlerts();
        } catch (Exception e) {
            log.error("Error in payment reminders", e);
        }
    }

    /**
     * Process pending notifications every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processPendingNotifications() {
        try {
            notificationService.sendPendingNotifications();
        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
        }
    }

    /**
     * Retry failed notifications every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void retryFailedNotifications() {
        try {
            notificationService.retryFailedNotifications();
        } catch (Exception e) {
            log.error("Error retrying failed notifications", e);
        }
    }

    /**
     * Weekly cleanup of old data every Sunday at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN", zone = "Asia/Phnom_Penh")
    public void weeklyCleanup() {
        log.info("Starting weekly cleanup");
        try {
            // Cleanup old notifications, messages, etc.
            // This would be implemented based on retention policies
            log.info("Weekly cleanup completed");
        } catch (Exception e) {
            log.error("Error in weekly cleanup", e);
        }
    }
}