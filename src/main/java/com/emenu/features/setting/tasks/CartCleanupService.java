package com.emenu.features.setting.tasks;

import com.emenu.features.order.repository.CartItemRepository;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartCleanupService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    /**
     * Clean up cart items for deleted or inactive products
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    @Transactional
    public void cleanupInvalidCartItems() {
        try {
            log.info("Starting cleanup of invalid cart items...");

            // Clean up cart items for deleted products
            int deletedForDeletedProducts = cartItemRepository.deleteCartItemsForDeletedProducts();
            log.info("Cleaned up {} cart items for deleted products", deletedForDeletedProducts);

            // Clean up cart items for inactive products
            int deletedForInactiveProducts = cartItemRepository.deleteCartItemsForInactiveProducts();
            log.info("Cleaned up {} cart items for inactive products", deletedForInactiveProducts);

            // Clean up cart items for deleted product sizes
            int deletedForDeletedSizes = cartItemRepository.deleteCartItemsForDeletedProductSizes();
            log.info("Cleaned up {} cart items for deleted product sizes", deletedForDeletedSizes);

            log.info("Cart cleanup completed. Total cleaned: {}", 
                    deletedForDeletedProducts + deletedForInactiveProducts + deletedForDeletedSizes);

        } catch (Exception e) {
            log.error("Error during cart cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old cart items (older than 7 days)
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldCartItems() {
        try {
            log.info("Starting cleanup of old cart items...");

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            int deletedCount = cartItemRepository.deleteOldCartItems(cutoffDate);
            
            log.info("Cleaned up {} old cart items (older than 7 days)", deletedCount);

        } catch (Exception e) {
            log.error("Error during old cart items cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Clear expired promotions on products and product sizes
     * Runs daily at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    @Transactional
    public void clearExpiredPromotions() {
        try {
            log.info("Starting cleanup of expired promotions...");

            LocalDateTime now = LocalDateTime.now();

            // Clear expired product-level promotions
            int productPromotionsCleared = productRepository.clearExpiredPromotions(now);
            log.info("Cleared {} expired product promotions", productPromotionsCleared);

            // Clear expired product size promotions
            int sizePromotionsCleared = productSizeRepository.clearExpiredPromotions(now);
            log.info("Cleared {} expired product size promotions", sizePromotionsCleared);

            log.info("Promotion cleanup completed. Total cleared: {}", 
                    productPromotionsCleared + sizePromotionsCleared);

        } catch (Exception e) {
            log.error("Error during promotion cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual cleanup method for admin use
     */
    @Transactional
    public void performManualCleanup() {
        log.info("Performing manual cleanup...");
        cleanupInvalidCartItems();
        cleanupOldCartItems();
        clearExpiredPromotions();
        log.info("Manual cleanup completed");
    }

    /**
     * Get cleanup statistics
     */
    @Transactional(readOnly = true)
    public CleanupStats getCleanupStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusDays(7);

            long totalCartItems = cartItemRepository.countActiveCartItems();
            long oldCartItems = cartItemRepository.countOldCartItems(weekAgo);
            long expiredProductPromotions = productRepository.countExpiredPromotions(now);
            long expiredSizePromotions = productSizeRepository.countExpiredPromotions(now);

            return new CleanupStats(totalCartItems, oldCartItems, 
                    expiredProductPromotions, expiredSizePromotions);

        } catch (Exception e) {
            log.error("Error getting cleanup statistics: {}", e.getMessage(), e);
            return new CleanupStats(0, 0, 0, 0);
        }
    }

    /**
     * Statistics holder class
     */
    public static class CleanupStats {
        public final long totalCartItems;
        public final long oldCartItems;
        public final long expiredProductPromotions;
        public final long expiredSizePromotions;

        public CleanupStats(long totalCartItems, long oldCartItems, 
                           long expiredProductPromotions, long expiredSizePromotions) {
            this.totalCartItems = totalCartItems;
            this.oldCartItems = oldCartItems;
            this.expiredProductPromotions = expiredProductPromotions;
            this.expiredSizePromotions = expiredSizePromotions;
        }

        @Override
        public String toString() {
            return String.format("CleanupStats{totalCartItems=%d, oldCartItems=%d, " +
                    "expiredProductPromotions=%d, expiredSizePromotions=%d}", 
                    totalCartItems, oldCartItems, expiredProductPromotions, expiredSizePromotions);
        }
    }
}