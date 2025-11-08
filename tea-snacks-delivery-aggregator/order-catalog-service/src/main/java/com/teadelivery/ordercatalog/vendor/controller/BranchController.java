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
            @PathVariable Long vendorId,
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Create branch request for vendor: {}", vendorId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = branchService.createBranch(vendorId, request, requestingUserId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/branches/{branchId}")
    public ResponseEntity<BranchResponse> getBranch(
            @PathVariable Long branchId) {
        
        log.info("Get branch request: {}", branchId);
        
        BranchResponse response = branchService.getBranch(branchId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/vendors/{vendorId}/branches/{branchId}")
    public ResponseEntity<BranchResponse> updateBranch(
            @PathVariable Long vendorId,
            @PathVariable Long branchId,
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Update branch request: vendorId={}, branchId={}", vendorId, branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = branchService.updateBranch(vendorId, branchId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/branches/{branchId}/status")
    public ResponseEntity<BranchResponse> toggleStatus(
            @PathVariable Long branchId,
            @Valid @RequestBody BranchStatusRequest request) {
        
        log.info("Toggle branch status request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        BranchResponse response = availabilityService.toggleBranchStatus(branchId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
}
