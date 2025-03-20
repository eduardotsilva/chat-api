package com.seven.cors.chat.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.seven.cors.chat.api")
public class ChatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatApiApplication.class, args);
	}

}
