package com.conel.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class LimoMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimoMarketApplication.class, args);
	}
}