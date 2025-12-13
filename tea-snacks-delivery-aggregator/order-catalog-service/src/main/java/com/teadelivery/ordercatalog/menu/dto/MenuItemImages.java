package com.teadelivery.ordercatalog.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Structured representation of menu item images.
 * Groups gallery images as an array for easier frontend rendering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemImages {

    /**
     * Primary image with all size variants
     */
    private Map<String, String> primary;

    /**
     * Gallery images as an array, each with all size variants
     */
    private List<ImageVariant> gallery;
}
