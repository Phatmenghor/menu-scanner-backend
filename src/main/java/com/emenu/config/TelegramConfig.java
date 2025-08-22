// src/main/java/com/emenu/config/TelegramConfig.java
package com.emenu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramConfig {

    private Bot bot = new Bot();

    @Data
    public static class Bot {
        private String token;
        private boolean enabled = true;
        private String chatId; // Platform owner chat ID for notifications

        public boolean isEnabled() {
            return enabled && token != null && !token.trim().isEmpty();
        }
    }
}