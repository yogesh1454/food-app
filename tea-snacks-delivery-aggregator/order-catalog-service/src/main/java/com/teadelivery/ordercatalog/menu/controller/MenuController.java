package com.teadelivery.ordercatalog.menu.controller;

import com.teadelivery.ordercatalog.menu.dto.MenuItemCreateRequest;
import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.dto.MenuItemUpdateRequest;
import com.teadelivery.ordercatalog.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menu-items")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuService menuService;
    
    @PostMapping("/branches/{branchId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable Long branchId,
            @Valid @RequestBody MenuItemCreateRequest request) {
        
        log.info("Create menu item request for branch: {}", branchId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        MenuItemResponse response = menuService.createMenuItem(branchId, request, requestingUserId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{menuItemId}")
    public ResponseEntity<MenuItemResponse> getMenuItem(
            @PathVariable UUID menuItemId) {
        
        log.info("Get menu item request: {}", menuItemId);
        
        MenuItemResponse response = menuService.getMenuItem(menuItemId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/branches/{branchId}")
    public ResponseEntity<List<MenuItemResponse>> getBranchMenu(
            @PathVariable Long branchId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Get branch menu request for branch: {}", branchId);
        
        Pageable pageable = PageRequest.of(page, size);
        List<MenuItemResponse> response = menuService.getBranchMenu(branchId, category, pageable);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{menuItemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable UUID menuItemId,
            @Valid @RequestBody MenuItemUpdateRequest request) {
        
        log.info("Update menu item request: {}", menuItemId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        MenuItemResponse response = menuService.updateMenuItem(menuItemId, request, requestingUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @PathVariable UUID menuItemId) {
        
        log.info("Delete menu item request: {}", menuItemId);
        
        // For now, using a hardcoded userId. In production, this would come from authentication
        UUID requestingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        menuService.deleteMenuItem(menuItemId, requestingUserId);
    }
}
