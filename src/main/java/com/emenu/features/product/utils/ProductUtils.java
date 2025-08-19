package com.emenu.features.product.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductUtils {

    /**
     * Sanitize product name - simple trim and space normalization
     */
    public String sanitizeProductName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Check if image URL is valid
     */
    public boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        
        String url = imageUrl.toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/");
    }

    /**
     * Log product operation
     */
    public void logProductOperation(String operation, String productName, String businessId) {
        log.info("Product Operation - {}: '{}' for business: {}", 
                operation, productName, businessId);
    }
}