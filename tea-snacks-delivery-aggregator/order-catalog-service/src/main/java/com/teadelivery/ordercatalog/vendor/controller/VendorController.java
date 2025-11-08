package com.teadelivery.ordercatalog.vendor.controller;

import com.teadelivery.ordercatalog.vendor.dto.BranchResponse;
import com.teadelivery.ordercatalog.vendor.dto.VendorRegistrationRequest;
import com.teadelivery.ordercatalog.vendor.dto.VendorResponse;
import com.teadelivery.ordercatalog.vendor.dto.VendorUpdateRequest;
import com.teadelivery.ordercatalog.vendor.service.BranchOnboardingService;
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
    private final BranchOnboardingService branchService;
    
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
            @PathVariable Long vendorId) {
        
        log.info("Get vendor request: {}", vendorId);
        
        VendorResponse response = vendorService.getVendor(vendorId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{vendorId}")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpdateRequest request) {
        
        log.info("Update vendor request: {}", vendorId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        VendorResponse response = vendorService.updateVendor(vendorId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unified upload endpoint for vendor and branch files
     * 
     * Examples:
     * - Vendor logo: POST /api/v1/vendors/{vendorId}/upload?target=vendor&fileType=logo
     * - Branch image: POST /api/v1/vendors/{vendorId}/upload?target=branch&branchId={branchId}&fileType=storefront
     * - Branch document: POST /api/v1/vendors/{vendorId}/upload?target=branch&branchId={branchId}&fileType=fssai&documentNumber=12345
     */
    @PostMapping("/{vendorId}/upload")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long vendorId,
            @RequestParam String target,  // "vendor" or "branch"
            @RequestParam String fileType,  // "logo", "cover", "fssai", "gst", etc.
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String issueDate,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String fileUrl) {
        
        log.info("Upload file request: vendorId={}, target={}, fileType={}, branchId={}", 
                 vendorId, target, fileType, branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        if ("vendor".equalsIgnoreCase(target)) {
            // Upload vendor file (logo, cover photo, etc.)
            String uploadedUrl = fileUrl != null ? fileUrl : 
                "https://s3.amazonaws.com/tea-snacks/vendors/" + vendorId + "/" + fileType + ".png";
            
            VendorResponse response = vendorService.uploadVendorImage(vendorId, fileType, uploadedUrl, requestingUserId);
            return ResponseEntity.ok(response);
            
        } else if ("branch".equalsIgnoreCase(target)) {
            // Upload branch file (image or document)
            if (branchId == null) {
                throw new IllegalArgumentException("branchId is required when target=branch");
            }
            
            String uploadedUrl = fileUrl != null ? fileUrl : 
                "https://s3.amazonaws.com/tea-snacks/branches/" + branchId + "/" + fileType + ".png";
            
            // Determine if it's a document or image based on fileType
            boolean isDocument = fileType.matches("(?i)(fssai|gst|shop_act|id_proof|trade_license)");
            
            BranchResponse response;
            if (isDocument) {
                response = branchService.uploadBranchDocument(branchId, fileType, documentNumber, 
                    issueDate, expiryDate, uploadedUrl, requestingUserId);
            } else {
                response = branchService.uploadBranchImage(branchId, fileType, uploadedUrl, requestingUserId);
            }
            
            return ResponseEntity.ok(response);
            
        } else {
            throw new IllegalArgumentException("Invalid target. Must be 'vendor' or 'branch'");
        }
    }
}
