package edu.ucdavis.fiehnlab.config;

import feign.Logger;
import feign.Request;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by diego on 2/16/2017.
 */
@Configuration
@EnableAutoConfiguration
public class CtsClientConfiguration {
	@Bean
	Logger.Level logger() {
		return Logger.Level.FULL;
	}

	@Bean
	Request.Options options() {
		return new Request.Options(1000, 120000);
	}
}
