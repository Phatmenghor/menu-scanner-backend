package com.emenu.features.main.scheduler;

import com.emenu.features.main.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPromotionScheduler {

    private final ProductService productService;

    /**
     * Runs every day at 00:05 to clear display fields for products
     * whose promotions have expired (toDate < today).
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void clearExpiredPromotions() {
        log.info("[Scheduler] Clearing expired product promotions...");

        int[] result = productService.syncExpiredPromotions();

        log.info("[Scheduler] Done. Products without sizes: {}, with sizes: {}, total: {}",
                result[0], result[1], result[0] + result[1]);
    }

    /**
     * Runs every day at 00:10 to activate display fields for products
     * whose promotions have just started (fromDate <= today and not yet active).
     */
    @Scheduled(cron = "0 10 0 * * *")
    public void activateStartedPromotions() {
        log.info("[Scheduler] Activating newly started product promotions...");

        int[] result = productService.syncStartedPromotions();

        log.info("[Scheduler] Done. Products without sizes: {}, with sizes: {}, total: {}",
                result[0], result[1], result[0] + result[1]);
    }
}
