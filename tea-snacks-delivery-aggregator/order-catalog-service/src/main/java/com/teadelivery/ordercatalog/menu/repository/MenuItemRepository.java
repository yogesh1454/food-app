package com.teadelivery.ordercatalog.menu.repository;

import com.teadelivery.ordercatalog.menu.model.MenuItem;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByBranchAndIsDeletedFalse(VendorBranch branch);

    Page<MenuItem> findByBranchAndIsDeletedFalse(VendorBranch branch, Pageable pageable);

    Page<MenuItem> findByBranchAndCategoryAndIsDeletedFalse(VendorBranch branch, String category, Pageable pageable);

    Page<MenuItem> findByBranchAndIsAvailableTrueAndIsDeletedFalse(VendorBranch branch, Pageable pageable);

    Optional<MenuItem> findByMenuItemIdAndIsDeletedFalse(Long menuItemId);

    @Query("SELECT COUNT(m) FROM MenuItem m WHERE m.branch = :branch AND m.isAvailable = true AND m.isDeleted = false")
    long countAvailableItems(VendorBranch branch);

    /**
     * Find the next available gallery index for a menu item.
     * Parses JSONB keys like 'gallery_1', 'gallery_2' and returns max + 1.
     * Returns 1 if no gallery images exist.
     */
    @Query(value = """
            SELECT COALESCE(
                MAX(
                    CAST(
                        SUBSTRING(key FROM 'gallery_(\\d+)') AS INTEGER
                    )
                ), 0
            ) + 1
            FROM menu_items,
            LATERAL jsonb_object_keys(images) AS key
            WHERE menu_item_id = :menuItemId
            AND key ~ '^gallery_\\d+$'
            """, nativeQuery = true)
    Integer findNextGalleryIndex(@Param("menuItemId") Long menuItemId);
}
