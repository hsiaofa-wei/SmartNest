package com.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.rental.repository")
@EnableScheduling
public class SmartRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartRentalApplication.class, args);
    }
}

