package com.teadelivery.ordercatalog.vendor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "branch_documents", indexes = {
    @Index(name = "idx_branch_documents_branch_id", columnList = "branch_id"),
    @Index(name = "idx_branch_documents_type", columnList = "document_type"),
    @Index(name = "idx_branch_documents_status", columnList = "verification_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BranchDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private VendorBranch branch;
    
    @Column(nullable = false, length = 50)
    private String documentType;
    
    @Column(nullable = false, length = 500)
    private String documentUrl;
    
    @Column(length = 100)
    private String documentNumber;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(length = 50, nullable = false)
    private String verificationStatus = "PENDING";
    
    @Column(columnDefinition = "TEXT")
    private String verificationNotes;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "verified_by")
    private UUID verifiedBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
