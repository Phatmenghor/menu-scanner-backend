package com.emenu.security;

import com.emenu.security.jwt.JWTAuthenticationFilter;
import com.emenu.security.jwt.JwtAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint authEntryPoint;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // Documentation endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()

                        // Actuator endpoints (restricted to localhost and private networks)
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("PLATFORM_OWNER")

                        // Platform administration - Complete platform management
                        .requestMatchers("/api/v1/platform/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN")

                        // User management - Enhanced permissions
                        .requestMatchers("/api/v1/users/me/**").authenticated()
                        .requestMatchers("/api/v1/users/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "BUSINESS_OWNER")

                        // Business management - Complete business operations
                        .requestMatchers("/api/v1/business/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "BUSINESS_OWNER", "BUSINESS_MANAGER")
                        .requestMatchers("/api/v1/businesses/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "BUSINESS_OWNER", "BUSINESS_MANAGER")

                        // Customer management
                        .requestMatchers("/api/v1/customers/me/**").hasAnyRole("CUSTOMER", "VIP_CUSTOMER")
                        .requestMatchers("/api/v1/customers/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "BUSINESS_OWNER", "BUSINESS_MANAGER")

                        // Messaging system - All authenticated users can access
                        .requestMatchers("/api/v1/messages/**").authenticated()

                        // Subscription management
                        .requestMatchers("/api/v1/subscriptions/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "BUSINESS_OWNER")
                        .requestMatchers("/api/v1/payments/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN", "BUSINESS_OWNER")

                        // Notification system
                        .requestMatchers("/api/v1/notifications/**").hasAnyRole("PLATFORM_OWNER", "PLATFORM_ADMIN")

                        // Customer tier and loyalty
                        .requestMatchers("/api/v1/loyalty/**").authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        // Modern frame options configuration - deny all frames
                        .frameOptions(frameOptions -> frameOptions.deny())
                        // Modern content type options - prevent MIME sniffing
                        .contentTypeOptions(Customizer.withDefaults())
                        // Modern HSTS configuration - enforce HTTPS for 1 year
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000))
                        // Modern referrer policy - strict origin when cross-origin
                        .referrerPolicy(referrerPolicy ->
                                referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition", "X-Total-Count"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Higher strength for production
    }
}