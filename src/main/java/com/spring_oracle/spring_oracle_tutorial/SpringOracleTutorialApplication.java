package com.spring_oracle.spring_oracle_tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringOracleTutorialApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOracleTutorialApplication.class, args);
	}

}
