package com.teadelivery.ordercatalog.vendor.mapper;

import com.teadelivery.ordercatalog.vendor.dto.VendorResponse;
import com.teadelivery.ordercatalog.vendor.model.Vendor;

public class VendorMapper {
    
    private VendorMapper() {
        // Utility class
    }
    
    public static VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) {
            return null;
        }
        
        return VendorResponse.builder()
            .vendorId(vendor.getVendorId())
            .companyName(vendor.getCompanyName())
            .brandName(vendor.getBrandName())
            .legalEntityName(vendor.getLegalEntityName())
            .companyEmail(vendor.getCompanyEmail())
            .companyPhone(vendor.getCompanyPhone())
            .panNumber(vendor.getPanNumber())
            .gstNumber(vendor.getGstNumber())
            .images(vendor.getImages())
            .metadata(vendor.getMetadata())
            .tags(vendor.getTags())
            .createdAt(vendor.getCreatedAt())
            .updatedAt(vendor.getUpdatedAt())
            .build();
    }
}
