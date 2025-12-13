# Image Processor - Clone-Based Pipeline

> Efficient WebP/JPEG variant generation using Sharp.js clone-based pipeline

## 🚀 Quick Start

### 1. Install Dependencies

```bash
cd scripts/image-processor
npm install
```

### 2. Run Demo (No Input Image Needed)

```bash
npm run demo
```

This creates a test image and generates all variants to verify the setup works.

### 3. Process Your Images

```bash
# Basic usage
node process-image.js <input-file> <image-type> <entity-type> <entity-id> [output-dir]

# Examples:
node process-image.js ./photo.jpg primary menu-item 501 ./output
node process-image.js ./logo.png logo vendor 101 ./output
node process-image.js ./storefront.jpg storefront branch 205 ./output
```

---

## 📋 Supported Image Types

### Vendor Images

| Type | Command | Generated Sizes |
|------|---------|-----------------|
| **Logo** | `logo vendor <id>` | 64px, 128px, 256px |
| **Cover Photo** | `cover vendor <id>` | 200px, 400px, 800px, 1200px |

### Branch Images

| Type | Command | Generated Sizes |
|------|---------|-----------------|
| **Storefront** | `storefront branch <id>` | 150px, 300px, 600px, 900px |
| **Interior** | `interior branch <id>` | 400px, 800px, 1200px |
| **Menu Board** | `menu_board branch <id>` | 800px, 1200px |
| **Kitchen** | `kitchen branch <id>` | 400px, 800px |

### Menu Item Images

| Type | Command | Generated Sizes |
|------|---------|-----------------|
| **Primary** | `primary menu-item <id>` | 80px, 160px, 320px, 640px |
| **Gallery** | `gallery menu-item <id>` | 200px, 400px, 800px |

---

## 📂 Output Structure

```
output/
├── vendors/
│   └── 101/
│       ├── logo_thumbnail.webp     ← 64px WebP
│       ├── logo_thumbnail.jpeg     ← 64px JPEG fallback
│       ├── logo_small.webp         ← 128px WebP
│       ├── logo_small.jpeg         ← 128px JPEG fallback
│       ├── logo_medium.webp        ← 256px WebP
│       └── logo_medium.jpeg        ← 256px JPEG fallback
├── branches/
│   └── 205/
│       ├── storefront_thumbnail.webp
│       ├── storefront_small.webp
│       └── ...
└── menu-items/
    └── 501/
        ├── primary_thumbnail.webp
        ├── primary_small.webp
        └── ...
```

---

## 🏗️ How It Works

### Clone-Based Pipeline (Why It's Efficient)

The script uses Sharp's **clone-based pipeline** architecture:

```
                         ┌──────────────────────────────────────────┐
                         │           SINGLE IMAGE DECODE            │
                         │     (Sharp loads image once into RAM)    │
                         └─────────────────┬────────────────────────┘
                                           │
              ┌────────────────────────────┼────────────────────────────┐
              │                            │                            │
              ▼                            ▼                            ▼
      ┌──────────────┐            ┌──────────────┐            ┌──────────────┐
      │   clone()    │            │   clone()    │            │   clone()    │
      │  thumbnail   │            │    small     │            │   medium     │
      └──────┬───────┘            └──────┬───────┘            └──────┬───────┘
             │                           │                           │
     ┌───────┴───────┐           ┌───────┴───────┐           ┌───────┴───────┐
     │               │           │               │           │               │
     ▼               ▼           ▼               ▼           ▼               ▼
  ┌──────┐       ┌──────┐    ┌──────┐       ┌──────┐    ┌──────┐       ┌──────┐
  │ WebP │       │ JPEG │    │ WebP │       │ JPEG │    │ WebP │       │ JPEG │
  │ 64px │       │ 64px │    │128px │       │128px │    │256px │       │256px │
  └──────┘       └──────┘    └──────┘       └──────┘    └──────┘       └──────┘
```

**Benefits:**
- 🚀 **Single decode** - Image is loaded into memory once
- ⚡ **Parallel processing** - All variants processed simultaneously
- 💾 **Memory efficient** - Sharp handles memory management
- 🎯 **Fast execution** - Typically 200-500ms for all variants

---

## 🔧 Integration with AWS Lambda

For Lambda deployment, use this pattern:

```javascript
import { processImage, generateCdnUrls } from './process-image.js';
import { S3Client, GetObjectCommand, PutObjectCommand } from '@aws-sdk/client-s3';

export const handler = async (event) => {
  const s3Client = new S3Client({ region: 'ap-south-1' });
  
  // Get the uploaded object from S3 event
  const bucket = event.Records[0].s3.bucket.name;
  const key = event.Records[0].s3.object.key;
  
  // Download the original image
  const getCommand = new GetObjectCommand({ Bucket: bucket, Key: key });
  const response = await s3Client.send(getCommand);
  const imageBuffer = await streamToBuffer(response.Body);
  
  // Parse path: originals/vendors/101/logo_original.jpg
  const [_, entityFolder, entityId, filename] = key.split('/');
  const imageType = filename.split('_')[0]; // 'logo'
  const entityType = entityFolder.replace('s', ''); // 'vendor'
  
  // Process the image
  const results = await processImage(
    imageBuffer,
    imageType,
    entityType,
    entityId,
    '/tmp'
  );
  
  // Upload all variants to S3
  for (const [sizeName, formats] of Object.entries(results.variants)) {
    for (const [format, info] of Object.entries(formats)) {
      const variantKey = `processed/${entityFolder}/${entityId}/${info.filename}`;
      const fileBuffer = await fs.readFile(info.path);
      
      await s3Client.send(new PutObjectCommand({
        Bucket: bucket,
        Key: variantKey,
        Body: fileBuffer,
        ContentType: `image/${format}`,
        CacheControl: 'public, max-age=2592000'
      }));
    }
  }
  
  // Generate CDN URLs for callback
  const urls = generateCdnUrls(results, entityType, entityId);
  
  return { statusCode: 200, body: JSON.stringify(urls) };
};
```

---

## 📊 Quality Settings

Based on the specification document:

| Format | Quality | Notes |
|--------|---------|-------|
| WebP | 80% | Primary format, 25-35% smaller than JPEG |
| JPEG | 85% | Fallback for older browsers |

### WebP Encoding Options

```javascript
{
  quality: 80,
  effort: 4,           // Balance between speed and compression (0-6)
  smartSubsample: true // Better color accuracy
}
```

### JPEG Encoding Options

```javascript
{
  quality: 85,
  mozjpeg: true,              // Better compression algorithm
  chromaSubsampling: '4:2:0'  // Standard chroma sampling
}
```

---

## 🧪 Testing

Run the demo to verify everything works:

```bash
npm run demo
```

Expected output:

```
🎮 Running Demo Mode...

📸 Created test image (1200x1200 PNG)

═══════════════════════════════════════════════════════════
Demo 1: Menu Item Primary Image
═══════════════════════════════════════════════════════════

📸 Input image: 1200x1200 (png)

🚀 Processing 8 variants in parallel...
   ✓ primary_thumbnail.webp: 80x80 (2.1 KB)
   ✓ primary_thumbnail.jpeg: 80x80 (2.8 KB)
   ✓ primary_small.webp: 160x160 (5.4 KB)
   ✓ primary_small.jpeg: 160x160 (7.2 KB)
   ✓ primary_medium.webp: 320x320 (14.2 KB)
   ✓ primary_medium.jpeg: 320x320 (19.8 KB)
   ✓ primary_large.webp: 640x640 (42.1 KB)
   ✓ primary_large.jpeg: 640x640 (58.3 KB)

✅ All variants generated in 287ms
```

---

## 📁 Project Structure

```
scripts/image-processor/
├── package.json          # Dependencies and scripts
├── process-image.js      # Main processor script
└── README.md             # This file
```

---

## 🔗 Related Documentation

- [Image Storage and Rendering Specification](../../docs/IMAGE_STORAGE_AND_RENDERING_SPECIFICATION.md)
- [Sharp.js Documentation](https://sharp.pixelplumbing.com/)
- [AWS Lambda with Sharp](https://sharp.pixelplumbing.com/install#aws-lambda)

---

## 💡 Tips

1. **Lambda Layer**: For AWS Lambda, use a pre-built Sharp layer or include `sharp` in your deployment package with the correct platform binaries.

2. **Memory**: Sharp is efficient but complex images may need more memory. Start with 1536MB for Lambda.

3. **Skip Upscaling**: The script automatically skips sizes larger than the original image to avoid quality loss.

4. **WebP Support Check**: Frontend should detect WebP support and fallback to JPEG:
   ```javascript
   const webpSupported = document.createElement('canvas')
     .toDataURL('image/webp')
     .startsWith('data:image/webp');
   ```
