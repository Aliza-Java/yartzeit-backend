package com.aliza.shul;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
//@EnableEncryptableProperties
@Slf4j
@EnableScheduling
@ServletComponentScan
public class ShulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShulApplication.class, args);
		System.out.println("New version date: September 7, 2025 21:32");
	}

	@Value("${app.version}")
	private String appVersion;

	@PostConstruct
	public void logVersion() {
		log.info("Running backend version: {}", appVersion);
	}
}
