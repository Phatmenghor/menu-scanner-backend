package com.emenu.features.setting.tasks;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.UserType;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.CustomerTierService;
import com.emenu.features.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackgroundTaskService {

    private final UserRepository userRepository;
    private final CustomerTierService customerTierService;
    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    @Transactional
    public void processCustomerTierUpdates() {
        log.info("Starting customer tier update process");
        
        List<User> customers = userRepository.findByUserTypeInAndIsDeletedFalse(
            UserType.CUSTOMER
        );
        
        int updated = 0;
        for (User customer : customers) {
            try {
                customerTierService.updateCustomerTier(customer);
                updated++;
            } catch (Exception e) {
                log.error("Failed to update tier for customer {}: {}", customer.getEmail(), e.getMessage());
            }
        }
        
        log.info("Customer tier update process completed. Updated: {}", updated);
    }

    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3 AM
    public void processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");
        subscriptionService.processExpiredSubscriptions();
    }

    @Scheduled(cron = "0 0 10 * * ?") // Daily at 10 AM
    public void sendExpirationNotifications() {
        log.info("Sending subscription expiration notifications");
        subscriptionService.sendExpirationNotifications();
    }

    @Scheduled(cron = "0 0 4 * * ?") // Daily at 4 AM
    @Transactional
    public void unlockExpiredAccounts() {
        log.info("Starting account unlock process");
        
        List<User> lockedUsers = userRepository.findByAccountStatusAndIsDeletedFalse(
            AccountStatus.LOCKED
        );
        
        int unlocked = 0;
        for (User user : lockedUsers) {
            if (!user.isAccountLocked()) { // Check if lock period has expired
                user.setAccountStatus(com.emenu.enums.AccountStatus.ACTIVE);
                userRepository.save(user);
                unlocked++;
                log.info("Auto-unlocked account for user: {}", user.getEmail());
            }
        }
        
        log.info("Account unlock process completed. Unlocked: {}", unlocked);
    }

    @Scheduled(cron = "0 0 0 * * SUN") // Weekly on Sunday at midnight
    @Transactional
    public void cleanupOldData() {
        log.info("Starting weekly data cleanup");
        
        // Clean up old audit logs (keep last 90 days)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        
        // This would be implemented based on specific cleanup requirements
        log.info("Weekly data cleanup completed");
    }
}