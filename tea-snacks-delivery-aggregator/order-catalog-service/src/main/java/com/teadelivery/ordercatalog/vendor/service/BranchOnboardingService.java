package com.teadelivery.ordercatalog.vendor.service;

import com.teadelivery.ordercatalog.common.exception.BranchNotFoundException;
import com.teadelivery.ordercatalog.common.exception.UnauthorizedException;
import com.teadelivery.ordercatalog.common.exception.VendorNotFoundException;
import com.teadelivery.ordercatalog.vendor.dto.BranchCreateRequest;
import com.teadelivery.ordercatalog.vendor.dto.BranchResponse;
import com.teadelivery.ordercatalog.vendor.dto.DocumentResponse;
import com.teadelivery.ordercatalog.vendor.mapper.BranchMapper;
import com.teadelivery.ordercatalog.vendor.model.BranchDocument;
import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.BranchDocumentRepository;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import com.teadelivery.ordercatalog.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BranchOnboardingService {
    
    private final VendorRepository vendorRepository;
    private final VendorBranchRepository branchRepository;
    private final BranchDocumentRepository documentRepository;
    
    private static final String[] REQUIRED_DOCUMENTS = {"FSSAI", "SHOP_ACT", "GST", "ID_PROOF"};
    
    @Transactional
    public BranchResponse createBranch(Long vendorId, BranchCreateRequest request, UUID requestingUserId) {
        log.info("Creating branch for vendor: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        if (!vendor.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to create branch for this vendor");
        }
        
        VendorBranch branch = new VendorBranch();
        branch.setVendor(vendor);
        branch.setBranchName(request.getBranchName());
        // Use provided branchCode or generate one
        branch.setBranchCode(request.getBranchCode() != null ? 
            request.getBranchCode() : generateBranchCode(vendor.getVendorId(), request.getCity()));
        branch.setDisplayName(request.getDisplayName());
        branch.setAddress(request.getAddress());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        branch.setCity(request.getCity());
        branch.setBranchPhone(request.getBranchPhone());
        branch.setBranchEmail(request.getBranchEmail());
        branch.setBranchManagerName(request.getBranchManagerName());
        branch.setBranchManagerPhone(request.getBranchManagerPhone());
        branch.setOnboardingStatus("PENDING");
        branch.setIsActive(false);
        branch.setIsOpen(false);
        
        // Set preferences and operating hours from request or use defaults
        branch.setPreferences(request.getPreferences() != null ? request.getPreferences() : getDefaultPreferences());
        branch.setOperatingHours(request.getOperatingHours() != null ? request.getOperatingHours() : getDefaultOperatingHours());
        branch.setImages(new HashMap<>());
        branch.setMetadata(new HashMap<>());
        
        VendorBranch savedBranch = branchRepository.save(branch);
        
        log.info("Branch created: {}", savedBranch.getBranchId());
        return BranchMapper.toResponse(savedBranch);
    }
    
    @Transactional(readOnly = true)
    public BranchResponse getBranch(Long branchId) {
        log.info("Fetching branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        return BranchMapper.toResponse(branch);
    }
    
    @Transactional
    public BranchResponse updateBranch(Long vendorId, Long branchId, BranchCreateRequest request, UUID requestingUserId) {
        log.info("Updating branch: vendorId={}, branchId={}", vendorId, branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getVendorId().equals(vendorId)) {
            throw new UnauthorizedException("Branch does not belong to this vendor");
        }
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to update this branch");
        }
        
        // Update fields if provided
        if (request.getBranchName() != null) {
            branch.setBranchName(request.getBranchName());
        }
        if (request.getBranchCode() != null) {
            branch.setBranchCode(request.getBranchCode());
        }
        if (request.getDisplayName() != null) {
            branch.setDisplayName(request.getDisplayName());
        }
        if (request.getAddress() != null) {
            branch.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            branch.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            branch.setLongitude(request.getLongitude());
        }
        if (request.getCity() != null) {
            branch.setCity(request.getCity());
        }
        if (request.getBranchPhone() != null) {
            branch.setBranchPhone(request.getBranchPhone());
        }
        if (request.getBranchEmail() != null) {
            branch.setBranchEmail(request.getBranchEmail());
        }
        if (request.getBranchManagerName() != null) {
            branch.setBranchManagerName(request.getBranchManagerName());
        }
        if (request.getBranchManagerPhone() != null) {
            branch.setBranchManagerPhone(request.getBranchManagerPhone());
        }
        if (request.getPreferences() != null) {
            branch.setPreferences(request.getPreferences());
        }
        if (request.getOperatingHours() != null) {
            branch.setOperatingHours(request.getOperatingHours());
        }
        
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        log.info("Branch updated: {}", branchId);
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional
    public DocumentResponse uploadDocument(Long branchId, String documentType, String documentNumber, 
                                          LocalDate issueDate, LocalDate expiryDate, 
                                          String documentUrl, UUID requestingUserId) {
        log.info("Uploading document for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to upload documents for this branch");
        }
        
        BranchDocument document = new BranchDocument();
        document.setBranch(branch);
        document.setDocumentType(documentType);
        document.setDocumentUrl(documentUrl);
        document.setDocumentNumber(documentNumber);
        document.setIssueDate(issueDate);
        document.setExpiryDate(expiryDate);
        document.setVerificationStatus("PENDING");
        
        BranchDocument savedDocument = documentRepository.save(document);
        
        // Check if all required documents uploaded
        if (allRequiredDocumentsUploaded(branch)) {
            branch.setOnboardingStatus("DOCUMENTS_SUBMITTED");
            branchRepository.save(branch);
            log.info("All required documents uploaded for branch: {}", branchId);
        }
        
        return BranchMapper.toDocumentResponse(savedDocument);
    }
    
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long branchId, UUID requestingUserId) {
        log.info("Fetching documents for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to view documents for this branch");
        }
        
        List<BranchDocument> documents = documentRepository.findByBranch(branch);
        return documents.stream()
            .map(BranchMapper::toDocumentResponse)
            .toList();
    }
    
    @Transactional
    public BranchResponse updatePreferences(Long branchId, Map<String, Object> preferences, UUID requestingUserId) {
        log.info("Updating preferences for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to update preferences for this branch");
        }
        
        branch.setPreferences(preferences);
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        log.info("Preferences updated for branch: {}", branchId);
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getOnboardingStatus(Long branchId, UUID requestingUserId) {
        log.info("Fetching onboarding status for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to view status for this branch");
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("branchId", branch.getBranchId());
        status.put("onboardingStatus", branch.getOnboardingStatus());
        status.put("isActive", branch.getIsActive());
        
        List<BranchDocument> documents = documentRepository.findByBranch(branch);
        status.put("totalDocuments", documents.size());
        status.put("approvedDocuments", documents.stream()
            .filter(d -> "APPROVED".equals(d.getVerificationStatus()))
            .count());
        status.put("documents", documents.stream()
            .map(d -> Map.of(
                "type", d.getDocumentType(),
                "status", d.getVerificationStatus()
            ))
            .toList());
        
        return status;
    }
    
    private String generateBranchCode(Long vendorId, String city) {
        // Pad vendorId to ensure minimum 4 characters for consistent branch codes
        String vendorIdStr = String.format("%04d", vendorId);
        String vendorPrefix = vendorIdStr.substring(0, Math.min(4, vendorIdStr.length())).toUpperCase();
        String cityPrefix = city.substring(0, Math.min(3, city.length())).toUpperCase();
        long timestamp = System.currentTimeMillis() % 10000;
        return vendorPrefix + "-" + cityPrefix + "-" + timestamp;
    }
    
    private boolean allRequiredDocumentsUploaded(VendorBranch branch) {
        List<BranchDocument> documents = documentRepository.findByBranch(branch);
        Set<String> uploadedTypes = documents.stream()
            .map(BranchDocument::getDocumentType)
            .collect(java.util.stream.Collectors.toSet());
        
        for (String required : REQUIRED_DOCUMENTS) {
            if (!uploadedTypes.contains(required)) {
                return false;
            }
        }
        return true;
    }
    
    private Map<String, Object> getDefaultPreferences() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("autoAcceptOrders", false);
        preferences.put("maxOrdersPerHour", 50);
        preferences.put("deliveryRadiusKm", 5.0);
        preferences.put("minOrderValue", 100);
        preferences.put("acceptsCash", true);
        preferences.put("acceptsOnlinePayment", true);
        preferences.put("packingTimeMinutes", 15);
        preferences.put("commissionRate", 18.0);
        return preferences;
    }
    
    private Map<String, Object> getDefaultOperatingHours() {
        Map<String, Object> hours = new HashMap<>();
        Map<String, String> dayHours = new HashMap<>();
        dayHours.put("open", "09:00");
        dayHours.put("close", "22:00");
        
        for (String day : new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}) {
            hours.put(day, dayHours);
        }
        return hours;
    }
    
    @Transactional
    public BranchResponse uploadBranchImage(Long branchId, String imageType, String imageUrl, UUID requestingUserId) {
        log.info("Uploading branch image: branchId={}, imageType={}", branchId, imageType);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to upload images for this branch");
        }
        
        // Update images
        if (branch.getImages() == null) {
            branch.setImages(new HashMap<>());
        }
        
        branch.getImages().put(imageType, imageUrl);
        
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        log.info("Branch image uploaded: branchId={}, imageType={}", branchId, imageType);
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional
    public BranchResponse uploadBranchDocument(Long branchId, String documentType, String documentNumber,
                                              String issueDate, String expiryDate, String documentUrl, UUID requestingUserId) {
        log.info("Uploading branch document: branchId={}, documentType={}", branchId, documentType);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to upload documents for this branch");
        }
        
        BranchDocument document = new BranchDocument();
        document.setBranch(branch);
        document.setDocumentType(documentType);
        document.setDocumentUrl(documentUrl);
        document.setDocumentNumber(documentNumber);
        
        if (issueDate != null && !issueDate.isEmpty()) {
            document.setIssueDate(LocalDate.parse(issueDate));
        }
        if (expiryDate != null && !expiryDate.isEmpty()) {
            document.setExpiryDate(LocalDate.parse(expiryDate));
        }
        
        document.setVerificationStatus("PENDING");
        
        documentRepository.save(document);
        
        // Check if all required documents uploaded
        if (allRequiredDocumentsUploaded(branch)) {
            branch.setOnboardingStatus("DOCUMENTS_SUBMITTED");
            branchRepository.save(branch);
            log.info("All required documents uploaded for branch: {}", branchId);
        }
        
        log.info("Branch document uploaded: branchId={}, documentType={}", branchId, documentType);
        return BranchMapper.toResponse(branch);
    }
}
