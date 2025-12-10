# BE-003-02: Vendor Company Registration & Profile Management

**Story ID:** BE-003-02  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 5  
**Epic:** BE-003  
**Dependencies:** BE-003-01 (Database Schema), Epic 2 (User Management)

---

## 📖 User Story

**As a** restaurant owner  
**I want** to register my company and manage my brand profile  
**So that** I can create branches and start selling food

---

## ✅ Acceptance Criteria

1. **Company Registration**
   - [ ] Owner registers with company name, brand name, legal entity
   - [ ] PAN and GST numbers captured
   - [ ] Company logo and cover photo uploaded
   - [ ] Company email and phone stored
   - [ ] Linked to user from User Management Service
   - [ ] Vendor created with ACTIVE status

2. **Company Profile Management**
   - [ ] Update company details (name, logo, contact)
   - [ ] Manage company-level images
   - [ ] Add/edit metadata and tags
   - [ ] View all branches under company
   - [ ] Only company owner can modify

3. **Image Upload**
   - [ ] Logo upload (max 5MB, PNG/JPG)
   - [ ] Cover photo upload (max 10MB, PNG/JPG)
   - [ ] Images stored in S3/CDN
   - [ ] URLs stored in JSONB images column

4. **API Endpoints**
   - [ ] POST /api/v1/vendors - Register company
   - [ ] GET /api/v1/vendors/{vendorId} - Get company profile
   - [ ] PUT /api/v1/vendors/{vendorId} - Update company
   - [ ] GET /api/v1/vendors/{vendorId}/branches - List branches

5. **Validation & Business Rules**
   - [ ] Company email must be unique
   - [ ] PAN format validation (10 alphanumeric)
   - [ ] GST format validation (15 digits)
   - [ ] Phone number validation (10 digits)
   - [ ] Only one vendor per user

---

## 🔧 Technical Implementation

### **DTOs**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegistrationRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 255)
    private String companyName;
    
    @Size(max = 255)
    private String brandName;
    
    @Size(max = 255)
    private String legalEntityName;
    
    @NotBlank(message = "Company email is required")
    @Email(message = "Invalid email format")
    private String companyEmail;
    
    @NotBlank(message = "Company phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String companyPhone;
    
    @Pattern(regexp = "^[A-Z0-9]{10}$", message = "Invalid PAN format")
    private String panNumber;
    
    @Pattern(regexp = "^[0-9]{15}$", message = "Invalid GST format")
    private String gstNumber;
}

@Data
@Builder
public class VendorResponse {
    private UUID vendorId;
    private String companyName;
    private String brandName;
    private String companyEmail;
    private String companyPhone;
    private Map<String, Object> images;
    private Map<String, Object> metadata;
    private String[] tags;
    private List<VendorBranchResponse> branches;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### **Service**
```java
@Service
@Slf4j
public class VendorService {
    
    private final VendorRepository vendorRepository;
    private final S3Service s3Service;
    private final VendorEventPublisher eventPublisher;
    
    @Transactional
    public VendorResponse registerVendor(VendorRegistrationRequest request, 
                                        UUID userId) {
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
        
        // Publish event
        eventPublisher.publishVendorCreated(savedVendor);
        
        log.info("Vendor registered: {}", savedVendor.getVendorId());
        return VendorMapper.toResponse(savedVendor);
    }
    
    @Transactional
    public VendorResponse updateVendor(UUID vendorId, 
                                      VendorUpdateRequest request,
                                      UUID requestingUserId) {
        log.info("Updating vendor: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        // Authorization
        if (!vendor.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to update this vendor");
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
        
        // Publish event
        eventPublisher.publishVendorUpdated(updatedVendor);
        
        log.info("Vendor updated: {}", vendorId);
        return VendorMapper.toResponse(updatedVendor);
    }
    
    @Transactional
    public String uploadLogo(UUID vendorId, MultipartFile file, UUID requestingUserId) {
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        
        if (!vendor.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // Upload to S3
        String logoUrl = s3Service.uploadFile(file, "vendors/" + vendorId + "/logo");
        
        // Update vendor
        Map<String, Object> images = vendor.getImages();
        images.put("logo", logoUrl);
        vendor.setImages(images);
        vendorRepository.save(vendor);
        
        return logoUrl;
    }
}
```

### **Controller**
```java
@RestController
@RequestMapping("/api/v1/vendors")
@Slf4j
public class VendorController {
    
    private final VendorService vendorService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<VendorResponse>> registerVendor(
            @Valid @RequestBody VendorRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Register vendor request from user: {}", userPrincipal.getUserId());
        
        VendorResponse response = vendorService.registerVendor(
            request, userPrincipal.getUserId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Vendor registered successfully"));
    }
    
    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendor(
            @PathVariable UUID vendorId) {
        
        VendorResponse response = vendorService.getVendor(vendorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable UUID vendorId,
            @Valid @RequestBody VendorUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        VendorResponse response = vendorService.updateVendor(
            vendorId, request, userPrincipal.getUserId());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Vendor updated successfully"));
    }
    
    @PostMapping("/{vendorId}/logo")
    public ResponseEntity<ApiResponse<String>> uploadLogo(
            @PathVariable UUID vendorId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String logoUrl = vendorService.uploadLogo(
            vendorId, file, userPrincipal.getUserId());
        
        return ResponseEntity.ok(ApiResponse.success(logoUrl, "Logo uploaded successfully"));
    }
}
```

---

## 🧪 Testing Requirements

- [ ] Company registration with valid data
- [ ] Duplicate email validation
- [ ] PAN/GST format validation
- [ ] Logo upload to S3
- [ ] Authorization checks
- [ ] Kafka events published

---

## 📋 Definition of Done

- [ ] VendorService implemented
- [ ] All endpoints working
- [ ] Image upload to S3
- [ ] Validation complete
- [ ] Kafka events published
- [ ] Unit tests passing (>80%)
- [ ] Integration tests passing
- [ ] Code reviewed

---

## 📚 References

- [REST API Spec](/docs/architecture/8-rest-api-spec.md)
- [Coding Standards](/docs/architecture/13-coding-standards.md)
