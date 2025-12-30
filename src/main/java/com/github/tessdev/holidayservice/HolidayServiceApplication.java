package com.github.tessdev.holidayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.tessdev.holidayservice")
public class HolidayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HolidayServiceApplication.class, args);
	}

}
