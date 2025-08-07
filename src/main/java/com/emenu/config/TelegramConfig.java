package com.emenu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramConfig {
    
    private Bot bot = new Bot();
    private Notifications notifications = new Notifications();
    private Templates templates = new Templates();
    
    @Data
    public static class Bot {
        private String token;
        private boolean enabled = true;
        private String chatId;
        private String webhookUrl;
        private boolean polling = true;
        private int timeout = 30;
        private int retryAttempts = 3;
    }
    
    @Data
    public static class Notifications {
        private boolean enabled = true;
        private boolean productCreated = true;
        private boolean userRegistered = true;
        private boolean orderPlaced = true;
        private boolean businessRegistered = true;
    }
    
    @Data
    public static class Templates {
        private String productCreated;
        private String userRegistered;
        private String businessRegistered;
    }
}