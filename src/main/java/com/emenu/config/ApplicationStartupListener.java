package com.emenu.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
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
                        "🔐 Default Credentials (UserIdentifier / Password):\n" +
                        "   Platform Owner:  phatmenghor19@gmail.com / 88889999\n" +
                        "   Business Owner:  demo-business-owner / Business123!\n" +
                        "   Customer:        demo-customer / Customer123!\n" +
                        "\n" +
                        "📝 Test Accounts (UserIdentifier / Password):\n" +
                        "   Inactive User:   inactive-user / Test123!\n" +
                        "   Locked User:     locked-user / Test123!\n" +
                        "   Suspended User:  suspended-user / Test123!\n"
                );
            }
            
        } catch (Exception e) {
            log.error("Failed to display startup information", e);
        }
    }
}