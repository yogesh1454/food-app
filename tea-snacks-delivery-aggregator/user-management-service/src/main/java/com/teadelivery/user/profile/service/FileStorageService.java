package com.teadelivery.user.profile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for file storage operations.
 * Follows coding standards with comprehensive file handling.
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.file.upload.max-size:5242880}")
    private long maxFileSize; // 5MB default

    /**
     * Upload file to storage.
     * 
     * @param file file to upload
     * @param subdirectory subdirectory for file storage
     * @return file URL
     * @throws IOException if upload fails
     */
    public String uploadFile(MultipartFile file, String subdirectory) throws IOException {
        log.info("Uploading file: {} to subdirectory: {}", file.getOriginalFilename(), subdirectory);
        
        // Validate file
        validateFile(file);
        
        // Create upload directory
        Path uploadDir = Paths.get(uploadPath, subdirectory);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return file URL
        String fileUrl = "/files/" + subdirectory + "/" + uniqueFilename;
        
        log.info("File uploaded successfully: {}", fileUrl);
        return fileUrl;
    }

    /**
     * Delete file from storage.
     * 
     * @param fileUrl file URL to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }
            
            // Extract file path from URL
            String filePath = fileUrl.replace("/files/", "");
            Path fullPath = Paths.get(uploadPath, filePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("File deleted successfully: {}", fileUrl);
                return true;
            }
            
            return false;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * Validate uploaded file.
     * 
     * @param file file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize + " bytes");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    /**
     * Check if content type is allowed.
     * 
     * @param contentType content type to check
     * @return true if allowed, false otherwise
     */
    private boolean isAllowedContentType(String contentType) {
        return contentType.startsWith("image/") || 
               contentType.startsWith("application/pdf") ||
               contentType.startsWith("text/");
    }

    /**
     * Get file extension from filename.
     * 
     * @param filename filename
     * @return file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex);
    }

    /**
     * Get file path from URL.
     * 
     * @param fileUrl file URL
     * @return file path
     */
    public Path getFilePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        String filePath = fileUrl.replace("/files/", "");
        return Paths.get(uploadPath, filePath);
    }

    /**
     * Check if file exists.
     * 
     * @param fileUrl file URL
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String fileUrl) {
        Path filePath = getFilePath(fileUrl);
        return filePath != null && Files.exists(filePath);
    }
} 