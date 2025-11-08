package com.teadelivery.ordercatalog.menu.service;

import com.teadelivery.ordercatalog.common.exception.BranchNotFoundException;
import com.teadelivery.ordercatalog.common.exception.MenuItemNotFoundException;
import com.teadelivery.ordercatalog.common.exception.UnauthorizedException;
import com.teadelivery.ordercatalog.menu.dto.MenuItemCreateRequest;
import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.dto.MenuItemUpdateRequest;
import com.teadelivery.ordercatalog.menu.mapper.MenuMapper;
import com.teadelivery.ordercatalog.menu.model.MenuItem;
import com.teadelivery.ordercatalog.menu.repository.MenuItemRepository;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {
    
    private final MenuItemRepository menuItemRepository;
    private final VendorBranchRepository branchRepository;
    private final MenuCacheService cacheService;
    
    @Transactional
    public MenuItemResponse createMenuItem(Long branchId, MenuItemCreateRequest request, UUID requestingUserId) {
        log.info("Creating menu item for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to modify this branch's menu");
        }
        
        MenuItem menuItem = new MenuItem();
        menuItem.setBranch(branch);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setIsAvailable(true);
        menuItem.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        menuItem.setMetadata(request.getMetadata());
        menuItem.setTags(request.getTags());
        menuItem.setImages(new HashMap<>());
        menuItem.setIsDeleted(false);
        
        MenuItem savedItem = menuItemRepository.save(menuItem);
        
        // Increment branch menu version
        branch.setMenuVersion(branch.getMenuVersion() + 1);
        branchRepository.save(branch);
        
        log.info("Menu item created: {} for branch: {}", savedItem.getMenuItemId(), branchId);
        return MenuMapper.toResponse(savedItem);
    }
    
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(Long menuItemId) {
        log.info("Fetching menu item: {}", menuItemId);
        
        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        return MenuMapper.toResponse(menuItem);
    }
    
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getBranchMenu(Long branchId, String category, Pageable pageable) {
        log.info("Fetching menu for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        Page<MenuItem> itemsPage;
        if (category != null) {
            itemsPage = menuItemRepository.findByBranchAndCategoryAndIsDeletedFalse(branch, category, pageable);
        } else {
            itemsPage = menuItemRepository.findByBranchAndIsDeletedFalse(branch, pageable);
        }
        
        return itemsPage.getContent().stream()
            .map(MenuMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public MenuItemResponse updateMenuItem(Long menuItemId, MenuItemUpdateRequest request, UUID requestingUserId) {
        log.info("Updating menu item: {}", menuItemId);
        
        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to modify this menu item");
        }
        
        if (request.getName() != null) {
            menuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            menuItem.setCategory(request.getCategory());
        }
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }
        if (request.getPreparationTimeMinutes() != null) {
            menuItem.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        }
        if (request.getMetadata() != null) {
            menuItem.setMetadata(request.getMetadata());
        }
        if (request.getTags() != null) {
            menuItem.setTags(request.getTags());
        }
        
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        
        // Increment branch menu version
        VendorBranch branch = menuItem.getBranch();
        branch.setMenuVersion(branch.getMenuVersion() + 1);
        branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchMenu(branch.getBranchId());
        cacheService.evictPopularItems(branch.getBranchId());
        
        log.info("Menu item updated: {}", menuItemId);
        return MenuMapper.toResponse(updatedItem);
    }
    
    @Transactional
    public void deleteMenuItem(Long menuItemId, UUID requestingUserId) {
        log.info("Deleting menu item: {}", menuItemId);
        
        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to delete this menu item");
        }
        
        // Soft delete
        menuItem.setIsDeleted(true);
        menuItem.setIsAvailable(false);
        menuItemRepository.save(menuItem);
        
        // Increment branch menu version
        VendorBranch branch = menuItem.getBranch();
        branch.setMenuVersion(branch.getMenuVersion() + 1);
        branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchMenu(branch.getBranchId());
        cacheService.evictPopularItems(branch.getBranchId());
        
        log.info("Menu item deleted: {}", menuItemId);
    }
}
