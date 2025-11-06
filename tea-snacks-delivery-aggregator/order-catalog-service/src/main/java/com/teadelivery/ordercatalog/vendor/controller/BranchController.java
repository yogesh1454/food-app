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
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class BranchController {
    
    private final BranchOnboardingService branchService;
    private final BranchAvailabilityService availabilityService;
    
    @PostMapping("/vendors/{vendorId}/branches")
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
    
    @PutMapping("/vendors/{vendorId}/branches/{branchId}")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable UUID vendorId,
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Update branch request: vendorId={}, branchId={}", vendorId, branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = branchService.updateBranch(vendorId, branchId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/branches/{branchId}/upload")
    public ResponseEntity<BranchResponse> uploadBranchFile(
            @PathVariable UUID branchId,
            @RequestParam(required = false) String imageType,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String issueDate,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String fileUrl) {
        
        log.info("Upload file request for branch: {}, imageType={}, documentType={}", branchId, imageType, documentType);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Mock S3 URL - in production, this would be uploaded to S3
        String uploadedUrl = fileUrl != null ? fileUrl : 
            "https://s3.amazonaws.com/tea-snacks/branches/" + branchId + "/" + 
            (imageType != null ? imageType : documentType) + ".png";
        
        BranchResponse response;
        if (imageType != null) {
            response = branchService.uploadBranchImage(branchId, imageType, uploadedUrl, requestingUserId);
        } else if (documentType != null) {
            response = branchService.uploadBranchDocument(branchId, documentType, documentNumber, 
                issueDate, expiryDate, uploadedUrl, requestingUserId);
        } else {
            throw new IllegalArgumentException("Either imageType or documentType must be provided");
        }
        
        return ResponseEntity.ok(response);
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
