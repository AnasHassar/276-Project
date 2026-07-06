package com.cmpt276.SFSS.Nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SfssNexusApplication {

	public static void main(String[] args) {
		SpringApplication.run(SfssNexusApplication.class, args);
	}

}
