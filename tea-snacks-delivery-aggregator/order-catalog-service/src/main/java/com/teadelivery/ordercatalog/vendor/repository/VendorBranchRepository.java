package com.teadelivery.ordercatalog.vendor.repository;

import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VendorBranchRepository extends JpaRepository<VendorBranch, Long> {
    List<VendorBranch> findByVendor(Vendor vendor);
    List<VendorBranch> findByCity(String city);
    List<VendorBranch> findByOnboardingStatus(String status);
    List<VendorBranch> findByIsActiveTrue();
    
    @Query("SELECT b FROM VendorBranch b WHERE b.isActive = true AND b.isOpen = true")
    List<VendorBranch> findOpenBranches();
}
