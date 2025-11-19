package com.teadelivery.ordercatalog.vendor.controller;

import com.teadelivery.ordercatalog.common.exception.ErrorResponse;
import com.teadelivery.ordercatalog.common.exception.ValidationErrorResponse;
import com.teadelivery.ordercatalog.vendor.dto.BranchResponse;
import com.teadelivery.ordercatalog.vendor.dto.VendorRegistrationRequest;
import com.teadelivery.ordercatalog.vendor.dto.VendorResponse;
import com.teadelivery.ordercatalog.vendor.dto.VendorUpdateRequest;
import com.teadelivery.ordercatalog.vendor.service.BranchOnboardingService;
import com.teadelivery.ordercatalog.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendors")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Vendor Management", description = "APIs for vendor registration, profile management, and file uploads")
public class VendorController {
    
    private final VendorService vendorService;
    private final BranchOnboardingService branchService;
    
    @Operation(
        summary = "Register a new vendor",
        description = "Creates a new vendor account with company details. This is the first step in vendor onboarding."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Vendor successfully registered",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = VendorResponse.class),
                examples = @ExampleObject(
                    value = "{\"vendorId\": 1, \"companyName\": \"Chai Express Pvt Ltd\", \"brandName\": \"Chai Express\", \"companyEmail\": \"contact@chaiexpress.com\", \"companyPhone\": \"9876543210\", \"createdAt\": \"2024-11-08T12:30:45\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ValidationErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"timestamp\": \"2024-11-08T12:30:45\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"Validation failed\", \"path\": \"/api/v1/vendors\", \"validationErrors\": {\"companyEmail\": \"must be a well-formed email address\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Vendor already exists or email already registered",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"timestamp\": \"2024-11-08T12:30:45\", \"status\": 409, \"error\": \"Conflict\", \"message\": \"Email already registered\", \"path\": \"/api/v1/vendors\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse registerVendor(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Vendor registration details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = VendorRegistrationRequest.class),
                    examples = @ExampleObject(
                        value = "{\"companyName\": \"Chai Express Pvt Ltd\", \"brandName\": \"Chai Express\", \"legalEntityName\": \"Chai Express Private Limited\", \"companyEmail\": \"contact@chaiexpress.com\", \"companyPhone\": \"9876543210\", \"panNumber\": \"ABCDE1234F\", \"gstNumber\": \"29ABCDE1234F1Z5\"}"
                    )
                )
            )
            @Valid @RequestBody VendorRegistrationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        
        log.info("Register vendor request: {}", request.getCompanyName());
        
        // Use header userId for testing, fallback to hardcoded for development
        // In production, this would come from JWT authentication
        UUID userId = (userIdHeader != null && !userIdHeader.isEmpty()) 
            ? UUID.fromString(userIdHeader)
            : UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        log.debug("Using userId: {}", userId);
        
        return vendorService.registerVendor(request, userId);
    }
    
    @Operation(
        summary = "Get vendor details",
        description = "Retrieves complete vendor information by vendor ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Vendor found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = VendorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Vendor not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"timestamp\": \"2024-11-08T12:30:45\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Vendor not found\", \"path\": \"/api/v1/vendors/999\"}"
                )
            )
        )
    })
    @GetMapping("/{vendorId}")
    public VendorResponse getVendor(
            @Parameter(description = "Vendor ID", example = "1", required = true)
            @PathVariable Long vendorId) {
        
        log.info("Get vendor request: {}", vendorId);
        
        return vendorService.getVendor(vendorId);
    }
    
    @Operation(
        summary = "Update vendor details",
        description = "Updates vendor information. Only the vendor owner can update their details."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Vendor successfully updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = VendorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ValidationErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to update this vendor",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Vendor not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{vendorId}")
    public VendorResponse updateVendor(
            @Parameter(description = "Vendor ID", example = "1", required = true)
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpdateRequest request) {
        
        log.info("Update vendor request: {}", vendorId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        return vendorService.updateVendor(vendorId, request, requestingUserId);
    }
    
    @Operation(
        summary = "Upload vendor or branch files",
        description = """
            Unified endpoint for uploading vendor and branch files (images and documents).
            
            **For Vendor Files:**
            - target=vendor, fileType=logo|cover
            
            **For Branch Images:**
            - target=branch, branchId=123, fileType=storefront|interior|menu_board
            
            **For Branch Documents:**
            - target=branch, branchId=123, fileType=fssai|gst|shop_act|id_proof
            - Include documentNumber, issueDate, expiryDate for documents
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "File uploaded successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(oneOf = {VendorResponse.class, BranchResponse.class})
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters or missing required fields",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"timestamp\": \"2024-11-08T12:30:45\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"branchId is required when target=branch\", \"path\": \"/api/v1/vendors/1/upload\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Vendor or branch not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/{vendorId}/upload")
    public Object uploadFile(
            @Parameter(description = "Vendor ID", example = "1", required = true)
            @PathVariable Long vendorId,
            
            @Parameter(description = "Upload target: 'vendor' or 'branch'", example = "vendor", required = true)
            @RequestParam String target,
            
            @Parameter(description = "File type: logo, cover, fssai, gst, shop_act, id_proof, storefront, interior, menu_board", example = "logo", required = true)
            @RequestParam String fileType,
            
            @Parameter(description = "Branch ID (required when target=branch)", example = "1")
            @RequestParam(required = false) Long branchId,
            
            @Parameter(description = "Document number (required for document uploads)", example = "12345678901234")
            @RequestParam(required = false) String documentNumber,
            
            @Parameter(description = "Document issue date (required for document uploads)", example = "2024-01-01")
            @RequestParam(required = false) String issueDate,
            
            @Parameter(description = "Document expiry date (required for document uploads)", example = "2029-01-01")
            @RequestParam(required = false) String expiryDate,
            
            @Parameter(description = "File URL (for testing; in production, file will be uploaded to S3)", example = "https://s3.amazonaws.com/tea-snacks/vendors/1/logo.png")
            @RequestParam(required = false) String fileUrl) {
        
        log.info("Upload file request: vendorId={}, target={}, fileType={}, branchId={}", 
                 vendorId, target, fileType, branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        if ("vendor".equalsIgnoreCase(target)) {
            // Upload vendor file (logo, cover photo, etc.)
            String uploadedUrl = fileUrl != null ? fileUrl : 
                "https://s3.amazonaws.com/tea-snacks/vendors/" + vendorId + "/" + fileType + ".png";
            
            return vendorService.uploadVendorImage(vendorId, fileType, uploadedUrl, requestingUserId);
            
        } else if ("branch".equalsIgnoreCase(target)) {
            // Upload branch file (image or document)
            if (branchId == null) {
                throw new IllegalArgumentException("branchId is required when target=branch");
            }
            
            String uploadedUrl = fileUrl != null ? fileUrl : 
                "https://s3.amazonaws.com/tea-snacks/branches/" + branchId + "/" + fileType + ".png";
            
            // Determine if it's a document or image based on fileType
            boolean isDocument = fileType.matches("(?i)(fssai|gst|shop_act|id_proof|trade_license)");
            
            if (isDocument) {
                return branchService.uploadBranchDocument(branchId, fileType, documentNumber, 
                    issueDate, expiryDate, uploadedUrl, requestingUserId);
            } else {
                return branchService.uploadBranchImage(branchId, fileType, uploadedUrl, requestingUserId);
            }
            
        } else {
            throw new IllegalArgumentException("Invalid target. Must be 'vendor' or 'branch'");
        }
    }
}
