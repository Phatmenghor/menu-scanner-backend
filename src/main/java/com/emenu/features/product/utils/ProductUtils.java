package com.emenu.features.product.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductUtils {

    public boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }

        String url = imageUrl.toLowerCase();
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/");
    }
}