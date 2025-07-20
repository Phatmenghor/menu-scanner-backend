package com.emenu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class EMenuApplication {

	public static void main(String[] args) {
		SpringApplication.run(EMenuApplication.class, args);
		System.out.println("""
            
            🚀 E-Menu SaaS Platform Started Successfully!
            
            📱 Features Available:
            ✅ Complete User Management (Platform, Business, Customer)
            ✅ JWT Authentication & Authorization
            ✅ Role-Based Access Control
            ✅ Complete Messaging System
            ✅ Subscription Management
            ✅ Customer Loyalty Tiers
            ✅ Audit Logging
            ✅ Multi-tenant Architecture
            
            🌐 Access Points:
            • Application: http://localhost:8080
            • Swagger UI: http://localhost:8080/swagger-ui.html
            • Health Check: http://localhost:8080/actuator/health
            
            🔑 Default Credentials:
            • Platform Owner: phatmenghor19@gmail.com / 88889999
            • Demo Business: demo-business@emenu-platform.com / Business123!
            • Demo Customer: demo-customer@emenu-platform.com / Customer123!
            
            """);
	}
}
