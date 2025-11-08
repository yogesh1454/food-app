package com.teadelivery.ordercatalog.menu.controller;

import com.teadelivery.ordercatalog.common.exception.ErrorResponse;
import com.teadelivery.ordercatalog.common.exception.ValidationErrorResponse;
import com.teadelivery.ordercatalog.menu.dto.MenuItemCreateRequest;
import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.dto.MenuItemUpdateRequest;
import com.teadelivery.ordercatalog.menu.service.MenuService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menu-items")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Menu Management", description = "APIs for managing branch menu items, categories, and availability")
public class MenuController {
    
    private final MenuService menuService;
    
    @Operation(
        summary = "Create a new menu item",
        description = "Adds a new item to the branch menu with pricing, category, and availability details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Menu item successfully created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MenuItemResponse.class),
                examples = @ExampleObject(
                    value = "{\"menuItemId\": \"550e8400-e29b-41d4-a716-446655440001\", \"branchId\": 1, \"name\": \"Masala Chai\", \"description\": \"Traditional Indian spiced tea\", \"price\": 20.00, \"category\": \"Beverages\", \"isAvailable\": true, \"preparationTimeMinutes\": 5}"
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
            description = "Not authorized to modify this branch's menu",
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
    @PostMapping("/branches/{branchId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(
            @Parameter(description = "Branch ID", example = "1", required = true)
            @PathVariable Long branchId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Menu item details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = MenuItemCreateRequest.class),
                    examples = @ExampleObject(
                        value = "{\"name\": \"Masala Chai\", \"description\": \"Traditional Indian spiced tea\", \"price\": 20.00, \"category\": \"Beverages\", \"isAvailable\": true, \"preparationTimeMinutes\": 5, \"tags\": [\"hot\", \"popular\", \"vegetarian\"]}"
                    )
                )
            )
            @Valid @RequestBody MenuItemCreateRequest request) {
        
        log.info("Create menu item request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        return menuService.createMenuItem(branchId, request, requestingUserId);
    }
    
    @Operation(
        summary = "Get menu item details",
        description = "Retrieves complete information about a specific menu item"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menu item found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MenuItemResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu item not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"timestamp\": \"2024-11-08T12:30:45\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Menu item not found\", \"path\": \"/api/v1/menu-items/550e8400-e29b-41d4-a716-446655440001\"}"
                )
            )
        )
    })
    @GetMapping("/{menuItemId}")
    public MenuItemResponse getMenuItem(
            @Parameter(description = "Menu Item ID", example = "1", required = true)
            @PathVariable Long menuItemId) {
        
        log.info("Get menu item request: {}", menuItemId);
        
        return menuService.getMenuItem(menuItemId);
    }
    
    @Operation(
        summary = "Get branch menu",
        description = "Retrieves all menu items for a branch with optional category filtering and pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menu items retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MenuItemResponse.class)
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
    @GetMapping("/branches/{branchId}")
    public List<MenuItemResponse> getBranchMenu(
            @Parameter(description = "Branch ID", example = "1", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Filter by category (optional)", example = "Beverages")
            @RequestParam(required = false) String category,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Get branch menu request for branch: {}", branchId);
        
        Pageable pageable = PageRequest.of(page, size);
        return menuService.getBranchMenu(branchId, category, pageable);
    }
    
    @Operation(
        summary = "Update menu item",
        description = "Updates menu item details including price, availability, and description"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Menu item successfully updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MenuItemResponse.class)
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
            description = "Not authorized to modify this menu item",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu item not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{menuItemId}")
    public MenuItemResponse updateMenuItem(
            @Parameter(description = "Menu Item ID", example = "1", required = true)
            @PathVariable Long menuItemId,
            @Valid @RequestBody MenuItemUpdateRequest request) {
        
        log.info("Update menu item request: {}", menuItemId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        return menuService.updateMenuItem(menuItemId, request, requestingUserId);
    }
    
    @Operation(
        summary = "Delete menu item",
        description = "Soft deletes a menu item (marks as deleted, doesn't remove from database)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Menu item successfully deleted"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to delete this menu item",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Menu item not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @Parameter(description = "Menu Item ID", example = "1", required = true)
            @PathVariable Long menuItemId) {
        
        log.info("Delete menu item request: {}", menuItemId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        menuService.deleteMenuItem(menuItemId, requestingUserId);
    }
}
