package com.induscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InduscoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(InduscoreApplication.class, args);
	}

}
