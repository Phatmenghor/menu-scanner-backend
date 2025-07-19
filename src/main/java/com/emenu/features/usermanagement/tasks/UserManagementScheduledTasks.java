import com.emenu.enums.AccountStatus;
import com.emenu.enums.CustomerTier;
import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserManagementScheduledTasks {

    private final UserRepository userRepository;

    /**
     * Unlock users whose lock period has expired
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void unlockExpiredAccounts() {
        log.debug("Running scheduled task: unlockExpiredAccounts");

        LocalDateTime now = LocalDateTime.now();
        List<User> usersToUnlock = userRepository.findUsersToUnlock(now);

        if (!usersToUnlock.isEmpty()) {
            log.info("Unlocking {} expired locked accounts", usersToUnlock.size());

            usersToUnlock.forEach(user -> {
                user.setAccountStatus(AccountStatus.ACTIVE);
                user.setAccountLockedUntil(null);
                user.setLoginAttempts(0);
                log.info("Unlocked account: {}", user.getEmail());
            });

            userRepository.saveAll(usersToUnlock);
            log.info("Successfully unlocked {} accounts", usersToUnlock.size());
        }
    }

    /**
     * Clean up unverified users older than 7 days
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.info("Running scheduled task: cleanupUnverifiedUsers");

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<User> unverifiedUsers = userRepository.findUnverifiedUsersOlderThan(threshold);

        if (!unverifiedUsers.isEmpty()) {
            log.info("Cleaning up {} unverified users older than 7 days", unverifiedUsers.size());

            unverifiedUsers.forEach(user -> {
                user.softDelete();
                user.setDeletedBy("SYSTEM_CLEANUP");
                log.debug("Soft deleted unverified user: {}", user.getEmail());
            });

            userRepository.saveAll(unverifiedUsers);
            log.info("Successfully cleaned up {} unverified users", unverifiedUsers.size());
        }
    }

    /**
     * Send subscription expiry notifications
     * Runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendSubscriptionExpiryNotifications() {
        log.info("Running scheduled task: sendSubscriptionExpiryNotifications");

        // Find subscriptions expiring in 30 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysFromNow = now.plusDays(30);
        List<User> subscriptionsExpiring30Days = userRepository.findBySubscriptionEndsBetween(now, thirtyDaysFromNow);

        // Find subscriptions expiring in 7 days
        LocalDateTime sevenDaysFromNow = now.plusDays(7);
        List<User> subscriptionsExpiring7Days = userRepository.findBySubscriptionEndsBetween(now, sevenDaysFromNow);

        // Find subscriptions expiring in 1 day
        LocalDateTime oneDayFromNow = now.plusDays(1);
        List<User> subscriptionsExpiring1Day = userRepository.findBySubscriptionEndsBetween(now, oneDayFromNow);

        // TODO: Implement notification service calls
        if (!subscriptionsExpiring30Days.isEmpty()) {
            log.info("Found {} subscriptions expiring in 30 days", subscriptionsExpiring30Days.size());
            // sendSubscriptionExpiryNotification(subscriptionsExpiring30Days, 30);
        }

        if (!subscriptionsExpiring7Days.isEmpty()) {
            log.info("Found {} subscriptions expiring in 7 days", subscriptionsExpiring7Days.size());
            // sendSubscriptionExpiryNotification(subscriptionsExpiring7Days, 7);
        }

        if (!subscriptionsExpiring1Day.isEmpty()) {
            log.info("Found {} subscriptions expiring in 1 day", subscriptionsExpiring1Day.size());
            // sendSubscriptionExpiryNotification(subscriptionsExpiring1Day, 1);
        }
    }

    /**
     * Update customer tiers based on loyalty points
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateCustomerTiers() {
        log.info("Running scheduled task: updateCustomerTiers");

        List<User> customers = userRepository.findCustomerUsers();
        int updatedCount = 0;

        for (User customer : customers) {
            if (customer.getLoyaltyPoints() != null) {
                CustomerTier newTier = CustomerTier.fromPoints(customer.getLoyaltyPoints());
                if (!newTier.equals(customer.getCustomerTier())) {
                    CustomerTier oldTier = customer.getCustomerTier();
                    customer.setCustomerTier(newTier);
                    updatedCount++;

                    log.info("Updated customer tier for {}: {} -> {}",
                            customer.getEmail(), oldTier, newTier);

                    // TODO: Send tier upgrade notification
                    // sendTierUpgradeNotification(customer, oldTier, newTier);
                }
            }
        }

        if (updatedCount > 0) {
            userRepository.saveAll(customers);
            log.info("Updated customer tiers for {} users", updatedCount);
        }
    }

    /**
     * Generate user activity reports
     * Runs weekly on Sunday at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * SUN")
    @Transactional(readOnly = true)
    public void generateWeeklyUserActivityReport() {
        log.info("Running scheduled task: generateWeeklyUserActivityReport");

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Count active users in the last week
        List<User> activeUsers = userRepository.findByLastActiveBetween(weekAgo, LocalDateTime.now());

        // Count new registrations
        long newRegistrations = userRepository.countByCreatedAtBetween(weekAgo, LocalDateTime.now());

        // Count locked accounts
        long lockedAccounts = userRepository.countByAccountStatusAndIsDeletedFalse(AccountStatus.LOCKED);

        // Count inactive users (not active for 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<User> inactiveUsers = userRepository.findUsersInactiveForDays(thirtyDaysAgo);

        log.info("Weekly User Activity Report:");
        log.info("  - Active users (last 7 days): {}", activeUsers.size());
        log.info("  - New registrations (last 7 days): {}", newRegistrations);
        log.info("  - Currently locked accounts: {}", lockedAccounts);
        log.info("  - Inactive users (30+ days): {}", inactiveUsers.size());

        // TODO: Send report to platform administrators
        // sendWeeklyActivityReport(activeUsers.size(), newRegistrations, lockedAccounts, inactiveUsers.size());
    }

    /**
     * Clean up expired tokens
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        log.debug("Running scheduled task: cleanupExpiredTokens");

        LocalDateTime now = LocalDateTime.now();
        List<User> users = userRepository.findAll();
        int cleanedCount = 0;

        for (User user : users) {
            boolean updated = false;

            // Clean up expired email verification tokens
            if (user.getEmailVerificationToken() != null &&
                    user.getEmailVerificationExpires() != null &&
                    user.getEmailVerificationExpires().isBefore(now)) {
                user.setEmailVerificationToken(null);
                user.setEmailVerificationExpires(null);
                updated = true;
            }

            // Clean up expired phone verification tokens
            if (user.getPhoneVerificationToken() != null &&
                    user.getPhoneVerificationExpires() != null &&
                    user.getPhoneVerificationExpires().isBefore(now)) {
                user.setPhoneVerificationToken(null);
                user.setPhoneVerificationExpires(null);
                updated = true;
            }

            // Clean up expired password reset tokens
            if (user.getPasswordResetToken() != null &&
                    user.getPasswordResetExpires() != null &&
                    user.getPasswordResetExpires().isBefore(now)) {
                user.setPasswordResetToken(null);
                user.setPasswordResetExpires(null);
                updated = true;
            }

            if (updated) {
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            userRepository.saveAll(users);
            log.info("Cleaned up expired tokens for {} users", cleanedCount);
        }
    }
}
