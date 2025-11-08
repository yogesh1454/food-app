-- V3__Create_branch_documents_table.sql
-- Branch documents table for verification

CREATE TABLE branch_documents (
    document_id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id) ON DELETE CASCADE,
    
    -- Document details
    document_type VARCHAR(50) NOT NULL,
    document_url VARCHAR(500) NOT NULL,
    document_number VARCHAR(100),
    
    -- Validity
    issue_date DATE,
    expiry_date DATE,
    
    -- Verification
    verification_status VARCHAR(50) DEFAULT 'PENDING',
    verification_notes TEXT,
    verified_at TIMESTAMP WITH TIME ZONE,
    verified_by UUID,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_branch_documents_branch_id ON branch_documents(branch_id);
CREATE INDEX idx_branch_documents_type ON branch_documents(document_type);
CREATE INDEX idx_branch_documents_status ON branch_documents(verification_status);
CREATE INDEX idx_branch_documents_expiry ON branch_documents(expiry_date) 
    WHERE expiry_date IS NOT NULL;
