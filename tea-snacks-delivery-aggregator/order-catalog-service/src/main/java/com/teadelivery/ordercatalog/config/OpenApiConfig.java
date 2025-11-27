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
                .title("Order Catalog & Delivery Management Service API")
                .version("2.0.0")
                .description("""
                    REST API for managing orders, deliveries, and catalog in the Tea Snacks Delivery platform.
                    
                    ## Features
                    - **Order Management**: Create, track, and manage orders with FSM
                    - **Delivery Management**: Rider assignment, tracking, and delivery lifecycle
                    - **Vendor & Catalog**: Vendor registration, branches, menu items
                    - **Smart Rider Assignment**: Geospatial search and ranking algorithm
                    - **Real-time Tracking**: Live delivery tracking with PostGIS
                    
                    ## State Machines
                    - **Order FSM**: 13 states (DRAFT → DELIVERED/CANCELLED)
                    - **Delivery FSM**: 9 states (PENDING → DELIVERED/FAILED)
                    
                    ## Authentication
                    Currently using header-based IDs for development (X-Customer-Id, X-Rider-Id, X-Restaurant-Id).
                    Production will use JWT tokens.
                    
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
