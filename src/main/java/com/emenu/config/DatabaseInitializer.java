package com.emenu.config;

import com.emenu.enums.RoleEnum;
import com.emenu.features.usermanagement.domain.Role;
import com.emenu.features.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Arrays;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing database with required data...");
        initializeRoles();
        log.info("Database initialization completed.");
    }

    private void initializeRoles() {
        log.info("Checking and creating system roles...");

        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            if (!roleRepository.existsByName(roleEnum)) {
                Role@Component
                @RequiredArgsConstructor
                @Slf4j
                public class ApplicationStartupListener {

                    private final Environment environment;

                    @Value("${app.name:E-Menu SaaS Platform}")
                    private String appName;

                    @Value("${app.version:1.0.0}")
                    private String appVersion;

                    @EventListener
                    public void onApplicationReady(ApplicationReadyEvent event) {
                        try {
                            String protocol = "http";
                            if (environment.getProperty("server.ssl.key-store") != null) {
                                protocol = "https";
                            }

                            String serverPort = environment.getProperty("server.port", "8080");
                            String contextPath = environment.getProperty("server.servlet.context-path", "");
                            String hostAddress = InetAddress.getLocalHost().getHostAddress();

                            log.info("\n" +
                                            "🚀 {} v{} is running successfully!\n" +
                                            "📍 Local URLs:\n" +
                                            "   Application:     {}://localhost:{}{}\n" +
                                            "   Swagger UI:      {}://localhost:{}{}/swagger-ui.html\n" +
                                            "   API Docs:        {}://localhost:{}{}/v3/api-docs\n" +
                                            "   H2 Console:      {}://localhost:{}{}/h2-console\n" +
                                            "🌐 External URLs:\n" +
                                            "   Application:     {}://{}:{}{}\n" +
                                            "   Swagger UI:      {}://{}:{}{}/swagger-ui.html\n" +
                                            "📊 Management:\n" +
                                            "   Health Check:    {}://localhost:{}{}/actuator/health\n" +
                                            "   Metrics:         {}://localhost:{}{}/actuator/metrics\n" +
                                            "🔧 Active Profiles: {}\n" +
                                            "💾 Database: {}\n",

                                    appName, appVersion,
                                    protocol, serverPort, contextPath,
                                    protocol, serverPort, contextPath,
                                    protocol, serverPort, contextPath,
                                    protocol, serverPort, contextPath,
                                    protocol, hostAddress, serverPort, contextPath,
                                    protocol, hostAddress, serverPort, contextPath,
                                    protocol, serverPort, contextPath,
                                    protocol, serverPort, contextPath,
                                    String.join(", ", environment.getActiveProfiles()),
                                    environment.getProperty("spring.datasource.url", "Not configured")
                            );

                            // Log default credentials in development
                            if (environment.acceptsProfiles("dev", "test")) {
                                log.info("\n" +
                                        "🔐 Default Credentials (Development Only):\n" +
                                        "   Platform Owner:  admin@emenu-platform.com / Admin123!@#\n" +
                                        "   Business Owner:  demo-business@emenu-platform.com / Business123!\n" +
                                        "   Customer:        demo-customer@emenu-platform.com / Customer123!\n"
                                );
                            }

                        } catch (Exception e) {
                            log.error("Failed to display startup information", e);
                        }
                    }
                }
                role = new Role(roleEnum);
                roleRepository.save(role);
                log.info("Created role: {} with permissions: {}",
                        roleEnum.name(), role.getPermissions());
            } else {
                log.debug("Role already exists: {}", roleEnum.name());
            }
        });

        log.info("System roles initialization completed. Total roles: {}", roleRepository.count());
    }
}
