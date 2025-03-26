package com.ed.edc_handler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EdcHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdcHandlerApplication.class, args);
	}

}
