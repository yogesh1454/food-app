package com.teadelivery.ordercatalog.vendor.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "vendors", indexes = {
    @Index(name = "idx_vendors_user_id", columnList = "user_id"),
    @Index(name = "idx_vendors_company_name", columnList = "company_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(nullable = false, length = 255)
    private String companyName;
    
    @Column(length = 255)
    private String brandName;
    
    @Column(length = 255)
    private String legalEntityName;
    
    @Column(length = 255)
    private String companyEmail;
    
    @Column(length = 20)
    private String companyPhone;
    
    @Column(length = 20)
    private String panNumber;
    
    @Column(length = 20)
    private String gstNumber;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> images;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(columnDefinition = "text[]")
    private String[] tags;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendorBranch> branches = new ArrayList<>();
}
