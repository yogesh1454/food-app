package com.teadelivery.ordercatalog.search.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.search.event.SearchIndexEvent;
import com.teadelivery.ordercatalog.search.service.SearchCacheService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * SQS listener that consumes search index events and updates search tables.
 * Events are published by SearchIndexEventPublisher when vendor/menu data
 * changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "features.sqs.search-sync.enabled", havingValue = "true")
public class SearchEventConsumer {

    private final VendorSearchIndexService vendorIndexService;
    private final MenuItemSearchIndexService menuItemIndexService;
    private final SearchCacheService cacheService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to search index queue and process events.
     * SQS handles retry and DLQ automatically based on queue configuration.
     */
    @SqsListener("${aws.sqs.queues.search-index}")
    public void handleSearchIndexEvent(String messageBody) {
        try {
            SearchIndexEvent event = objectMapper.readValue(messageBody, SearchIndexEvent.class);

            log.info("Received search index event: eventType={}, entityType={}, entityId={}",
                    event.getEventType(), event.getEntityType(), event.getEntityId());

            processEvent(event);

            log.info("Successfully processed search index event: eventType={}, entityId={}",
                    event.getEventType(), event.getEntityId());

        } catch (Exception e) {
            log.error("Failed to process search index event", e);
            // Throw exception to trigger SQS retry mechanism
            throw new RuntimeException("Failed to process search index event", e);
        }
    }

    private void processEvent(SearchIndexEvent event) {
        switch (event.getEventType()) {
            case SearchIndexEvent.VENDOR_CREATED, SearchIndexEvent.VENDOR_UPDATED -> {
                vendorIndexService.syncVendor(event.getEntityId());
                invalidateVendorCache(event.getEntityId());
            }
            case SearchIndexEvent.VENDOR_DELETED -> {
                vendorIndexService.deleteFromIndex(event.getEntityId());
                invalidateVendorCache(event.getEntityId());
            }
            case SearchIndexEvent.MENU_ITEM_CREATED, SearchIndexEvent.MENU_ITEM_UPDATED -> {
                menuItemIndexService.syncMenuItem(event.getEntityId());
                invalidateMenuItemCache(event.getEntityId());
            }
            case SearchIndexEvent.MENU_ITEM_DELETED -> {
                menuItemIndexService.deleteFromIndex(event.getEntityId());
                invalidateMenuItemCache(event.getEntityId());
            }
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void invalidateVendorCache(Long branchId) {
        try {
            cacheService.invalidateByPattern("search:*");
            log.debug("Invalidated cache for vendor: branchId={}", branchId);
        } catch (Exception e) {
            log.warn("Failed to invalidate cache for vendor: branchId={}", branchId, e);
            // Don't fail the event processing if cache invalidation fails
        }
    }

    private void invalidateMenuItemCache(Long menuItemId) {
        try {
            cacheService.invalidateByPattern("search:*");
            log.debug("Invalidated cache for menu item: menuItemId={}", menuItemId);
        } catch (Exception e) {
            log.warn("Failed to invalidate cache for menu item: menuItemId={}", menuItemId, e);
            // Don't fail the event processing if cache invalidation fails
        }
    }
}
