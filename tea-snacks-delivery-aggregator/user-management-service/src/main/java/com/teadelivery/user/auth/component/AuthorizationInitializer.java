package com.teadelivery.user.auth.component;

import com.teadelivery.user.auth.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationInitializer implements ApplicationRunner {
    
    private final AuthorizationService authorizationService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing authorization framework...");
        
        try {
            // Initialize default permissions and role assignments
            authorizationService.initializeDefaultPermissions();
            log.info("Authorization framework initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize authorization framework", e);
            // Don't throw exception to allow application to start
        }
    }
} 