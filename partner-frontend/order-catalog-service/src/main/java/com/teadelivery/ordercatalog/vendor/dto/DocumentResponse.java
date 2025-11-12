package com.teadelivery.ordercatalog.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    
    private Long documentId;
    private Long branchId;
    private String documentType;
    private String documentUrl;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String verificationStatus;
    private String verificationNotes;
    private UUID verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
