package com.teadelivery.ordercatalog.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single image with all its size variants.
 * Used for gallery images in the API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageVariant {

    private Integer index;
    private String thumbnail;
    private String small;
    private String medium;
    private String large;
}
