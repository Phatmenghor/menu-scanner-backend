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
            LocalDateTime now = LocalDateTime.now();
            List<Subdomain> expiredSubdomains = subdomainRepository.findExpiredSubdomains(now);
            
            if (expiredSubdomains.isEmpty()) {
                log.debug("No subdomains with expired subscriptions found");
                return;
            }

            int updatedCount = 0;
            for (Subdomain subdomain : expiredSubdomains) {
                if (subdomain.getStatus() == SubdomainStatus.ACTIVE) {
                    subdomain.setStatus(SubdomainStatus.EXPIRED);
                    subdomain.setIsActive(false);
                    subdomain.setNotes((subdomain.getNotes() != null ? subdomain.getNotes() + "\n" : "") + 
                                     "Automatically marked as expired due to subscription expiry on " + now);
                    subdomainRepository.save(subdomain);
                    updatedCount++;
                    
                    log.debug("Marked subdomain as expired: {}", subdomain.getSubdomain());
                }
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
            long activeSubdomains = subdomainRepository.countActiveSubdomains();
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
}