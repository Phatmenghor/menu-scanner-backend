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
}