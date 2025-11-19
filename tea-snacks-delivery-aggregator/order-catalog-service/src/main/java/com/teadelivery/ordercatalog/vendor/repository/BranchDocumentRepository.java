package com.teadelivery.ordercatalog.vendor.repository;

import com.teadelivery.ordercatalog.vendor.model.BranchDocument;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchDocumentRepository extends JpaRepository<BranchDocument, UUID> {
    List<BranchDocument> findByBranch(VendorBranch branch);
    List<BranchDocument> findByBranchAndDocumentType(VendorBranch branch, String documentType);
    List<BranchDocument> findByVerificationStatus(String status);
}
