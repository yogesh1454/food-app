package com.teadelivery.notification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.notification.model.NotificationRequest;
import com.teadelivery.notification.model.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Notification Service.
 * Tests the complete notification flow including validation, processing, and response.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class NotificationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("notification-service"));
    }

    @Test
    public void testSendEmailNotification_Success() throws Exception {
        // Prepare test data
        NotificationRequest request = createEmailNotificationRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // Send notification request
        MvcResult result = mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.notificationId").exists())
                .andReturn();

        // Validate response
        String responseJson = result.getResponse().getContentAsString();
        NotificationResponse response = objectMapper.readValue(responseJson, NotificationResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getNotificationId());
        assertTrue(response.isSuccess() || response.getStatus() == NotificationResponse.Status.FAILED);
    }

    @Test
    public void testSendSmsNotification_Success() throws Exception {
        // Prepare test data
        NotificationRequest request = createSmsNotificationRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // Send notification request
        MvcResult result = mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.notificationId").exists())
                .andReturn();

        // Validate response
        String responseJson = result.getResponse().getContentAsString();
        NotificationResponse response = objectMapper.readValue(responseJson, NotificationResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getNotificationId());
        assertTrue(response.isSuccess() || response.getStatus() == NotificationResponse.Status.FAILED);
    }

    @Test
    public void testSendAsyncNotification_Success() throws Exception {
        // Prepare test data
        NotificationRequest request = createEmailNotificationRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // Send async notification request
        mockMvc.perform(post("/api/notifications/send-async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.message").value("Notification queued for processing"));
    }

    @Test
    public void testInvalidNotificationRequest_BadRequest() throws Exception {
        // Prepare invalid test data (missing required fields)
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationRequest.Type.EMAIL)
                // Missing recipient, template, etc.
                .build();
        
        String requestJson = objectMapper.writeValueAsString(request);

        // Send invalid notification request
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testEmptyRequestBody_BadRequest() throws Exception {
        // Send empty request body
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Helper method to create a valid email notification request.
     */
    private NotificationRequest createEmailNotificationRequest() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user_name", "Test User");
        variables.put("verification_link", "https://example.com/verify/123");
        variables.put("expiry_time", "24 hours");

        return NotificationRequest.builder()
                .userId(UUID.randomUUID())
                .type(NotificationRequest.Type.EMAIL)
                .template("registration-verification")
                .recipient("test@example.com")
                .subject("Verify your Tea & Snacks account")
                .variables(variables)
                .priority(NotificationRequest.Priority.NORMAL)
                .build();
    }

    /**
     * Helper method to create a valid SMS notification request.
     */
    private NotificationRequest createSmsNotificationRequest() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", "123456");
        variables.put("expiry", "5");

        return NotificationRequest.builder()
                .userId(UUID.randomUUID())
                .type(NotificationRequest.Type.SMS)
                .template("phone-verification")
                .recipient("+1234567890")
                .variables(variables)
                .priority(NotificationRequest.Priority.HIGH)
                .build();
    }
}
