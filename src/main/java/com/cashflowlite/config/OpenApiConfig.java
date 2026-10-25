package com.cashflowlite.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Title and description shown at the top of Swagger UI (/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI cashflowOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Cashflow Lite API")
				.version("v1")
				.description("Record expenses (money paid out), deposits (money received), browse all "
						+ "transactions and read the monthly report. Dates are YYYY-MM-DD. Amounts are in rupees."));
	}
}
