package com.teadelivery.ordercatalog.vendor.service;

import com.teadelivery.ordercatalog.common.exception.DuplicateVendorEmailException;
import com.teadelivery.ordercatalog.common.exception.VendorAlreadyExistsException;
import com.teadelivery.ordercatalog.common.exception.VendorNotFoundException;
import com.teadelivery.ordercatalog.vendor.dto.VendorRegistrationRequest;
import com.teadelivery.ordercatalog.vendor.dto.VendorResponse;
import com.teadelivery.ordercatalog.vendor.dto.VendorUpdateRequest;
import com.teadelivery.ordercatalog.vendor.mapper.VendorMapper;
import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VendorService {
    
    private final VendorRepository vendorRepository;
    
    @Transactional
    public VendorResponse registerVendor(VendorRegistrationRequest request, UUID userId) {
        log.info("Registering vendor for user: {}", userId);
        
        // Check if user already has a vendor
        if (vendorRepository.findByUserId(userId).isPresent()) {
            throw new VendorAlreadyExistsException("User already has a vendor account");
        }
        
        // Validate unique email
        if (vendorRepository.findByCompanyEmail(request.getCompanyEmail()).isPresent()) {
            throw new DuplicateVendorEmailException("Email already registered");
        }
        
        // Create vendor
        Vendor vendor = new Vendor();
        vendor.setUserId(userId);
        vendor.setCompanyName(request.getCompanyName());
        vendor.setBrandName(request.getBrandName());
        vendor.setLegalEntityName(request.getLegalEntityName());
        vendor.setCompanyEmail(request.getCompanyEmail());
        vendor.setCompanyPhone(request.getCompanyPhone());
        vendor.setPanNumber(request.getPanNumber());
        vendor.setGstNumber(request.getGstNumber());
        
        // Initialize images and metadata
        vendor.setImages(new HashMap<>());
        vendor.setMetadata(new HashMap<>());
        vendor.setTags(new String[]{});
        
        Vendor savedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor registered: {}", savedVendor.getVendorId());
        return VendorMapper.toResponse(savedVendor);
    }
    
    @Transactional(readOnly = true)
    public VendorResponse getVendor(Long vendorId) {
        log.info("Fetching vendor: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        return VendorMapper.toResponse(vendor);
    }
    
    @Transactional
    public VendorResponse updateVendor(Long vendorId, VendorUpdateRequest request, UUID requestingUserId) {
        log.info("Updating vendor: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        // Authorization
        if (!vendor.getUserId().equals(requestingUserId)) {
            throw new RuntimeException("Not authorized to update this vendor");
        }
        
        // Update fields
        if (request.getCompanyName() != null) {
            vendor.setCompanyName(request.getCompanyName());
        }
        if (request.getBrandName() != null) {
            vendor.setBrandName(request.getBrandName());
        }
        if (request.getCompanyPhone() != null) {
            vendor.setCompanyPhone(request.getCompanyPhone());
        }
        if (request.getMetadata() != null) {
            vendor.setMetadata(request.getMetadata());
        }
        if (request.getTags() != null) {
            vendor.setTags(request.getTags());
        }
        
        Vendor updatedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor updated: {}", vendorId);
        return VendorMapper.toResponse(updatedVendor);
    }
    
    @Transactional
    public VendorResponse uploadVendorImage(Long vendorId, String imageType, String imageUrl, UUID requestingUserId) {
        log.info("Uploading vendor image: vendorId={}, imageType={}", vendorId, imageType);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        // Authorization
        if (!vendor.getUserId().equals(requestingUserId)) {
            throw new RuntimeException("Not authorized to update this vendor");
        }
        
        // Update images
        if (vendor.getImages() == null) {
            vendor.setImages(new HashMap<>());
        }
        
        vendor.getImages().put(imageType, imageUrl);
        
        Vendor updatedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor image uploaded: vendorId={}, imageType={}", vendorId, imageType);
        return VendorMapper.toResponse(updatedVendor);
    }
}
