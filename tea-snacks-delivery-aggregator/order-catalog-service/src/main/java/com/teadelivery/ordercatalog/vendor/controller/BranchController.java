package com.teadelivery.ordercatalog.vendor.controller;

import com.teadelivery.ordercatalog.common.exception.ErrorResponse;
import com.teadelivery.ordercatalog.common.exception.ValidationErrorResponse;
import com.teadelivery.ordercatalog.vendor.dto.*;
import com.teadelivery.ordercatalog.vendor.service.BranchAvailabilityService;
import com.teadelivery.ordercatalog.vendor.service.BranchOnboardingService;
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
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "APIs for branch onboarding, configuration, and status management")
public class BranchController {
    
    private final BranchOnboardingService branchService;
    private final BranchAvailabilityService availabilityService;
    
    @Operation(
        summary = "Create a new branch",
        description = "Creates a new branch for a vendor. This is used for both first branch and subsequent branches."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Branch successfully created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BranchResponse.class),
                examples = @ExampleObject(
                    value = "{\"branchId\": 1, \"vendorId\": 1, \"branchName\": \"Chai Express - Koramangala\", \"city\": \"Bangalore\", \"isActive\": false, \"onboardingStatus\": \"PENDING\", \"createdAt\": \"2024-11-08T12:30:45\"}"
                )
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
            description = "Not authorized to create branch for this vendor",
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
    @PostMapping("/vendors/{vendorId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse createBranch(
            @Parameter(description = "Vendor ID", example = "1", required = true)
            @PathVariable Long vendorId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Branch creation details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = BranchCreateRequest.class),
                    examples = @ExampleObject(
                        value = "{\"branchName\": \"Chai Express - Koramangala\", \"city\": \"Bangalore\", \"address\": {\"street\": \"100 Feet Road\", \"area\": \"Koramangala\", \"city\": \"Bangalore\", \"state\": \"Karnataka\", \"pincode\": \"560034\"}, \"latitude\": 12.9352, \"longitude\": 77.6245, \"branchPhone\": \"9876543210\", \"branchEmail\": \"koramangala@chaiexpress.com\"}"
                    )
                )
            )
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Create branch request for vendor: {}", vendorId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        return branchService.createBranch(vendorId, request, requestingUserId);
    }
    
    @Operation(
        summary = "Get branch details",
        description = "Retrieves complete branch information including status, operating hours, and preferences"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Branch found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BranchResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Branch not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"timestamp\": \"2024-11-08T12:30:45\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Branch not found\", \"path\": \"/api/v1/branches/999\"}"
                )
            )
        )
    })
    @GetMapping("/branches/{branchId}")
    public BranchResponse getBranch(
            @Parameter(description = "Branch ID", example = "1", required = true)
            @PathVariable Long branchId) {
        
        log.info("Get branch request: {}", branchId);
        
        return branchService.getBranch(branchId);
    }
    
    @Operation(
        summary = "Update branch details",
        description = "Updates branch information including address, contact details, preferences, and operating hours"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Branch successfully updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BranchResponse.class)
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
            description = "Not authorized to update this branch",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Branch or vendor not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/vendors/{vendorId}/branches/{branchId}")
    public BranchResponse updateBranch(
            @Parameter(description = "Vendor ID", example = "1", required = true)
            @PathVariable Long vendorId,
            @Parameter(description = "Branch ID", example = "1", required = true)
            @PathVariable Long branchId,
            @Valid @RequestBody BranchCreateRequest request) {
        
        log.info("Update branch request: vendorId={}, branchId={}", vendorId, branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        return branchService.updateBranch(vendorId, branchId, request, requestingUserId);
    }
    
    @Operation(
        summary = "Toggle branch online/offline status",
        description = "Updates the branch operational status (isOpen). Used by vendors to temporarily close/open their branch."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Branch status successfully updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = BranchResponse.class),
                examples = @ExampleObject(
                    value = "{\"branchId\": 1, \"branchName\": \"Chai Express - Koramangala\", \"isOpen\": true, \"isActive\": true, \"onboardingStatus\": \"APPROVED\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid status value",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ValidationErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to update this branch status",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Branch not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/branches/{branchId}/status")
    public BranchResponse toggleStatus(
            @Parameter(description = "Branch ID", example = "1", required = true)
            @PathVariable Long branchId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Branch status update",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = BranchStatusRequest.class),
                    examples = @ExampleObject(
                        value = "{\"isOpen\": true}"
                    )
                )
            )
            @Valid @RequestBody BranchStatusRequest request) {
        
        log.info("Toggle branch status request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        return availabilityService.toggleBranchStatus(branchId, request, requestingUserId);
    }
}
