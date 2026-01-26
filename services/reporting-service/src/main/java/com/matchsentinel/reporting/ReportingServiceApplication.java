package com.matchsentinel.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class ReportingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportingServiceApplication.class, args);
	}

}
