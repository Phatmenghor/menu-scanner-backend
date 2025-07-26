package com.emenu.features.subdomain.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class SubdomainUtils {
    
    private static final Pattern SUBDOMAIN_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 63;
    
    /**
     * Validate subdomain format according to RFC standards
     */
    public static boolean isValidSubdomainFormat(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            return false;
        }
        
        // Check length
        if (subdomain.length() < MIN_LENGTH || subdomain.length() > MAX_LENGTH) {
            log.debug("Subdomain length invalid: {} (must be {}-{} characters)", 
                     subdomain.length(), MIN_LENGTH, MAX_LENGTH);
            return false;
        }
        
        // Check format
        if (!SUBDOMAIN_PATTERN.matcher(subdomain).matches()) {
            log.debug("Subdomain format invalid: {} (must match pattern: {})", 
                     subdomain, SUBDOMAIN_PATTERN.pattern());
            return false;
        }
        
        // Additional checks
        if (subdomain.contains("--")) {
            log.debug("Subdomain contains double hyphens: {}", subdomain);
            return false;
        }
        
        return true;
    }
    
    /**
     * Clean and format subdomain name
     */
    public static String cleanSubdomainName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        return input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "")     // Remove invalid characters
                .replaceAll("^-+|-+$", "")        // Remove leading/trailing hyphens
                .replaceAll("-{2,}", "-");        // Replace multiple hyphens with single
    }
    
    /**
     * Generate subdomain from business name
     */
    public static String generateFromBusinessName(String businessName) {
        if (businessName == null || businessName.trim().isEmpty()) {
            return "business";
        }
        
        String cleaned = businessName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")  // Remove special characters
                .replaceAll("\\s+", "-")           // Replace spaces with hyphens
                .replaceAll("-{2,}", "-")          // Replace multiple hyphens with single
                .replaceAll("^-+|-+$", "");        // Remove leading/trailing hyphens
        
        // Limit length
        if (cleaned.length() > MAX_LENGTH) {
            cleaned = cleaned.substring(0, MAX_LENGTH);
            // Ensure we don't end with a hyphen after truncation
            cleaned = cleaned.replaceAll("-+$", "");
        }
        
        // Ensure minimum length
        if (cleaned.length() < MIN_LENGTH) {
            cleaned = "business-" + System.currentTimeMillis() % 10000;
        }
        
        return cleaned;
    }
    
    /**
     * Check if subdomain is reserved
     */
    public static boolean isReservedSubdomain(String subdomain) {
        if (subdomain == null) return false;
        
        String[] reserved = {
            "www", "api", "admin", "app", "mail", "email", "ftp", "blog", "shop", "store",
            "support", "help", "docs", "dev", "test", "staging", "cdn", "static", "assets",
            "img", "images", "js", "css", "fonts", "media", "uploads", "downloads",
            "dashboard", "panel", "control", "manage", "account", "profile", "settings",
            "login", "register", "signup", "signin", "auth", "oauth", "sso",
            "payment", "pay", "billing", "invoice", "order", "cart", "checkout",
            "search", "find", "lookup", "query", "browse", "explore",
            "news", "press", "media", "about", "contact", "terms", "privacy", "legal",
            "careers", "jobs", "team", "company", "corporate",
            "mobile", "m", "wap", "amp", "rss", "xml", "json", "ajax",
            "root", "administrator", "postmaster", "webmaster", "hostmaster",
            "menu", "emenu", "e-menu", "restaurant", "food", "kitchen", "chef"
        };
        
        String lowerSubdomain = subdomain.toLowerCase();
        for (String reservedWord : reserved) {
            if (lowerSubdomain.equals(reservedWord)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get full domain with subdomain
     */
    public static String getFullDomain(String subdomain, String baseDomain) {
        if (subdomain == null || baseDomain == null) {
            return null;
        }
        return subdomain + "." + baseDomain;
    }
    
    /**
     * Get full URL with protocol
     */
    public static String getFullUrl(String subdomain, String baseDomain, boolean ssl) {
        String domain = getFullDomain(subdomain, baseDomain);
        if (domain == null) return null;
        
        String protocol = ssl ? "https" : "http";
        return protocol + "://" + domain;
    }
}