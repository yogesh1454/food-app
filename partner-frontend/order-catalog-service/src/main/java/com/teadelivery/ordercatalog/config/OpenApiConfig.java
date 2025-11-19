package com.teadelivery.ordercatalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Order & Catalog Management Service.
 * Access Swagger UI at: http://localhost:8082/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8082/v3/api-docs
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order & Catalog Management Service API")
                .version("1.0.0")
                .description("""
                    REST API for managing vendors, branches, menu items, and orders in the Tea Snacks Delivery platform.
                    
                    ## Features
                    - Vendor registration and management
                    - Branch onboarding and configuration
                    - Menu item management
                    - Operating hours and availability management
                    - Document upload and verification
                    
                    ## Authentication
                    Currently using hardcoded user IDs for development. Production will use JWT tokens.
                    
                    ## Error Handling
                    All endpoints return consistent error responses with appropriate HTTP status codes.
                    """)
                .contact(new Contact()
                    .name("Tea Snacks Delivery Team")
                    .email("support@teadelivery.com")
                    .url("https://teadelivery.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8082")
                    .description("Local Development Server"),
                new Server()
                    .url("https://api-dev.teadelivery.com")
                    .description("Development Environment"),
                new Server()
                    .url("https://api.teadelivery.com")
                    .description("Production Environment")
            ));
    }
}
