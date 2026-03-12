package com.emenu.features.main.scheduler;

import com.emenu.features.main.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPromotionScheduler {

    private final ProductRepository productRepository;

    /**
     * Runs every day at 00:05 to clear display fields for products
     * whose promotions have expired (toDate < today).
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void clearExpiredPromotions() {
        log.info("[Scheduler] Clearing expired product promotions...");

        int noSizesUpdated = productRepository.clearExpiredPromotionsForProductsWithoutSizes();
        log.info("[Scheduler] Products without sizes updated: {}", noSizesUpdated);

        int withSizesUpdated = productRepository.clearExpiredPromotionsForProductsWithSizes();
        log.info("[Scheduler] Products with sizes updated: {}", withSizesUpdated);

        log.info("[Scheduler] Done. Total updated: {}", noSizesUpdated + withSizesUpdated);
    }
}
