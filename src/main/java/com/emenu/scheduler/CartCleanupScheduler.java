package com.emenu.scheduler;

import com.emenu.features.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task to automatically cleanup stale cart items.
 * This ensures cart items are synchronized with product changes and old carts are removed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupScheduler {

    private final CartItemRepository cartItemRepository;

    /**
     * Clean up cart items for deleted or inactive products.
     * Runs every hour to keep carts synchronized with product changes.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void cleanupStaleCartItems() {
        log.info("Starting scheduled cart cleanup task");

        try {
            // Delete cart items for products that have been marked as deleted
            int deletedProductItems = cartItemRepository.deleteCartItemsForDeletedProducts();
            log.info("Deleted {} cart items for deleted products", deletedProductItems);

            // Delete cart items for products that are inactive (status != ACTIVE)
            int inactiveProductItems = cartItemRepository.deleteCartItemsForInactiveProducts();
            log.info("Deleted {} cart items for inactive products", inactiveProductItems);

            // Delete cart items for product sizes that have been deleted
            int deletedSizeItems = cartItemRepository.deleteCartItemsForDeletedProductSizes();
            log.info("Deleted {} cart items for deleted product sizes", deletedSizeItems);

            log.info("Completed scheduled cart cleanup. Total items deleted: {}",
                    deletedProductItems + inactiveProductItems + deletedSizeItems);
        } catch (Exception e) {
            log.error("Error during scheduled cart cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old cart items (older than 30 days).
     * Runs daily at 2 AM to remove abandoned carts.
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    public void cleanupOldCartItems() {
        log.info("Starting scheduled old cart cleanup task");

        try {
            // Delete cart items older than 30 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedOldItems = cartItemRepository.deleteOldCartItems(cutoffDate);
            log.info("Deleted {} cart items older than 30 days", deletedOldItems);
        } catch (Exception e) {
            log.error("Error during old cart cleanup: {}", e.getMessage(), e);
        }
    }
}
