# BE-003-03: Branch Onboarding & Document Verification

**Story ID:** BE-003-03  
**Story Points:** 13  
**Priority:** Critical (P0)  
**Sprint:** 5  
**Epic:** BE-003  
**Dependencies:** BE-003-02 (Vendor Registration)

---

## 📖 User Story

**As a** restaurant owner  
**I want** to onboard a new branch with document verification  
**So that** the branch can start receiving orders after approval

---

## ✅ Acceptance Criteria

1. **Branch Creation**
   - [ ] Create branch with location details (address, lat/long, city)
   - [ ] Branch code auto-generated or provided
   - [ ] Branch manager details captured
   - [ ] Branch preferences initialized with defaults
   - [ ] Operating hours set to defaults
   - [ ] Branch created with onboarding_status = PENDING

2. **Document Upload**
   - [ ] Upload FSSAI license
   - [ ] Upload Shop Act license
   - [ ] Upload GST certificate
   - [ ] Upload owner ID proof
   - [ ] Upload menu card (optional)
   - [ ] Each document stored in branch_documents table
   - [ ] Document URLs stored (S3/CDN)
   - [ ] Document numbers and expiry dates captured

3. **Branch Images**
   - [ ] Upload branch logo
   - [ ] Upload cover photo
   - [ ] Upload storefront photo
   - [ ] Upload interior photos (multiple)
   - [ ] Upload kitchen photos (multiple)
   - [ ] Images stored in JSONB images column

4. **Onboarding Status Flow**
   - [ ] PENDING → DOCUMENTS_SUBMITTED (when all docs uploaded)
   - [ ] DOCUMENTS_SUBMITTED → UNDER_VERIFICATION (admin review starts)
   - [ ] UNDER_VERIFICATION → APPROVED (all docs verified)
   - [ ] UNDER_VERIFICATION → REJECTED (docs rejected)
   - [ ] APPROVED → branch.is_active = true (can receive orders)

5. **Preferences Management**
   - [ ] Set auto-accept orders preference
   - [ ] Set max orders per hour
   - [ ] Set delivery radius
   - [ ] Set minimum order value
   - [ ] Set payment methods accepted
   - [ ] Set packing time
   - [ ] Preferences stored as JSONB

6. **API Endpoints**
   - [ ] POST /api/v1/vendors/{vendorId}/branches - Create branch
   - [ ] PUT /api/v1/branches/{branchId} - Update branch details
   - [ ] POST /api/v1/branches/{branchId}/documents - Upload document
   - [ ] GET /api/v1/branches/{branchId}/documents - List documents
   - [ ] POST /api/v1/branches/{branchId}/images - Upload image
   - [ ] PUT /api/v1/branches/{branchId}/preferences - Update preferences
   - [ ] GET /api/v1/branches/{branchId}/onboarding-status - Check status

---

## 🔧 Key Implementation

### **Branch Creation Service**
```java
@Service
@Transactional
public class BranchOnboardingService {
    
    public BranchResponse createBranch(UUID vendorId, 
                                      BranchCreateRequest request,
                                      UUID requestingUserId) {
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        if (!vendor.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        VendorBranch branch = new VendorBranch();
        branch.setVendor(vendor);
        branch.setBranchName(request.getBranchName());
        branch.setBranchCode(generateBranchCode(vendor, request.getCity()));
        branch.setAddress(request.getAddress());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        branch.setCity(request.getCity());
        branch.setBranchPhone(request.getBranchPhone());
        branch.setBranchEmail(request.getBranchEmail());
        branch.setBranchManagerName(request.getBranchManagerName());
        branch.setOnboardingStatus("PENDING");
        branch.setIsActive(false);
        branch.setIsOpen(false);
        
        // Initialize preferences
        branch.setPreferences(getDefaultPreferences());
        branch.setOperatingHours(getDefaultOperatingHours());
        branch.setImages(new HashMap<>());
        branch.setMetadata(new HashMap<>());
        
        VendorBranch savedBranch = branchRepository.save(branch);
        eventPublisher.publishBranchCreated(savedBranch);
        
        return BranchMapper.toResponse(savedBranch);
    }
    
    public void uploadDocument(UUID branchId, 
                              DocumentUploadRequest request,
                              MultipartFile file,
                              UUID requestingUserId) {
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // Upload to S3
        String documentUrl = s3Service.uploadFile(
            file, "branches/" + branchId + "/documents/" + request.getDocumentType());
        
        // Create document record
        BranchDocument document = new BranchDocument();
        document.setBranch(branch);
        document.setDocumentType(request.getDocumentType());
        document.setDocumentUrl(documentUrl);
        document.setDocumentNumber(request.getDocumentNumber());
        document.setIssueDate(request.getIssueDate());
        document.setExpiryDate(request.getExpiryDate());
        document.setVerificationStatus("PENDING");
        
        documentRepository.save(document);
        
        // Check if all required documents uploaded
        if (allRequiredDocumentsUploaded(branch)) {
            branch.setOnboardingStatus("DOCUMENTS_SUBMITTED");
            branchRepository.save(branch);
            eventPublisher.publishDocumentsSubmitted(branch);
        }
    }
    
    public void uploadImage(UUID branchId, 
                           String imageType,
                           MultipartFile file,
                           UUID requestingUserId) {
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        String imageUrl = s3Service.uploadFile(
            file, "branches/" + branchId + "/images/" + imageType);
        
        Map<String, Object> images = branch.getImages();
        
        if (imageType.equals("interior") || imageType.equals("kitchen") || imageType.equals("gallery")) {
            List<String> imageList = (List<String>) images.getOrDefault(imageType, new ArrayList<>());
            imageList.add(imageUrl);
            images.put(imageType, imageList);
        } else {
            images.put(imageType, imageUrl);
        }
        
        branch.setImages(images);
        branchRepository.save(branch);
    }
    
    public void updatePreferences(UUID branchId,
                                 Map<String, Object> preferences,
                                 UUID requestingUserId) {
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        branch.setPreferences(preferences);
        branchRepository.save(branch);
        eventPublisher.publishPreferencesUpdated(branch);
    }
}
```

### **Admin Verification Service**
```java
@Service
@Transactional
public class DocumentVerificationService {
    
    public void verifyDocument(UUID documentId, 
                              boolean approved,
                              String notes,
                              UUID adminUserId) {
        BranchDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        
        document.setVerificationStatus(approved ? "APPROVED" : "REJECTED");
        document.setVerificationNotes(notes);
        document.setVerifiedAt(LocalDateTime.now());
        document.setVerifiedBy(adminUserId);
        
        documentRepository.save(document);
        
        // Check if all documents approved
        VendorBranch branch = document.getBranch();
        if (allDocumentsApproved(branch)) {
            branch.setOnboardingStatus("APPROVED");
            branch.setIsActive(true);
            branch.setActivatedAt(LocalDateTime.now());
            branchRepository.save(branch);
            eventPublisher.publishBranchApproved(branch);
        }
    }
}
```

---

## 📋 Definition of Done

- [ ] Branch creation implemented
- [ ] Document upload working
- [ ] Image upload working
- [ ] Preferences management
- [ ] Onboarding status flow
- [ ] Admin verification
- [ ] All endpoints working
- [ ] Tests passing (>80%)
- [ ] Code reviewed

---

## 📚 References

- [Database Schema](/docs/architecture/9-database-schema.md)
