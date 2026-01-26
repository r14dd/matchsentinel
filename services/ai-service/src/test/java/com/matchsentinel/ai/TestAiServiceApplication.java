package com.matchsentinel.ai;

import org.springframework.boot.SpringApplication;

public class TestAiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AiServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
