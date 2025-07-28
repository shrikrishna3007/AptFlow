package com.project.aptflow;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableGlobalAuthentication
@OpenAPIDefinition
@EnableScheduling
public class AptFlow {
	public static void main(String[] args) {
		SpringApplication.run(AptFlow.class, args);
	}
}
