package com.teadelivery.ordercatalog.search.sync;

import com.teadelivery.ordercatalog.common.exception.MenuItemNotFoundException;
import com.teadelivery.ordercatalog.menu.model.MenuItem;
import com.teadelivery.ordercatalog.menu.repository.MenuItemRepository;
import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import com.teadelivery.ordercatalog.search.repository.SearchMenuItemRepository;
import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service to sync menu item data to search_menu_items table.
 * Called by SearchEventConsumer when menu item events are received.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemSearchIndexService {

    private final MenuItemRepository menuItemRepository;
    private final SearchMenuItemRepository searchMenuItemRepository;

    // SRID 4326 = WGS 84 (standard for GPS coordinates)
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Sync a menu item to the search index.
     * Fetches fresh data from transactional table and updates search table.
     */
    @Transactional
    public void syncMenuItem(Long menuItemId) {
        log.info("Syncing menu item to search index: menuItemId={}", menuItemId);

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found: " + menuItemId));

        SearchMenuItem searchMenuItem = mapToSearchMenuItem(menuItem);
        searchMenuItemRepository.save(searchMenuItem);

        log.info("Successfully synced menu item to search index: menuItemId={}", menuItemId);
    }

    /**
     * Bulk sync all menu items to search index.
     * Used for initial population or full re-sync.
     */
    @Transactional
    public void syncAllMenuItems() {
        log.info("Starting bulk sync of all menu items to search index");

        List<MenuItem> allItems = menuItemRepository.findAll();
        int count = 0;

        for (MenuItem item : allItems) {
            try {
                SearchMenuItem searchMenuItem = mapToSearchMenuItem(item);
                searchMenuItemRepository.save(searchMenuItem);
                count++;
            } catch (Exception e) {
                log.error("Failed to sync menu item: menuItemId={}", item.getMenuItemId(), e);
            }
        }

        log.info("Bulk sync completed: {} menu items synced", count);
    }

    /**
     * Delete a menu item from the search index.
     */
    @Transactional
    public void deleteFromIndex(Long menuItemId) {
        log.info("Deleting menu item from search index: menuItemId={}", menuItemId);
        searchMenuItemRepository.deleteById(menuItemId);
        log.info("Successfully deleted menu item from search index: menuItemId={}", menuItemId);
    }

    /**
     * Map MenuItem entity to SearchMenuItem entity.
     */
    private SearchMenuItem mapToSearchMenuItem(MenuItem item) {
        VendorBranch branch = item.getBranch();
        Vendor vendor = branch != null ? branch.getVendor() : null;

        // Create PostGIS Point from branch lat/lng
        Point branchLocation = null;
        if (branch != null && branch.getLatitude() != null && branch.getLongitude() != null) {
            Coordinate coordinate = new Coordinate(
                    branch.getLongitude().doubleValue(),
                    branch.getLatitude().doubleValue());
            branchLocation = GEOMETRY_FACTORY.createPoint(coordinate);
        }

        SearchMenuItem searchMenuItem = new SearchMenuItem();
        searchMenuItem.setMenuItemId(item.getMenuItemId());
        searchMenuItem.setItemName(item.getName());
        searchMenuItem.setDescription(item.getDescription());
        searchMenuItem.setPrice(item.getPrice());
        searchMenuItem.setCategory(item.getCategory());

        // Vendor context (denormalized)
        if (branch != null) {
            searchMenuItem.setBranchId(branch.getBranchId());
            searchMenuItem.setBranchName(branch.getBranchName());
            searchMenuItem.setBranchLocation(branchLocation);
            searchMenuItem.setBranchLatitude(branch.getLatitude());
            searchMenuItem.setBranchLongitude(branch.getLongitude());
            searchMenuItem.setCity(branch.getCity());
        }

        if (vendor != null) {
            searchMenuItem.setVendorId(vendor.getVendorId());
            // Use brandName or companyName as vendor name
            searchMenuItem
                    .setVendorName(vendor.getBrandName() != null ? vendor.getBrandName() : vendor.getCompanyName());
        }

        // Availability
        searchMenuItem.setIsAvailable(item.getIsAvailable());
        searchMenuItem.setIsDeleted(item.getIsDeleted() != null ? item.getIsDeleted() : false);

        // Tags (String[])
        searchMenuItem.setTags(item.getTags());

        // Sync metadata
        searchMenuItem.setLastSyncedAt(Instant.now());

        return searchMenuItem;
    }
}
