package com.teadelivery.ordercatalog.vendor.model;

import com.teadelivery.ordercatalog.menu.model.MenuItem;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "vendor_branches", indexes = {
    @Index(name = "idx_branches_vendor_id", columnList = "vendor_id"),
    @Index(name = "idx_branches_city", columnList = "city"),
    @Index(name = "idx_branches_onboarding_status", columnList = "onboarding_status"),
    @Index(name = "idx_branches_active", columnList = "is_active,is_open")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VendorBranch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @Column(nullable = false, length = 255)
    private String branchName;
    
    @Column(length = 50, unique = true)
    private String branchCode;
    
    @Column(length = 255)
    private String displayName;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> address;
    
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(length = 20)
    private String branchPhone;
    
    @Column(length = 255)
    private String branchEmail;
    
    @Column(length = 255)
    private String branchManagerName;
    
    @Column(length = 20)
    private String branchManagerPhone;
    
    @Column(length = 50, nullable = false)
    private String onboardingStatus = "PENDING";
    
    @Column(columnDefinition = "TEXT")
    private String verificationNotes;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "verified_by")
    private UUID verifiedBy;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> preferences;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> images;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "is_open")
    private Boolean isOpen = false;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> operatingHours;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Column(name = "menu_version")
    private Integer menuVersion = 1;
    
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
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BranchDocument> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems = new ArrayList<>();
    
    public void incrementMenuVersion() {
        this.menuVersion++;
    }
}
