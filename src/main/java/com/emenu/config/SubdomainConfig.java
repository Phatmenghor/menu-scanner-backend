package com.emenu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.subdomain")
@Data
public class SubdomainConfig {
    
    /**
     * Base domain for subdomains (e.g., "menu.com")
     */
    private String baseDomain = "menu.com";
    
    /**
     * Whether to enable automatic subdomain creation during business registration
     */
    private boolean autoCreateEnabled = true;
    
    /**
     * Whether to enable subdomain maintenance tasks
     */
    private Maintenance maintenance = new Maintenance();
    
    /**
     * SSL configuration
     */
    private Ssl ssl = new Ssl();
    
    /**
     * Verification settings
     */
    private Verification verification = new Verification();

    @Data
    public static class Maintenance {
        private boolean enabled = true;
        private int accessLogRetentionDays = 90;
        private String expiredCheckCron = "0 0 */6 * * ?"; // Every 6 hours
        private String statisticsCron = "0 0 1 * * ?";     // Daily at 1 AM
        private String cleanupCron = "0 0 2 * * SUN";      // Weekly on Sunday at 2 AM
    }

    @Data
    public static class Ssl {
        private boolean autoEnable = false;
        private String certificateProvider = "letsencrypt";
    }

    @Data
    public static class Verification {
        private boolean required = false;
        private int tokenExpiryHours = 24;
    }
}