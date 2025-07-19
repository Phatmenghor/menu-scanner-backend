package com.emenu.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class PerformanceMonitoringConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "emenu-platform");
    }

    @Bean
    public OncePerRequestFilter performanceFilter(MeterRegistry meterRegistry) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                
                Timer.Sample sample = Timer.start(meterRegistry);
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    sample.stop(Timer.builder("http.requests")
                            .tag("method", request.getMethod())
                            .tag("uri", request.getRequestURI())
                            .tag("status", String.valueOf(response.getStatus()))
                            .register(meterRegistry));
                }
            }
        };
    }
}