package com.teadelivery.ordercatalog.vendor.controller;

import com.teadelivery.ordercatalog.vendor.dto.VendorRegistrationRequest;
import com.teadelivery.ordercatalog.vendor.dto.VendorResponse;
import com.teadelivery.ordercatalog.vendor.dto.VendorUpdateRequest;
import com.teadelivery.ordercatalog.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendors")
@Slf4j
@RequiredArgsConstructor
public class VendorController {
    
    private final VendorService vendorService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VendorResponse> registerVendor(
            @Valid @RequestBody VendorRegistrationRequest request) {
        
        log.info("Register vendor request: {}", request.getCompanyName());
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        VendorResponse response = vendorService.registerVendor(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorResponse> getVendor(
            @PathVariable UUID vendorId) {
        
        log.info("Get vendor request: {}", vendorId);
        
        VendorResponse response = vendorService.getVendor(vendorId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{vendorId}")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorUpdateRequest request) {
        
        log.info("Update vendor request: {}", vendorId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        VendorResponse response = vendorService.updateVendor(vendorId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
}
