package com.teadelivery.ordercatalog.search.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base event for search index synchronization.
 * Published to SNS topic when vendor/menu transactional data changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndexEvent {

    /**
     * Unique event identifier
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Event type: VENDOR_CREATED, VENDOR_UPDATED, VENDOR_DELETED,
     * MENU_ITEM_CREATED, MENU_ITEM_UPDATED, MENU_ITEM_DELETED
     */
    private String eventType;

    /**
     * Timestamp when event was created
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Entity ID (branchId for vendors, menuItemId for menu items)
     */
    private Long entityId;

    /**
     * Entity type: VENDOR or MENU_ITEM
     */
    private String entityType;

    /**
     * Event payload (JSON string of VendorIndexPayload or MenuItemIndexPayload)
     */
    private String payload;

    /**
     * Version for optimistic locking / deduplication
     */
    @Builder.Default
    private Integer version = 1;

    // Event type constants
    public static final String VENDOR_CREATED = "VENDOR_CREATED";
    public static final String VENDOR_UPDATED = "VENDOR_UPDATED";
    public static final String VENDOR_DELETED = "VENDOR_DELETED";
    public static final String MENU_ITEM_CREATED = "MENU_ITEM_CREATED";
    public static final String MENU_ITEM_UPDATED = "MENU_ITEM_UPDATED";
    public static final String MENU_ITEM_DELETED = "MENU_ITEM_DELETED";

    // Entity type constants
    public static final String ENTITY_VENDOR = "VENDOR";
    public static final String ENTITY_MENU_ITEM = "MENU_ITEM";
}
