package com.teadelivery.ordercatalog.common.service;

import com.teadelivery.ordercatalog.config.AwsStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling S3 operations for image storage.
 * Implements Phase 1 of IMAGE_STORAGE_AND_RENDERING_SPECIFICATION.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsStorageProperties awsStorageProperties;

    // Supported image types
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif");

    // Maximum file sizes (in bytes)
    private static final long MAX_LOGO_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final long MAX_COVER_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_MENU_IMAGE_SIZE = 3 * 1024 * 1024; // 3 MB

    /**
     * Upload an image to S3 for a vendor.
     * 
     * @param vendorId  Vendor ID
     * @param imageType Type of image (logo, cover)
     * @param file      MultipartFile to upload
     * @return S3 key where the file was stored
     */
    public String uploadVendorImage(Long vendorId, String imageType, MultipartFile file) {
        validateFile(file, imageType);

        String extension = getFileExtension(file.getOriginalFilename());
        String s3Key = String.format("originals/vendors/%d/%s_original.%s",
                vendorId, imageType, extension);

        return uploadToS3(s3Key, file, "vendor", vendorId.toString(), imageType);
    }

    /**
     * Upload an image to S3 for a branch.
     * 
     * @param branchId  Branch ID
     * @param imageType Type of image (storefront, interior, menu_board, kitchen)
     * @param file      MultipartFile to upload
     * @return S3 key where the file was stored
     */
    public String uploadBranchImage(Long branchId, String imageType, MultipartFile file) {
        validateFile(file, imageType);

        String extension = getFileExtension(file.getOriginalFilename());
        String s3Key = String.format("originals/vendor-branches/%d/%s_original.%s",
                branchId, imageType, extension);

        return uploadToS3(s3Key, file, "vendor-branch", branchId.toString(), imageType);
    }

    /**
     * Upload an image to S3 for a menu item.
     * 
     * @param menuItemId   Menu item ID
     * @param imageType    Type of image (primary, gallery)
     * @param file         MultipartFile to upload
     * @param galleryIndex Gallery index (only for gallery images)
     * @return S3 key where the file was stored
     */
    public String uploadMenuItemImage(Long menuItemId, String imageType, MultipartFile file, Integer galleryIndex) {
        validateFile(file, "menu");

        // Preserve original filename with imageType as prefix
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "image." + getFileExtension(originalFilename);
        }

        // Clean filename (remove special chars, keep only alphanumeric, dots, hyphens,
        // underscores)
        String cleanFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Build S3 key: originals/menu-items/{id}/{imageType}_{originalFilename}
        String fileName;
        if ("gallery".equals(imageType) && galleryIndex != null) {
            fileName = String.format("gallery_%d_%s", galleryIndex, cleanFilename);
        } else {
            fileName = String.format("%s_%s", imageType, cleanFilename);
        }

        String s3Key = String.format("originals/menu-items/%d/%s", menuItemId, fileName);

        // Always pass "gallery" as imageType for gallery images (not "gallery_1")
        String actualImageType = "gallery".equals(imageType) ? "gallery" : imageType;
        return uploadToS3(s3Key, file, "menu-item", menuItemId.toString(), actualImageType);
    }

    /**
     * Upload a document to S3 for a branch (FSSAI, GST, etc.).
     * 
     * @param branchId     Branch ID
     * @param documentType Type of document (fssai, gst, shop_act, id_proof)
     * @param file         MultipartFile to upload
     * @return S3 key where the file was stored
     */
    public String uploadBranchDocument(Long branchId, String documentType, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = String.valueOf(System.currentTimeMillis());
        String s3Key = String.format("branches/%d/%s/document_%s.%s",
                branchId, documentType, timestamp, extension);

        String bucket = awsStorageProperties.getS3().getBuckets().getDocuments();

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("entity-type", "branch-document");
            metadata.put("entity-id", branchId.toString());
            metadata.put("document-type", documentType);
            metadata.put("uploaded-at", java.time.Instant.now().toString());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Document uploaded to S3: bucket={}, key={}", bucket, s3Key);
            return s3Key;

        } catch (IOException e) {
            log.error("Failed to upload document to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    /**
     * Delete an image from S3.
     * 
     * @param s3Key S3 key of the file to delete
     */
    public void deleteImage(String s3Key) {
        String bucket = awsStorageProperties.getS3().getBuckets().getMedia();

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Image deleted from S3: bucket={}, key={}", bucket, s3Key);

        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", e.getMessage());
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    /**
     * Generate a pre-signed URL for temporary access to a private object.
     * 
     * @param s3Key      S3 key of the file
     * @param bucket     Bucket name
     * @param expiration Duration for which the URL is valid
     * @return Pre-signed URL
     */
    public String generatePresignedUrl(String s3Key, String bucket, Duration expiration) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(b -> b.bucket(bucket).key(s3Key))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Generate the CDN URL for a processed image.
     * 
     * @param entityType Entity type (vendors, branches, menu-items)
     * @param entityId   Entity ID
     * @param imageType  Image type (logo, cover, storefront, primary, etc.)
     * @param size       Image size (thumbnail, small, medium, large)
     * @return CDN URL for the image
     */
    public String generateCdnUrl(String entityType, String entityId, String imageType, String size) {
        String baseUrl = awsStorageProperties.getCloudfront().getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            // Fallback to S3 direct URL if CloudFront not configured
            String bucket = awsStorageProperties.getS3().getBuckets().getMedia();
            String region = awsStorageProperties.getS3().getRegion();
            return String.format("https://%s.s3.%s.amazonaws.com/processed/%s/%s/%s_%s.webp",
                    bucket, region, entityType, entityId, imageType, size);
        }
        return String.format("%s/%s/%s/%s_%s.webp", baseUrl, entityType, entityId, imageType, size);
    }

    /**
     * Build simplified image URLs map for database storage.
     * Creates placeholder URLs that will be replaced when Lambda processing
     * completes.
     * Format: { "thumbnail": "url", "small": "url", "medium": "url", "large": "url"
     * }
     * 
     * @param entityType Entity type
     * @param entityId   Entity ID
     * @param imageType  Image type
     * @param s3Key      Original S3 key (not stored in simplified structure)
     * @return Map with placeholder CDN URLs by size
     */
    public Map<String, Object> buildImageUrlsMap(String entityType, String entityId,
            String imageType, String s3Key) {
        Map<String, Object> imageData = new HashMap<>();

        // Create placeholder URLs (will be updated by Lambda after processing)
        // Direct size -> URL mapping (simplified structure for mobile)
        imageData.put("thumbnail", generateCdnUrl(entityType, entityId, imageType, "thumbnail"));
        imageData.put("small", generateCdnUrl(entityType, entityId, imageType, "small"));
        imageData.put("medium", generateCdnUrl(entityType, entityId, imageType, "medium"));
        if (!"logo".equals(imageType)) {
            imageData.put("large", generateCdnUrl(entityType, entityId, imageType, "large"));
        }

        return imageData;
    }

    // ==================== Private Helper Methods ====================

    private String uploadToS3(String s3Key, MultipartFile file, String entityType,
            String entityId, String imageType) {
        String bucket = awsStorageProperties.getS3().getBuckets().getMedia();

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("entity-type", entityType);
            metadata.put("entity-id", entityId);
            metadata.put("image-type", imageType);
            metadata.put("uploaded-at", java.time.Instant.now().toString());
            metadata.put("original-filename", file.getOriginalFilename());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Image uploaded to S3: bucket={}, key={}, size={} bytes",
                    bucket, s3Key, file.getSize());
            return s3Key;

        } catch (IOException e) {
            log.error("Failed to upload image to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    private void validateFile(MultipartFile file, String imageType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!CONTENT_TYPES.containsKey(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported image format. Allowed: jpg, jpeg, png, webp, gif");
        }

        // Validate file size based on image type
        long maxSize = switch (imageType.toLowerCase()) {
            case "logo" -> MAX_LOGO_SIZE;
            case "cover" -> MAX_COVER_SIZE;
            case "menu", "primary", "gallery" -> MAX_MENU_IMAGE_SIZE;
            default -> MAX_COVER_SIZE; // Default to cover size
        };

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed (%d MB)", maxSize / (1024 * 1024)));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
