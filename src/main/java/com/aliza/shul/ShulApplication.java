package com.aliza.shul;

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
@EnableScheduling
@ServletComponentScan
//@SpringBootApplication(scanBasePackages = "com.aliza.shul")
//@EntityScan({"com.aliza.shul.entities", "com.aliza.shul.security"}) // <- this line ensures JPA picks up your entities
//@EnableJpaRepositories("com.aliza.shul.repositories")
public class ShulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShulApplication.class, args);
	}

}
