package com.teadelivery.ordercatalog.vendor.controller;

import com.teadelivery.ordercatalog.vendor.dto.*;
import com.teadelivery.ordercatalog.vendor.service.BranchAvailabilityService;
import com.teadelivery.ordercatalog.vendor.service.BranchOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@Slf4j
@RequiredArgsConstructor
public class BranchController {
    
    private final BranchOnboardingService branchService;
    private final BranchAvailabilityService availabilityService;
    
    @PostMapping("/vendors/{vendorId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BranchResponse> createBranch(
            @PathVariable UUID vendorId,
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Create branch request for vendor: {}", vendorId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = branchService.createBranch(vendorId, request, requestingUserId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{branchId}")
    public ResponseEntity<BranchResponse> getBranch(
            @PathVariable UUID branchId) {
        
        log.info("Get branch request: {}", branchId);
        
        BranchResponse response = branchService.getBranch(branchId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{branchId}")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Update branch request: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = branchService.updateBranch(branchId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{branchId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable UUID branchId,
            @Valid @RequestBody DocumentUploadRequest request) {
        
        log.info("Upload document request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Mock S3 URL - in production, this would be uploaded to S3
        String documentUrl = "https://s3.example.com/branches/" + branchId + "/documents/" + request.getDocumentType();
        
        DocumentResponse response = branchService.uploadDocument(
            branchId, 
            request.getDocumentType(), 
            request.getDocumentNumber(),
            request.getIssueDate(),
            request.getExpiryDate(),
            documentUrl,
            requestingUserId
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{branchId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable UUID branchId) {
        
        log.info("Get documents request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        List<DocumentResponse> response = branchService.getDocuments(branchId, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{branchId}/preferences")
    public ResponseEntity<BranchResponse> updatePreferences(
            @PathVariable UUID branchId,
            @RequestBody Map<String, Object> preferences) {
        
        log.info("Update preferences request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = branchService.updatePreferences(branchId, preferences, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{branchId}/onboarding-status")
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(
            @PathVariable UUID branchId) {
        
        log.info("Get onboarding status request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        Map<String, Object> response = branchService.getOnboardingStatus(branchId, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{branchId}/operating-hours")
    public ResponseEntity<BranchResponse> updateOperatingHours(
            @PathVariable UUID branchId,
            @Valid @RequestBody OperatingHoursRequest request) {
        
        log.info("Update operating hours request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = availabilityService.updateOperatingHours(branchId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{branchId}/operating-hours")
    public ResponseEntity<Map<String, Object>> getOperatingHours(
            @PathVariable UUID branchId) {
        
        log.info("Get operating hours request for branch: {}", branchId);
        
        BranchResponse branch = branchService.getBranch(branchId);
        
        return ResponseEntity.ok(branch.getOperatingHours());
    }
    
    @PutMapping("/{branchId}/status")
    public ResponseEntity<BranchResponse> toggleStatus(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchStatusRequest request) {
        
        log.info("Toggle branch status request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = availabilityService.toggleBranchStatus(branchId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{branchId}/availability")
    public ResponseEntity<BranchAvailabilityResponse> checkAvailability(
            @PathVariable UUID branchId) {
        
        log.info("Check availability request for branch: {}", branchId);
        
        BranchAvailabilityResponse response = availabilityService.checkAvailability(branchId);
        
        return ResponseEntity.ok(response);
    }
}
