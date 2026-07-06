package com.talentbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TalentbridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TalentbridgeApplication.class, args);
	}

}
