package com.bancatlan.atmauthorizer;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		//TODO add class enum with custom exception
		//TODO add custom errors model json structure to response
		//TODO add Static class with useCase, status and etc codes
		//TODO finish model customer, status, useCase, currency, limits
		//TODO add security
		//TODO Add limits validations
		//TODO add scheduler to check expired transactions
		//TODO add mainly in interceptor and then Log everyWhere you consider necessary
		//TODO add util that create a pickup code for each single request
		//TODO Generar pickupCode
		return application.sources(AtmAuthorizerApplication.class);
	}

}
