package com.teamproject.sellog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SellogApplication {

	public static void main(String[] args) {
		SpringApplication.run(SellogApplication.class, args);
	}

}
