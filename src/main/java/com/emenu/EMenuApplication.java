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
            
            🇰🇭 Cambodia E-Menu Platform Started Successfully! 🇰🇭
            
            🏗️ Clean Architecture Features:
            ✅ Universal Pagination Mappers
            ✅ Specification-Based Filtering
            ✅ Clean Service Implementations
            ✅ Cambodia-Specific Configuration
            ✅ Payment & Subscription System
            ✅ UserIdentifier-Based Authentication
            
            🌐 Access Points:
            • Application: http://localhost:8080
            • Swagger UI: http://localhost:8080/swagger-ui.html
            • Health Check: http://localhost:8080/actuator/health
            
            🔑 Default Credentials (UserIdentifier / Password):
            • Platform Owner: phatmenghor19@gmail.com / 88889999
            • Business Owner: demo-business-owner / Business123!
            • Customer: demo-customer / Customer123!
            
            📝 Test Accounts:
            • Inactive User: inactive-user / Test123!
            • Locked User: locked-user / Test123!
            • Suspended User: suspended-user / Test123!
            
            """);
	}
}