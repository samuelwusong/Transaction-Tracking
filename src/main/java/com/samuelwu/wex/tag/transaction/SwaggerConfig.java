package com.samuelwu.wex.tag.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI awesomeAPI() {
		return new OpenAPI()
				.info(new Info().title("WEX TAG Transaction API").description("APIs provided by the transaction application")
						.version("1.0")
						.license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
				.externalDocs(new ExternalDocumentation().description("Author: Samuel WU")
						.url("https://github.com/samuelwusong"));
	}
}