package com.teadelivery.ordercatalog.vendor.mapper;

import com.teadelivery.ordercatalog.vendor.dto.BranchResponse;
import com.teadelivery.ordercatalog.vendor.dto.DocumentResponse;
import com.teadelivery.ordercatalog.vendor.model.BranchDocument;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;

public class BranchMapper {
    
    private BranchMapper() {
        // Utility class
    }
    
    public static BranchResponse toResponse(VendorBranch branch) {
        if (branch == null) {
            return null;
        }
        
        return BranchResponse.builder()
            .branchId(branch.getBranchId())
            .vendorId(branch.getVendor().getVendorId())
            .branchName(branch.getBranchName())
            .branchCode(branch.getBranchCode())
            .address(branch.getAddress())
            .latitude(branch.getLatitude())
            .longitude(branch.getLongitude())
            .city(branch.getCity())
            .branchPhone(branch.getBranchPhone())
            .branchEmail(branch.getBranchEmail())
            .branchManagerName(branch.getBranchManagerName())
            .onboardingStatus(branch.getOnboardingStatus())
            .isActive(branch.getIsActive())
            .isOpen(branch.getIsOpen())
            .preferences(branch.getPreferences())
            .operatingHours(branch.getOperatingHours())
            .images(branch.getImages())
            .metadata(branch.getMetadata())
            .createdAt(branch.getCreatedAt())
            .updatedAt(branch.getUpdatedAt())
            .build();
    }
    
    public static DocumentResponse toDocumentResponse(BranchDocument document) {
        if (document == null) {
            return null;
        }
        
        return DocumentResponse.builder()
            .documentId(document.getDocumentId())
            .branchId(document.getBranch().getBranchId())
            .documentType(document.getDocumentType())
            .documentUrl(document.getDocumentUrl())
            .documentNumber(document.getDocumentNumber())
            .issueDate(document.getIssueDate())
            .expiryDate(document.getExpiryDate())
            .verificationStatus(document.getVerificationStatus())
            .verificationNotes(document.getVerificationNotes())
            .verifiedBy(document.getVerifiedBy())
            .verifiedAt(document.getVerifiedAt())
            .createdAt(document.getCreatedAt())
            .build();
    }
}
