package com.menghor.ksit.scheduled;

import com.menghor.ksit.feature.setting.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    // Run once a day at midnight to process expired subscriptions
    @Scheduled(cron = "0 0 0 * * ?")
    public void processExpiredSubscriptions() {
        log.info("Starting scheduled task: Processing expired subscriptions");
        subscriptionService.processExpiredSubscriptions();
        log.info("Completed scheduled task: Processing expired subscriptions");
    }
    
    // Run once a day at 1 AM to send subscription expiration notifications
    // This would typically integrate with an email or notification service
    @Scheduled(cron = "0 0 1 * * ?")
    public void sendSubscriptionExpirationNotifications() {
        log.info("Starting scheduled task: Sending subscription expiration notifications");
        // Implementation would integrate with notification service
        log.info("Completed scheduled task: Sending subscription expiration notifications");
    }


}