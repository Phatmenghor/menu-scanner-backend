package com.menghor.ksit.feature.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SubscriptionSecurityConfig implements WebMvcConfigurer {

    private final SubscriptionInterceptor subscriptionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(subscriptionInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/api/v1/plans/**",
                        "/api/v1/subscriptions/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/api/v1/images/**",
                        "/favicon.ico",
                        "/*.ico",
                        "/static/**",
                        "/error"         // Add the error page
                );
    }
}