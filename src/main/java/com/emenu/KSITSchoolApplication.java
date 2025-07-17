package com.emenu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KSITSchoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(KSITSchoolApplication.class, args);
	}
}
