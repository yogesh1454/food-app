package com.teadelivery.ordercatalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order & Catalog Management Service API")
                .version("1.0.0")
                .description("REST API for managing vendors, branches, menu items, and orders in the Tea Snacks Delivery platform")
                .contact(new Contact()
                    .name("Tea Snacks Delivery Team")
                    .email("support@teadelivery.com")
                    .url("https://teadelivery.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
