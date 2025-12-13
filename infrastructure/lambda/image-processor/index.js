/**
 * Nashtto Image Processor Lambda
 * 
 * Triggered by SQS messages when images are uploaded to S3.
 * Generates multiple size variants in WebP and JPEG formats.
 * Updates the backend via SQS message with final CDN URLs.
 * 
 * Based on IMAGE_STORAGE_AND_RENDERING_SPECIFICATION_REVISED.md
 */

const { S3Client, GetObjectCommand, PutObjectCommand } = require('@aws-sdk/client-s3');
const { SQSClient, SendMessageCommand } = require('@aws-sdk/client-sqs');
const sharp = require('sharp');

const s3Client = new S3Client({ region: process.env.AWS_REGION || 'us-east-1' });
const sqsClient = new SQSClient({ region: process.env.AWS_REGION || 'us-east-1' });

// Image size configurations
const IMAGE_VARIANTS = {
    thumbnail: { width: 150, height: 150, fit: 'cover' },
    small: { width: 300, height: 300, fit: 'inside' },
    medium: { width: 600, height: 600, fit: 'inside' },
    large: { width: 1200, height: 1200, fit: 'inside' }
};

// Image types that don't need large variant
const SKIP_LARGE_FOR = ['logo'];

// Environment variables
const MEDIA_BUCKET = process.env.MEDIA_BUCKET || 'nashtto-media-prod';
const CLOUDFRONT_DOMAIN = process.env.CLOUDFRONT_DOMAIN || '';
const RESULT_QUEUE_URL = process.env.RESULT_QUEUE_URL || '';

/**
 * Lambda handler - processes SQS messages from image upload events
 */
exports.handler = async (event) => {
    console.log('Processing batch of', event.Records?.length || 0, 'messages');

    const results = [];

    for (const record of event.Records || []) {
        try {
            // Parse SQS message body (contains S3 event notification)
            const body = JSON.parse(record.body);

            // Handle direct S3 event or SNS-wrapped event
            const s3Event = body.Records ? body.Records[0] : (body.Message ? JSON.parse(body.Message).Records[0] : null);

            if (!s3Event || !s3Event.s3) {
                console.log('Invalid S3 event format, skipping:', body);
                continue;
            }

            const bucket = s3Event.s3.bucket.name;
            const key = decodeURIComponent(s3Event.s3.object.key.replace(/\+/g, ' '));

            console.log(`Processing image: bucket=${bucket}, key=${key}`);

            // Only process files in originals/ folder
            if (!key.startsWith('originals/')) {
                console.log('Skipping non-original file:', key);
                continue;
            }

            const result = await processImage(bucket, key);
            results.push(result);

        } catch (error) {
            console.error('Error processing record:', error);
            results.push({ success: false, error: error.message });
        }
    }

    console.log('Batch processing complete. Results:', JSON.stringify(results));

    return {
        statusCode: 200,
        body: JSON.stringify({ processed: results.length, results })
    };
};

/**
 * Process a single image - generate all variants
 */
async function processImage(bucket, originalKey) {
    console.log(`Starting image processing: ${originalKey}`);

    // Parse the key to extract entity info
    // New format: originals/{entity}/{entityId}/{imageType}_{originalFilename}.{ext}
    // Examples:
    //   - originals/menu-items/12/primary_masala-maggi.jpg
    //   - originals/menu-items/12/gallery_1_maggi-masala-gallery2.webp
    const keyParts = originalKey.match(/originals\/([\w-]+)\/(\d+)\/([\w]+)_(.+)$/);

    if (!keyParts) {
        throw new Error(`Invalid key format: ${originalKey}`);
    }

    const [, entityType, entityId, rawImageType, filenameWithExt] = keyParts;

    // Extract extension from filename
    const extension = filenameWithExt.split('.').pop();

    // Normalize gallery image types: gallery_1, gallery_2 -> gallery for database storage
    // But keep rawImageType for S3 processed filenames to ensure uniqueness
    const imageType = rawImageType.startsWith('gallery_') ? 'gallery' : rawImageType;

    console.log(`Entity: ${entityType}/${entityId}, ImageType: ${imageType} (raw: ${rawImageType}), Extension: ${extension}`);


    // Download original image from S3
    const originalImage = await downloadFromS3(bucket, originalKey);

    // Get image metadata
    const metadata = await sharp(originalImage).metadata();
    console.log(`Original image: ${metadata.width}x${metadata.height}, format: ${metadata.format}`);

    // Generate variants
    const variants = {};
    const processedKeys = [];

    for (const [sizeName, config] of Object.entries(IMAGE_VARIANTS)) {
        // Skip large variant for logos
        if (sizeName === 'large' && SKIP_LARGE_FOR.includes(imageType)) {
            continue;
        }

        try {
            // Use rawImageType for S3 keys to ensure unique filenames (gallery_1, gallery_2)
            // But use normalized imageType for database storage (gallery)
            const fileImageType = rawImageType; // Use original for unique filenames

            // Generate WebP variant (primary)
            const webpKey = `processed/${entityType}/${entityId}/${fileImageType}_${sizeName}.webp`;
            const webpBuffer = await generateVariant(originalImage, config, 'webp');
            await uploadToS3(bucket, webpKey, webpBuffer, 'image/webp');
            processedKeys.push(webpKey);

            // Store CDN URLs
            variants[sizeName] = {
                webp: getCdnUrl(webpKey)
            };

            console.log(`Generated ${sizeName} variant`);

        } catch (error) {
            console.error(`Error generating ${sizeName} variant:`, error);
        }
    }

    // Build result with all CDN URLs
    // Send rawImageType to backend so gallery images get unique keys (gallery_1, gallery_2)
    const result = {
        success: true,
        entityType,
        entityId,
        imageType: rawImageType,  // Use rawImageType for unique keys in database
        originalKey,
        variants,
        processedAt: new Date().toISOString()
    };

    // Publish result to SQS for backend consumption
    if (RESULT_QUEUE_URL) {
        await publishResultToSQS(result);
    } else {
        console.warn('RESULT_QUEUE_URL not configured - skipping result publication');
    }

    console.log(`Image processing complete: ${originalKey}`);

    return result;
}

/**
 * Generate a single image variant
 */
async function generateVariant(imageBuffer, config, format) {
    let pipeline = sharp(imageBuffer)
        .resize(config.width, config.height, {
            fit: config.fit,
            withoutEnlargement: true
        });

    if (format === 'webp') {
        pipeline = pipeline.webp({ quality: 80, effort: 4 });
    } else if (format === 'jpeg') {
        pipeline = pipeline.jpeg({ quality: 85, progressive: true });
    }

    return pipeline.toBuffer();
}

/**
 * Download image from S3
 */
async function downloadFromS3(bucket, key) {
    const command = new GetObjectCommand({ Bucket: bucket, Key: key });
    const response = await s3Client.send(command);

    // Convert stream to buffer
    const chunks = [];
    for await (const chunk of response.Body) {
        chunks.push(chunk);
    }
    return Buffer.concat(chunks);
}

/**
 * Upload image to S3 with proper headers
 */
async function uploadToS3(bucket, key, buffer, contentType) {
    const command = new PutObjectCommand({
        Bucket: bucket,
        Key: key,
        Body: buffer,
        ContentType: contentType,
        CacheControl: 'public, max-age=604800', // 7 days
        Metadata: {
            'processed-at': new Date().toISOString()
        }
    });

    await s3Client.send(command);
    console.log(`Uploaded: ${key} (${buffer.length} bytes)`);
}

/**
 * Get CDN URL for a processed image
 */
function getCdnUrl(key) {
    // Remove 'processed/' prefix since CloudFront origin path includes it
    const keyWithoutPrefix = key.replace(/^processed\//, '');

    if (CLOUDFRONT_DOMAIN) {
        return `https://${CLOUDFRONT_DOMAIN}/${keyWithoutPrefix}`;
    }

    // Fallback to S3 direct URL
    return `https://${MEDIA_BUCKET}.s3.amazonaws.com/${key}`;
}

/**
 * Publish processing result to SQS for backend consumption
 */
async function publishResultToSQS(result) {
    try {
        const command = new SendMessageCommand({
            QueueUrl: RESULT_QUEUE_URL,
            MessageBody: JSON.stringify(result),
            MessageAttributes: {
                'EntityType': {
                    DataType: 'String',
                    StringValue: result.entityType
                },
                'EntityId': {
                    DataType: 'String',
                    StringValue: result.entityId
                },
                'ImageType': {
                    DataType: 'String',
                    StringValue: result.imageType
                }
            }
        });

        await sqsClient.send(command);
        console.log('Result published to SQS successfully');
    } catch (error) {
        console.error('Error publishing result to SQS:', error);
        // Don't throw - image processing succeeded even if notification failed
    }
}
