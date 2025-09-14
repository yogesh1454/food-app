package com.teadelivery.notification.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for notification processing.
 * Follows coding standards with comprehensive async setup.
 */
@Slf4j
@Configuration
@EnableAsync
@EnableRetry
@RequiredArgsConstructor
public class AsyncConfig {

    private final NotificationConfig notificationConfig;

    /**
     * Creates task executor for async notification processing.
     * 
     * @return configured task executor
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        NotificationConfig.Async asyncConfig = notificationConfig.getAsync();
        executor.setCorePoolSize(asyncConfig.getThreadPoolSize());
        executor.setMaxPoolSize(asyncConfig.getThreadPoolSize() * 2);
        executor.setQueueCapacity(asyncConfig.getQueueCapacity());
        executor.setThreadNamePrefix(asyncConfig.getThreadNamePrefix());
        
        // Rejection policy - caller runs the task
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Notification task queue is full, running task synchronously");
            r.run();
        });
        
        executor.initialize();
        
        log.info("Notification task executor configured with {} core threads, {} max threads, {} queue capacity",
                asyncConfig.getThreadPoolSize(),
                asyncConfig.getThreadPoolSize() * 2,
                asyncConfig.getQueueCapacity());
        
        return executor;
    }
}
