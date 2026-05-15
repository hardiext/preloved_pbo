package com.preloved_id.preloved;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrelovedApplication {
	public static void main(String[] args) {
		SpringApplication.run(PrelovedApplication.class, args);
	}
}