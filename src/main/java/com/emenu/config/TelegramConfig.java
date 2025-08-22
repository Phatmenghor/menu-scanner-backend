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
        private String groupChatId; // Group chat ID: -2784141362
        private Notifications notifications = new Notifications();

        public boolean isEnabled() {
            return enabled && token != null && !token.trim().isEmpty();
        }
        
        public boolean hasGroupChat() {
            return groupChatId != null && !groupChatId.trim().isEmpty();
        }
    }
    
    @Data
    public static class Notifications {
        // Platform-wide notifications (sent to group)
        private boolean customerRegistration = true;
        private boolean businessRegistration = true;
        private boolean telegramLinking = true;
        private boolean systemEvents = true;
        
        // Business-specific notifications (sent to business owners only)
        private boolean orderNotifications = true;
        private boolean subscriptionAlerts = true;
        private boolean paymentUpdates = true;
        
        // Personal notifications (sent to individual users)
        private boolean welcomeMessages = true;
        private boolean accountUpdates = true;
    }
}