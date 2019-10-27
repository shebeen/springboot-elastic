package com.Elastic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.Elastic.service.SpreadsheetService;

@Configuration
@ComponentScan("com.Elastic.service")
public class ServiceConfiguration {
	@Bean
	public SpreadsheetService spreadSheetService() {
		return new SpreadsheetService();
	}
}
