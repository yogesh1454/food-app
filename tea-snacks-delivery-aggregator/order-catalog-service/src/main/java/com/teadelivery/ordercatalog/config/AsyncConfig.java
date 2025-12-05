package com.teadelivery.ordercatalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async Configuration
 * 
 * Enables asynchronous method execution for non-blocking operations
 * like analytics tracking
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Uses default async executor
}


