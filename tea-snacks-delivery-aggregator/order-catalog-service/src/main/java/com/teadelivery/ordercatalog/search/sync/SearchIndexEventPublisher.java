package com.teadelivery.ordercatalog.search.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.menu.model.MenuItem;
import com.teadelivery.ordercatalog.search.event.MenuItemIndexPayload;
import com.teadelivery.ordercatalog.search.event.SearchIndexEvent;
import com.teadelivery.ordercatalog.search.event.VendorIndexPayload;
import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishes search index events to SNS topic.
 * Events are then consumed by SQS listener to update search tables.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "features.sns.search-sync.enabled", havingValue = "true")
public class SearchIndexEventPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topics.search-index-events}")
    private String topicArn;

    // ==================== VENDOR EVENTS ====================

    /**
     * Publish event when a new vendor branch is created.
     */
    public void publishVendorCreated(VendorBranch branch) {
        publishVendorEvent(SearchIndexEvent.VENDOR_CREATED, branch);
    }

    /**
     * Publish event when a vendor branch is updated.
     */
    public void publishVendorUpdated(VendorBranch branch) {
        publishVendorEvent(SearchIndexEvent.VENDOR_UPDATED, branch);
    }

    /**
     * Publish event when a vendor branch is deleted.
     */
    public void publishVendorDeleted(Long branchId) {
        SearchIndexEvent event = SearchIndexEvent.builder()
                .eventType(SearchIndexEvent.VENDOR_DELETED)
                .entityType(SearchIndexEvent.ENTITY_VENDOR)
                .entityId(branchId)
                .payload(null) // No payload for delete
                .build();

        publishEvent(event);
    }

    // ==================== MENU ITEM EVENTS ====================

    /**
     * Publish event when a new menu item is created.
     */
    public void publishMenuItemCreated(MenuItem item) {
        publishMenuItemEvent(SearchIndexEvent.MENU_ITEM_CREATED, item);
    }

    /**
     * Publish event when a menu item is updated.
     */
    public void publishMenuItemUpdated(MenuItem item) {
        publishMenuItemEvent(SearchIndexEvent.MENU_ITEM_UPDATED, item);
    }

    /**
     * Publish event when a menu item is deleted.
     */
    public void publishMenuItemDeleted(Long menuItemId) {
        SearchIndexEvent event = SearchIndexEvent.builder()
                .eventType(SearchIndexEvent.MENU_ITEM_DELETED)
                .entityType(SearchIndexEvent.ENTITY_MENU_ITEM)
                .entityId(menuItemId)
                .payload(null) // No payload for delete
                .build();

        publishEvent(event);
    }

    // ==================== PRIVATE METHODS ====================

    private void publishVendorEvent(String eventType, VendorBranch branch) {
        try {
            VendorIndexPayload payload = mapToVendorPayload(branch);
            String payloadJson = objectMapper.writeValueAsString(payload);

            SearchIndexEvent event = SearchIndexEvent.builder()
                    .eventType(eventType)
                    .entityType(SearchIndexEvent.ENTITY_VENDOR)
                    .entityId(branch.getBranchId())
                    .payload(payloadJson)
                    .build();

            publishEvent(event);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize vendor payload: branchId={}", branch.getBranchId(), e);
            throw new RuntimeException("Failed to serialize vendor payload", e);
        }
    }

    private void publishMenuItemEvent(String eventType, MenuItem item) {
        try {
            MenuItemIndexPayload payload = mapToMenuItemPayload(item);
            String payloadJson = objectMapper.writeValueAsString(payload);

            SearchIndexEvent event = SearchIndexEvent.builder()
                    .eventType(eventType)
                    .entityType(SearchIndexEvent.ENTITY_MENU_ITEM)
                    .entityId(item.getMenuItemId())
                    .payload(payloadJson)
                    .build();

            publishEvent(event);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize menu item payload: menuItemId={}", item.getMenuItemId(), e);
            throw new RuntimeException("Failed to serialize menu item payload", e);
        }
    }

    private void publishEvent(SearchIndexEvent event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(event.getEventType())
                    .build());
            messageAttributes.put("entityType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(event.getEntityType())
                    .build());

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(messageBody)
                    .messageAttributes(messageAttributes)
                    .build();

            PublishResponse response = snsClient.publish(request);

            log.info("Published search index event to SNS: eventType={}, entityType={}, entityId={}, messageId={}",
                    event.getEventType(), event.getEntityType(), event.getEntityId(), response.messageId());

        } catch (Exception e) {
            log.error("Failed to publish search index event to SNS: eventType={}, entityType={}, entityId={}",
                    event.getEventType(), event.getEntityType(), event.getEntityId(), e);
            // Don't throw - transactional write should succeed even if SNS fails
            // Event will be re-synced via scheduled job or manual trigger
        }
    }

    private VendorIndexPayload mapToVendorPayload(VendorBranch branch) {
        Vendor vendor = branch.getVendor();

        return VendorIndexPayload.builder()
                .branchId(branch.getBranchId())
                .vendorId(vendor != null ? vendor.getVendorId() : null)
                // Use brandName or companyName as vendor name
                .vendorName(vendor != null
                        ? (vendor.getBrandName() != null ? vendor.getBrandName() : vendor.getCompanyName())
                        : null)
                .branchName(branch.getBranchName())
                .displayName(branch.getDisplayName())
                .city(branch.getCity())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .address(branch.getAddress())
                .tags(branch.getTags() != null ? Arrays.asList(branch.getTags()) : null)
                .rating(branch.getRating())
                .totalRatings(branch.getTotalReviews())
                .isOpen(branch.getIsOpen())
                .isActive(branch.getIsActive())
                .orderCount(branch.getTotalOrders())
                .build();
    }

    private MenuItemIndexPayload mapToMenuItemPayload(MenuItem item) {
        VendorBranch branch = item.getBranch();
        Vendor vendor = branch != null ? branch.getVendor() : null;

        return MenuItemIndexPayload.builder()
                .menuItemId(item.getMenuItemId())
                .branchId(branch != null ? branch.getBranchId() : null)
                .vendorId(vendor != null ? vendor.getVendorId() : null)
                .itemName(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .branchName(branch != null ? branch.getBranchName() : null)
                // Use brandName or companyName as vendor name
                .vendorName(vendor != null
                        ? (vendor.getBrandName() != null ? vendor.getBrandName() : vendor.getCompanyName())
                        : null)
                .city(branch != null ? branch.getCity() : null)
                .branchLatitude(branch != null ? branch.getLatitude() : null)
                .branchLongitude(branch != null ? branch.getLongitude() : null)
                .isAvailable(item.getIsAvailable())
                .tags(item.getTags() != null ? Arrays.asList(item.getTags()) : null)
                .build();
    }
}
