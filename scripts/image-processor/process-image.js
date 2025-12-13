#!/usr/bin/env node

/**
 * Image Processor - Clone-Based Pipeline for Efficient Variant Generation
 * ========================================================================
 * 
 * This script implements an efficient clone-based pipeline using Sharp.js
 * to generate all required WebP and JPEG variants simultaneously from a
 * single input image.
 * 
 * Key Features:
 * - Single decode, multiple outputs (clone-based pipeline)
 * - Generates both WebP (primary) and JPEG (fallback) formats
 * - Configurable size presets based on image type
 * - Memory-efficient parallel processing
 * 
 * Usage:
 *   node process-image.js <input-file> <image-type> <entity-type> <entity-id> [output-dir]
 * 
 * Example:
 *   node process-image.js ./photo.jpg primary menu-item 501 ./output
 */

import sharp from 'sharp';
import fs from 'fs/promises';
import path from 'path';

// ============================================================================
// CONFIGURATION - Based on IMAGE_STORAGE_AND_RENDERING_SPECIFICATION.md
// ============================================================================

/**
 * Image type configurations with their required sizes
 * Sizes are configured per the specification document
 */
const IMAGE_TYPE_CONFIG = {
    // Vendor Images
    'logo': {
        sizes: {
            thumbnail: 64,
            small: 128,
            medium: 256
        },
        aspectRatio: '1:1',
        quality: { webp: 80, jpeg: 85 }
    },
    'cover': {
        sizes: {
            thumbnail: 200,
            small: 400,
            medium: 800,
            large: 1200
        },
        aspectRatio: '16:9',
        quality: { webp: 80, jpeg: 85 }
    },

    // Branch Images
    'storefront': {
        sizes: {
            thumbnail: 150,
            small: 300,
            medium: 600,
            large: 900
        },
        aspectRatio: null,
        quality: { webp: 80, jpeg: 85 }
    },
    'interior': {
        sizes: {
            small: 400,
            medium: 800,
            large: 1200
        },
        aspectRatio: null,
        quality: { webp: 80, jpeg: 85 }
    },
    'menu_board': {
        sizes: {
            medium: 800,
            large: 1200
        },
        aspectRatio: null,
        quality: { webp: 80, jpeg: 85 }
    },
    'kitchen': {
        sizes: {
            small: 400,
            medium: 800
        },
        aspectRatio: null,
        quality: { webp: 80, jpeg: 85 }
    },

    // Menu Item Images
    'primary': {
        sizes: {
            thumbnail: 80,
            small: 160,
            medium: 320,
            large: 640
        },
        aspectRatio: '1:1',
        quality: { webp: 80, jpeg: 85 }
    },
    'gallery': {
        sizes: {
            small: 200,
            medium: 400,
            large: 800
        },
        aspectRatio: null,
        quality: { webp: 80, jpeg: 85 }
    }
};

// Entity type to folder mapping
const ENTITY_FOLDERS = {
    'vendor': 'vendors',
    'branch': 'branches',
    'menu-item': 'menu-items'
};

// ============================================================================
// CORE PROCESSING FUNCTIONS
// ============================================================================

/**
 * Generates all image variants using Sharp's clone-based pipeline
 * This is the most efficient approach as the image is decoded only once
 * 
 * @param {Buffer|string} input - Input image buffer or file path
 * @param {string} imageType - Type of image (logo, cover, primary, etc.)
 * @param {string} entityType - Entity type (vendor, branch, menu-item)
 * @param {string} entityId - Entity identifier
 * @param {string} outputDir - Output directory path
 * @returns {Promise<Object>} - Object containing all generated variant URLs
 */
async function processImage(input, imageType, entityType, entityId, outputDir) {
    const config = IMAGE_TYPE_CONFIG[imageType];

    if (!config) {
        throw new Error(`Unknown image type: ${imageType}. Valid types: ${Object.keys(IMAGE_TYPE_CONFIG).join(', ')}`);
    }

    const entityFolder = ENTITY_FOLDERS[entityType];
    if (!entityFolder) {
        throw new Error(`Unknown entity type: ${entityType}. Valid types: ${Object.keys(ENTITY_FOLDERS).join(', ')}`);
    }

    // Create output directory structure
    const outputPath = path.join(outputDir, entityFolder, entityId);
    await fs.mkdir(outputPath, { recursive: true });

    // Load the image once - Sharp will keep this in memory for cloning
    const baseImage = sharp(input, { failOnError: true });

    // Get image metadata for smart resizing decisions
    const metadata = await baseImage.metadata();
    console.log(`\n📸 Input image: ${metadata.width}x${metadata.height} (${metadata.format})`);

    const results = {
        original: null,
        variants: {},
        metadata: {
            originalWidth: metadata.width,
            originalHeight: metadata.height,
            originalFormat: metadata.format,
            processedAt: new Date().toISOString()
        }
    };

    // Create an array of all processing promises
    const processingPromises = [];

    // Process each size variant
    for (const [sizeName, targetWidth] of Object.entries(config.sizes)) {
        // Skip if target size is larger than original (upscaling not recommended)
        if (targetWidth > metadata.width) {
            console.log(`⚠️  Skipping ${sizeName} (${targetWidth}px) - larger than original (${metadata.width}px)`);
            continue;
        }

        // WebP variant (primary format)
        processingPromises.push(
            generateVariant(
                baseImage.clone(), // Clone for independent processing
                sizeName,
                targetWidth,
                'webp',
                config.quality.webp,
                imageType,
                outputPath
            ).then(result => {
                if (!results.variants[sizeName]) {
                    results.variants[sizeName] = {};
                }
                results.variants[sizeName].webp = result;
            })
        );

        // JPEG variant (fallback format)
        processingPromises.push(
            generateVariant(
                baseImage.clone(), // Clone for independent processing
                sizeName,
                targetWidth,
                'jpeg',
                config.quality.jpeg,
                imageType,
                outputPath
            ).then(result => {
                if (!results.variants[sizeName]) {
                    results.variants[sizeName] = {};
                }
                results.variants[sizeName].jpeg = result;
            })
        );
    }

    // Execute all processing in parallel
    console.log(`\n🚀 Processing ${processingPromises.length} variants in parallel...`);
    const startTime = Date.now();

    await Promise.all(processingPromises);

    const processingTime = Date.now() - startTime;
    console.log(`\n✅ All variants generated in ${processingTime}ms`);

    results.metadata.processingTimeMs = processingTime;
    results.metadata.variantCount = processingPromises.length;

    return results;
}

/**
 * Generates a single image variant
 * 
 * @param {sharp.Sharp} sharpInstance - Cloned Sharp instance
 * @param {string} sizeName - Name of the size (thumbnail, small, etc.)
 * @param {number} width - Target width in pixels
 * @param {string} format - Output format (webp or jpeg)
 * @param {number} quality - Compression quality (0-100)
 * @param {string} imageType - Type of image for naming
 * @param {string} outputPath - Directory to save the file
 * @returns {Promise<Object>} - Variant info including path and size
 */
async function generateVariant(sharpInstance, sizeName, width, format, quality, imageType, outputPath) {
    const filename = `${imageType}_${sizeName}.${format}`;
    const filePath = path.join(outputPath, filename);

    // Configure resize operation
    let pipeline = sharpInstance
        .resize({
            width: width,
            withoutEnlargement: true, // Never upscale
            fit: 'inside'            // Maintain aspect ratio
        });

    // Apply format-specific encoding
    if (format === 'webp') {
        pipeline = pipeline.webp({
            quality: quality,
            effort: 4,           // Balance between speed and compression
            smartSubsample: true // Better color accuracy
        });
    } else if (format === 'jpeg') {
        pipeline = pipeline.jpeg({
            quality: quality,
            mozjpeg: true,       // Use mozjpeg encoder for better compression
            chromaSubsampling: '4:2:0'
        });
    }

    // Write to file
    const info = await pipeline.toFile(filePath);

    console.log(`   ✓ ${filename}: ${info.width}x${info.height} (${(info.size / 1024).toFixed(1)} KB)`);

    return {
        path: filePath,
        filename: filename,
        width: info.width,
        height: info.height,
        size: info.size,
        format: format
    };
}

/**
 * Process multiple images in batch
 * 
 * @param {Array<Object>} images - Array of image processing requests
 * @returns {Promise<Array<Object>>} - Array of processing results
 */
async function processBatch(images) {
    console.log(`\n📦 Processing batch of ${images.length} images...`);

    const results = [];

    for (const image of images) {
        try {
            const result = await processImage(
                image.input,
                image.imageType,
                image.entityType,
                image.entityId,
                image.outputDir
            );
            results.push({ success: true, ...image, result });
        } catch (error) {
            console.error(`❌ Failed to process ${image.input}: ${error.message}`);
            results.push({ success: false, ...image, error: error.message });
        }
    }

    return results;
}

/**
 * Generate CDN-style URLs for processed variants
 * 
 * @param {Object} results - Processing results from processImage
 * @param {string} entityType - Entity type
 * @param {string} entityId - Entity identifier
 * @param {string} cdnBase - CDN base URL
 * @returns {Object} - URL mapping for database storage
 */
function generateCdnUrls(results, entityType, entityId, cdnBase = 'https://cdn.foodapp.com') {
    const entityFolder = ENTITY_FOLDERS[entityType];
    const urls = {};

    for (const [sizeName, formats] of Object.entries(results.variants)) {
        urls[sizeName] = {};

        if (formats.webp) {
            urls[sizeName].webp = `${cdnBase}/${entityFolder}/${entityId}/${formats.webp.filename}`;
        }
        if (formats.jpeg) {
            urls[sizeName].jpeg = `${cdnBase}/${entityFolder}/${entityId}/${formats.jpeg.filename}`;
        }
    }

    return urls;
}

// ============================================================================
// CLI INTERFACE
// ============================================================================

async function main() {
    const args = process.argv.slice(2);

    // Check for demo mode
    if (args.includes('--demo')) {
        await runDemo();
        return;
    }

    // Print usage if no arguments
    if (args.length < 4) {
        console.log(`
╔══════════════════════════════════════════════════════════════════════════════╗
║                     Image Processor - Clone-Based Pipeline                    ║
╠══════════════════════════════════════════════════════════════════════════════╣
║  Efficiently generates WebP and JPEG variants from a single input image      ║
╚══════════════════════════════════════════════════════════════════════════════╝

Usage:
  node process-image.js <input-file> <image-type> <entity-type> <entity-id> [output-dir]

Arguments:
  input-file    Path to the input image (JPEG, PNG, or WebP)
  image-type    Type of image:
                  - Vendor: logo, cover
                  - Branch: storefront, interior, menu_board, kitchen
                  - Menu Item: primary, gallery
  entity-type   Entity type: vendor, branch, menu-item
  entity-id     Unique identifier for the entity
  output-dir    (Optional) Output directory (default: ./output)

Examples:
  # Process a vendor logo
  node process-image.js ./logo.png logo vendor 101 ./output

  # Process a menu item primary image
  node process-image.js ./burger.jpg primary menu-item 501 ./output

  # Process a branch storefront
  node process-image.js ./store.jpg storefront branch 205 ./output

  # Run demo with a generated test image
  node process-image.js --demo

Generated Variants:
  Each input generates multiple sizes in both WebP (primary) and JPEG (fallback)
  formats. See configuration for size details per image type.
`);
        process.exit(0);
    }

    // Parse arguments
    const [inputFile, imageType, entityType, entityId, outputDir = './output'] = args;

    // Validate input file exists
    try {
        await fs.access(inputFile);
    } catch {
        console.error(`❌ Error: Input file not found: ${inputFile}`);
        process.exit(1);
    }

    console.log('\n🖼️  Image Processor - Clone-Based Pipeline');
    console.log('═'.repeat(50));
    console.log(`   Input:       ${inputFile}`);
    console.log(`   Image Type:  ${imageType}`);
    console.log(`   Entity:      ${entityType}/${entityId}`);
    console.log(`   Output:      ${outputDir}`);

    try {
        // Process the image
        const results = await processImage(
            inputFile,
            imageType,
            entityType,
            entityId,
            outputDir
        );

        // Generate CDN URLs
        const urls = generateCdnUrls(results, entityType, entityId);

        console.log('\n📋 Generated CDN URLs (for database storage):');
        console.log(JSON.stringify(urls, null, 2));

        console.log('\n📊 Summary:');
        console.log(`   Variants created: ${results.metadata.variantCount}`);
        console.log(`   Processing time:  ${results.metadata.processingTimeMs}ms`);
        console.log(`   Source:           ${results.metadata.originalWidth}x${results.metadata.originalHeight}`);

    } catch (error) {
        console.error(`\n❌ Error: ${error.message}`);
        process.exit(1);
    }
}

/**
 * Demo mode - creates a test image and processes it
 */
async function runDemo() {
    console.log('\n🎮 Running Demo Mode...\n');

    // Create a demo output directory
    const demoDir = './demo-output';
    await fs.mkdir(demoDir, { recursive: true });

    // Create a test image using Sharp with a simple gradient and shapes
    const svgContent = `<svg width="1200" height="1200" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="bgGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style="stop-color:#FF6B35;stop-opacity:1" />
        <stop offset="100%" style="stop-color:#F7C59F;stop-opacity:1" />
      </linearGradient>
    </defs>
    <rect x="0" y="0" width="1200" height="1200" fill="url(#bgGradient)"/>
    <circle cx="600" cy="450" r="280" fill="#FFFACD" stroke="#FF6B35" stroke-width="12"/>
    <circle cx="600" cy="420" r="120" fill="#8B4513"/>
    <ellipse cx="600" cy="420" rx="100" ry="40" fill="#D2691E"/>
    <path d="M720 380 Q780 380 780 440 Q780 500 720 500" stroke="#8B4513" stroke-width="20" fill="none"/>
    <rect x="480" y="550" width="240" height="40" rx="20" fill="#8B4513"/>
    <text x="600" y="850" font-family="Arial, sans-serif" font-size="72" fill="white" text-anchor="middle" font-weight="bold">Tea and Snacks</text>
    <text x="600" y="950" font-family="Arial, sans-serif" font-size="36" fill="rgba(255,255,255,0.8)" text-anchor="middle">Demo Test Image</text>
    <text x="600" y="1050" font-family="Arial, sans-serif" font-size="28" fill="rgba(255,255,255,0.6)" text-anchor="middle">1200 x 1200 pixels</text>
  </svg>`;

    const testImageBuffer = await sharp(Buffer.from(svgContent))
        .png()
        .toBuffer();
    console.log('📸 Created test image (1200x1200 PNG)');

    // Demo: Process as menu item primary image
    console.log('\n═══════════════════════════════════════════════════════════');
    console.log('Demo 1: Menu Item Primary Image');
    console.log('═══════════════════════════════════════════════════════════');

    const primaryResults = await processImage(
        testImageBuffer,
        'primary',
        'menu-item',
        'demo-501',
        demoDir
    );

    const primaryUrls = generateCdnUrls(primaryResults, 'menu-item', 'demo-501');
    console.log('\n📋 JSONB Structure for Database:');
    console.log(JSON.stringify({ primary: primaryUrls }, null, 2));

    // Demo: Process as vendor logo
    console.log('\n═══════════════════════════════════════════════════════════');
    console.log('Demo 2: Vendor Logo');
    console.log('═══════════════════════════════════════════════════════════');

    const logoResults = await processImage(
        testImageBuffer,
        'logo',
        'vendor',
        'demo-101',
        demoDir
    );

    const logoUrls = generateCdnUrls(logoResults, 'vendor', 'demo-101');
    console.log('\n📋 JSONB Structure for Database:');
    console.log(JSON.stringify({ logo: logoUrls }, null, 2));

    console.log('\n✅ Demo complete! Check the', demoDir, 'directory for generated files.');
    console.log('\n📂 Generated Files:');

    // List generated files
    const listFiles = async (dir, prefix = '') => {
        const entries = await fs.readdir(dir, { withFileTypes: true });
        for (const entry of entries) {
            if (entry.isDirectory()) {
                console.log(`   ${prefix}📁 ${entry.name}/`);
                await listFiles(path.join(dir, entry.name), prefix + '   ');
            } else {
                const stats = await fs.stat(path.join(dir, entry.name));
                console.log(`   ${prefix}📄 ${entry.name} (${(stats.size / 1024).toFixed(1)} KB)`);
            }
        }
    };

    await listFiles(demoDir);
}

// Export for use as module
export { processImage, processBatch, generateCdnUrls, IMAGE_TYPE_CONFIG };

// Run CLI if executed directly
main().catch(console.error);
