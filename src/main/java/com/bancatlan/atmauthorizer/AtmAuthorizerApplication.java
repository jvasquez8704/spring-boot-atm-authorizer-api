package com.bancatlan.atmauthorizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class AtmAuthorizerApplication {
	public static void main(String[] args) {
		Logger LOG = LoggerFactory.getLogger(AtmAuthorizerApplication.class);
		SpringApplication.run(AtmAuthorizerApplication.class, args);
		LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><ATM AUTHORIZER INITIALIZED SUCCESSFUL<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
}
