package com.teadelivery.ordercatalog.search.sync;

import com.teadelivery.ordercatalog.common.exception.BranchNotFoundException;
import com.teadelivery.ordercatalog.search.model.SearchVendor;
import com.teadelivery.ordercatalog.search.repository.SearchVendorRepository;
import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
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
import java.util.Map;

/**
 * Service to sync vendor branch data to search_vendors table.
 * Called by SearchEventConsumer when vendor events are received.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorSearchIndexService {

    private final VendorBranchRepository branchRepository;
    private final SearchVendorRepository searchVendorRepository;

    // SRID 4326 = WGS 84 (standard for GPS coordinates)
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Sync a vendor branch to the search index.
     * Fetches fresh data from transactional table and updates search table.
     */
    @Transactional
    public void syncVendor(Long branchId) {
        log.info("Syncing vendor to search index: branchId={}", branchId);

        VendorBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found: " + branchId));

        SearchVendor searchVendor = mapToSearchVendor(branch);
        searchVendorRepository.save(searchVendor);

        log.info("Successfully synced vendor to search index: branchId={}", branchId);
    }

    /**
     * Bulk sync all vendors to search index.
     * Used for initial population or full re-sync.
     */
    @Transactional
    public void syncAllVendors() {
        log.info("Starting bulk sync of all vendors to search index");

        List<VendorBranch> allBranches = branchRepository.findAll();
        int count = 0;

        for (VendorBranch branch : allBranches) {
            try {
                SearchVendor searchVendor = mapToSearchVendor(branch);
                searchVendorRepository.save(searchVendor);
                count++;
            } catch (Exception e) {
                log.error("Failed to sync vendor: branchId={}", branch.getBranchId(), e);
            }
        }

        log.info("Bulk sync completed: {} vendors synced", count);
    }

    /**
     * Delete a vendor from the search index.
     */
    @Transactional
    public void deleteFromIndex(Long branchId) {
        log.info("Deleting vendor from search index: branchId={}", branchId);
        searchVendorRepository.deleteById(branchId);
        log.info("Successfully deleted vendor from search index: branchId={}", branchId);
    }

    /**
     * Map VendorBranch entity to SearchVendor entity.
     */
    private SearchVendor mapToSearchVendor(VendorBranch branch) {
        Vendor vendor = branch.getVendor();

        // Create PostGIS Point from lat/lng
        Point location = null;
        if (branch.getLatitude() != null && branch.getLongitude() != null) {
            Coordinate coordinate = new Coordinate(
                    branch.getLongitude().doubleValue(),
                    branch.getLatitude().doubleValue());
            location = GEOMETRY_FACTORY.createPoint(coordinate);
        }

        SearchVendor searchVendor = new SearchVendor();
        searchVendor.setBranchId(branch.getBranchId());
        searchVendor.setVendorId(vendor != null ? vendor.getVendorId() : null);
        // Use brandName or companyName as vendor name
        searchVendor.setVendorName(
                vendor != null ? (vendor.getBrandName() != null ? vendor.getBrandName() : vendor.getCompanyName())
                        : null);
        searchVendor.setBranchName(branch.getBranchName());
        searchVendor.setDisplayName(branch.getDisplayName() != null ? branch.getDisplayName() : branch.getBranchName());
        searchVendor.setLocation(location);
        searchVendor.setLatitude(branch.getLatitude());
        searchVendor.setLongitude(branch.getLongitude());
        searchVendor.setCity(branch.getCity());
        searchVendor.setAddress(branch.getAddress());

        // Tags (String[])
        searchVendor.setTags(branch.getTags());

        searchVendor.setRating(branch.getRating());
        searchVendor.setTotalRatings(branch.getTotalReviews());
        searchVendor.setIsOpen(branch.getIsOpen());
        searchVendor.setIsActive(branch.getIsActive());
        searchVendor.setOrderCount(branch.getTotalOrders());

        // Images (JSONB) - Merge vendor and branch images into single Map
        // Structure: { "logo": {...}, "cover": {...}, "storefront": {...} }
        Map<String, Object> mergedImages = new java.util.HashMap<>();

        // Add vendor images (logo, cover)
        if (vendor != null && vendor.getImages() != null && !vendor.getImages().isEmpty()) {
            mergedImages.putAll(vendor.getImages());
        }

        // Add branch images (storefront, interior, etc.)
        if (branch.getImages() != null && !branch.getImages().isEmpty()) {
            mergedImages.putAll(branch.getImages());
        }

        if (!mergedImages.isEmpty()) {
            searchVendor.setImages(mergedImages);

            // Set primary image URL (prefer vendor logo medium, fallback to branch primary
            // medium)
            String primaryImageUrl = null;
            if (vendor != null && vendor.getImages() != null) {
                Object logoData = vendor.getImages().get("logo");
                if (logoData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> logoMap = (Map<String, Object>) logoData;
                    // New structure: direct size -> URL mapping
                    String mediumUrl = (String) logoMap.get("medium");
                    if (mediumUrl != null) {
                        primaryImageUrl = mediumUrl;
                    }
                }
            }

            // Fallback to branch primary image
            if (primaryImageUrl == null && branch.getImages() != null) {
                Object primaryData = branch.getImages().get("primary");
                if (primaryData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> primaryMap = (Map<String, Object>) primaryData;
                    String mediumUrl = (String) primaryMap.get("medium");
                    if (mediumUrl != null) {
                        primaryImageUrl = mediumUrl;
                    }
                }
            }

            if (primaryImageUrl != null) {
                searchVendor.setPrimaryImage(primaryImageUrl);
            }
        }

        searchVendor.setLastSyncedAt(Instant.now());

        return searchVendor;
    }
}
