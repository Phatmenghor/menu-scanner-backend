// ================================
// FIXED SUBDOMAIN MAINTENANCE SCHEDULER
// ================================

package com.emenu.features.setting.tasks;

import com.emenu.enums.subdomain.SubdomainStatus;
import com.emenu.features.subdomain.models.Subdomain;
import com.emenu.features.subdomain.repository.SubdomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.subdomain.maintenance.enabled", havingValue = "true", matchIfMissing = true)
public class SubdomainMaintenanceScheduler {

    private final SubdomainRepository subdomainRepository;

    /**
     * Check and update subdomains with expired subscriptions every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    @Transactional
    public void checkExpiredSubscriptions() {
        log.info("Starting scheduled check for expired subscriptions");

        try {
            // ✅ FIXED: Use simplified query with JOIN to get expired business subscriptions
            List<Subdomain> expiredSubdomains = subdomainRepository.findAll().stream()
                    .filter(subdomain -> !subdomain.getIsDeleted())
                    .filter(subdomain -> subdomain.getBusiness() != null)
                    .filter(subdomain -> !subdomain.getBusiness().hasActiveSubscription())
                    .filter(subdomain -> subdomain.getStatus() == SubdomainStatus.ACTIVE)
                    .toList();

            if (expiredSubdomains.isEmpty()) {
                log.debug("No subdomains with expired subscriptions found");
                return;
            }

            int updatedCount = 0;
            for (Subdomain subdomain : expiredSubdomains) {
                // ✅ FIXED: Use setStatus instead of setIsActive (which doesn't exist)
                subdomain.setStatus(SubdomainStatus.EXPIRED);
                subdomain.setNotes((subdomain.getNotes() != null ? subdomain.getNotes() + "\n" : "") +
                        "Automatically marked as expired due to subscription expiry on " + LocalDateTime.now());
                subdomainRepository.save(subdomain);
                updatedCount++;

                log.debug("Marked subdomain as expired: {}", subdomain.getSubdomain());
            }

            if (updatedCount > 0) {
                log.info("Marked {} subdomains as expired due to subscription expiry", updatedCount);
            }

        } catch (Exception e) {
            log.error("Failed to execute expired subscription check", e);
        }
    }

    /**
     * Log subdomain statistics every day at 1 AM for monitoring
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void logSubdomainStatistics() {
        try {
            long totalSubdomains = subdomainRepository.countTotalSubdomains();

            // ✅ FIXED: Use countByStatus instead of countActiveSubdomains
            long activeSubdomains = subdomainRepository.countByStatus(SubdomainStatus.ACTIVE);
            long expiredSubdomains = subdomainRepository.countByStatus(SubdomainStatus.EXPIRED);
            long suspendedSubdomains = subdomainRepository.countByStatus(SubdomainStatus.SUSPENDED);

            log.info("Daily Subdomain Statistics - Total: {}, Active: {}, Expired: {}, Suspended: {}",
                    totalSubdomains, activeSubdomains, expiredSubdomains, suspendedSubdomains);

            // Alert if too many subdomains are expired or suspended
            double expiredPercentage = totalSubdomains > 0 ? (double) expiredSubdomains / totalSubdomains * 100 : 0;
            if (expiredPercentage > 20) {
                log.warn("High percentage of expired subdomains detected: {:.1f}% ({}/{})",
                        expiredPercentage, expiredSubdomains, totalSubdomains);
            }

        } catch (Exception e) {
            log.error("Failed to log subdomain statistics", e);
        }
    }

    /**
     * Clean up old access logs every week on Sunday at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void cleanupOldAccessLogs() {
        log.info("Starting cleanup of old access logs");

        try {
            // Reset access counts for subdomains not accessed in last 90 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);

            List<Subdomain> oldSubdomains = subdomainRepository.findAll().stream()
                    .filter(s -> !s.getIsDeleted())
                    .filter(s -> s.getLastAccessed() != null && s.getLastAccessed().isBefore(cutoffDate))
                    .filter(s -> s.getAccessCount() > 0)
                    .toList();

            if (oldSubdomains.isEmpty()) {
                log.debug("No old access logs found for cleanup");
                return;
            }

            int cleanedCount = 0;
            for (Subdomain subdomain : oldSubdomains) {
                subdomain.setAccessCount(0L);
                subdomain.setLastAccessed(null);
                subdomainRepository.save(subdomain);
                cleanedCount++;
            }

            log.info("Cleaned up access logs for {} subdomains (not accessed in 90 days)", cleanedCount);

        } catch (Exception e) {
            log.error("Failed to cleanup old access logs", e);
        }
    }

    /**
     * ✅ NEW: Activate subdomains for businesses that got new subscriptions
     */
    @Scheduled(cron = "0 0 */6 * * ?") // Every 6 hours
    @Transactional
    public void activateSubdomainsWithNewSubscriptions() {
        log.debug("Checking for subdomains to reactivate due to new subscriptions");

        try {
            // Find expired subdomains where business now has active subscription
            List<Subdomain> subdomainsToReactivate = subdomainRepository.findAll().stream()
                    .filter(subdomain -> !subdomain.getIsDeleted())
                    .filter(subdomain -> subdomain.getStatus() == SubdomainStatus.EXPIRED)
                    .filter(subdomain -> subdomain.getBusiness() != null)
                    .filter(subdomain -> subdomain.getBusiness().hasActiveSubscription())
                    .toList();

            if (subdomainsToReactivate.isEmpty()) {
                log.debug("No subdomains found for reactivation");
                return;
            }

            int reactivatedCount = 0;
            for (Subdomain subdomain : subdomainsToReactivate) {
                subdomain.setStatus(SubdomainStatus.ACTIVE);
                subdomain.setNotes((subdomain.getNotes() != null ? subdomain.getNotes() + "\n" : "") +
                        "Automatically reactivated due to new subscription on " + LocalDateTime.now());
                subdomainRepository.save(subdomain);
                reactivatedCount++;

                log.debug("Reactivated subdomain: {}", subdomain.getSubdomain());
            }

            if (reactivatedCount > 0) {
                log.info("Reactivated {} subdomains due to new subscriptions", reactivatedCount);
            }

        } catch (Exception e) {
            log.error("Failed to reactivate subdomains", e);
        }
    }
}