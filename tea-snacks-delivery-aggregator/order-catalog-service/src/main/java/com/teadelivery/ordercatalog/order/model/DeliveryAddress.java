package com.teadelivery.ordercatalog.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Delivery Address - Embeddable component
 * Represents the delivery location for an order
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {

    @NotBlank(message = "Address line 1 is required")
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "landmark", length = 255)
    private String landmark;

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Column(name = "address_state", nullable = false, length = 100)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "address_type", length = 32)
    private String addressType; // HOME, WORK, OTHER

    @Column(name = "address_label", length = 100)
    private String label; // e.g., "Home", "Office", "Mom's Place"
}
