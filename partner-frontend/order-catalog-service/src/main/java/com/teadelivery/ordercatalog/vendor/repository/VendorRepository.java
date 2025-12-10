package com.teadelivery.ordercatalog.vendor.repository;

import com.teadelivery.ordercatalog.vendor.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(UUID userId);
    List<Vendor> findByCompanyNameContainingIgnoreCase(String companyName);
    Optional<Vendor> findByCompanyEmail(String companyEmail);
}
