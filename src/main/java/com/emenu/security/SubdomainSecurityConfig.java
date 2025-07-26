package com.emenu.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SubdomainSecurityConfig {

    @Value("${app.subdomain.base-domain:menu.com}")
    private String baseDomain;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String[] allowedOrigins;

    public SubdomainSecurityConfig(String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * CORS configuration that includes subdomain support
     */
    @Bean("subdomainCorsConfigurationSource")
    public CorsConfigurationSource subdomainCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Add base allowed origins
        List<String> origins = Arrays.asList(allowedOrigins);
        configuration.setAllowedOriginPatterns(origins);
        
        // Add subdomain patterns
        configuration.addAllowedOriginPattern("https://*." + baseDomain);
        configuration.addAllowedOriginPattern("http://*." + baseDomain);
        
        // Allow common subdomain patterns for development/testing
        configuration.addAllowedOriginPattern("https://*.localhost:*");
        configuration.addAllowedOriginPattern("http://*.localhost:*");
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("Configured CORS for subdomain support with base domain: {}", baseDomain);
        return source;
    }
}
