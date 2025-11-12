# Restaurant Partner App - Asset Creation Guide

*Generated on: November 6, 2025*

## Overview

This guide provides detailed specifications for creating professional assets for the Restaurant Partner App. All current assets are placeholder files that need to be replaced with production-quality designs.

## Current Asset Status

**Location**: `/assets/` directory  
**Issue**: All files contain placeholder content  
**Impact**: Poor user experience and branding

## Required Assets

### 1. App Icon (`icon.png`)
**Specifications**:
- **Format**: PNG with transparent background
- **Size**: 1024x1024 pixels (retina quality)
- **Design Elements**:
  - Clean, professional logo representing restaurant/food service
  - Scalable design (will be resized for different platforms)
  - Brand colors should align with app theme (green: #16a34a)
  - Minimalist design for app icon clarity

**Export Requirements**:
- 1024x1024px (original)
- 512x512px (high resolution)
- 256x256px (medium resolution)
- 128x128px (standard resolution)
- 64x64px (low resolution)

### 2. Adaptive Icon (`adaptive-icon.png`) - Android Only
**Specifications**:
- **Format**: PNG
- **Size**: 1024x1024 pixels
- **Design**: Foreground layer only (background handled by Android)
- **Purpose**: Android adaptive icon support

**Android Specifications**:
- Safe area: Center 72x72dp area for app content
- Maximum image size: 432x432dp
- Corner radius: Adaptive based on device theme

### 3. Splash Screen (`splash.png`)
**Specifications**:
- **Format**: PNG
- **Size**: 1284x2778 pixels (iPhone X+ resolution)
- **Background**: Single color background matching brand (#16a34a)
- **Design Elements**:
  - Center-aligned app logo/icon
  - App name or tagline
  - Loading indicator or progress bar
  - Minimal, clean design

**Platform-Specific Requirements**:

#### iOS Splash Screen
- **Size**: 1284x2778px (iPhone X/XS/11/12/13/14)
- **Background Color**: #16a34a (brand green)
- **Foreground**: White or light colored elements

#### Android Splash Screen
- **Size**: 1080x1920px (common Android resolution)
- **Background**: Same as iOS
- **Format**: PNG with transparency support

#### Web Splash Screen
- **Size**: 1920x1080px (desktop web)
- **Background**: Responsive design
- **Format**: PNG with WebP fallback

### 4. Favicon (`favicon.png`)
**Specifications**:
- **Format**: PNG
- **Size**: 32x32 pixels
- **Design**: Simplified version of app icon
- **Purpose**: Web browser tab icon

**Additional Web Assets**:
- 16x16px (standard favicon)
- 48x48px (high DPI displays)
- 192x192px (PWA manifest)

## Design Guidelines

### Color Palette
Based on the app's design system:

```css
/* Primary Colors */
--brand-green: #16a34a;
--brand-green-light: #22c55e;
--brand-green-dark: #15803d;

/* Neutral Colors */
--white: #ffffff;
--light-gray: #f8fafc;
--medium-gray: #64748b;
--dark-gray: #1e293b;

/* Status Colors */
--success: #22c55e;
--warning: #f59e0b;
--error: #ef4444;
--info: #3b82f6;
```

### Typography
- **Primary Font**: System fonts (SF Pro on iOS, Roboto on Android)
- **App Name Font**: Bold, readable at small sizes
- **Loading Text**: Regular weight, high contrast

### Design Principles
1. **Consistency**: Align with existing app design system
2. **Scalability**: Clear at all sizes (from 16x16 to 1024x1024)
3. **Accessibility**: High contrast, readable fonts
4. **Platform-Optimized**: Respect platform-specific guidelines
5. **Brand Recognition**: Memorable, distinctive design

## Asset File Structure

```
assets/
├── icon.png                    # Main app icon (1024x1024)
├── icon-512.png               # High resolution icon (512x512)
├── icon-256.png               # Medium resolution icon (256x256)
├── icon-128.png               # Standard icon (128x128)
├── icon-64.png                # Low resolution icon (64x64)
├── adaptive-icon.png          # Android adaptive icon (1024x1024)
├── splash.png                 # Universal splash screen (1284x2778)
├── splash-ios.png             # iOS-specific splash screen
├── splash-android.png         # Android-specific splash screen
├── splash-web.png             # Web-specific splash screen (1920x1080)
├── favicon.png                # Standard favicon (32x32)
├── favicon-16.png             # Small favicon (16x16)
├── favicon-48.png             # High DPI favicon (48x48)
├── favicon-192.png            # PWA manifest icon (192x192)
└── adaptive-icon-background.png # Android background (optional)
```

## Implementation Steps

### Step 1: Design Creation
1. Create app icon in vector format (SVG/AI)
2. Test icon at all required sizes
3. Verify design clarity and readability
4. Create splash screen variations for each platform

### Step 2: Asset Export
1. Export PNG files with proper dimensions
2. Optimize file sizes (compress without quality loss)
3. Test on different background colors
4. Verify transparency and edge quality

### Step 3: Configuration Updates
Update `app.json` to reference new assets:

```json
{
  "expo": {
    "icon": "./assets/icon.png",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#16a34a"
    },
    "android": {
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#16a34a"
      }
    },
    "web": {
      "favicon": "./assets/favicon.png"
    }
  }
}
```

### Step 4: Testing
1. Test app icon on all platforms
2. Verify splash screen display and timing
3. Check favicon on web browsers
4. Validate adaptive icon on Android devices

## Quality Assurance Checklist

### App Icon
- [ ] Clear and recognizable at 16x16px
- [ ] Consistent across all sizes
- [ ] No pixelation or blurring
- [ ] Good contrast on light and dark backgrounds
- [ ] Brand colors match design system

### Splash Screen
- [ ] Loads quickly (under 3 seconds)
- [ ] Consistent branding elements
- [ ] Proper platform-specific sizing
- [ ] Clear loading indicators
- [ ] Responsive design for different screen sizes

### Favicon
- [ ] Visible and clear in browser tabs
- [ ] Proper scaling on high-DPI displays
- [ ] Works as PWA manifest icon
- [ ] Good contrast on different browser themes

## File Size Optimization

### Recommended File Sizes
- **App Icon**: < 50KB each
- **Splash Screen**: < 200KB
- **Favicon**: < 5KB

### Optimization Techniques
1. Use PNG optimization tools
2. Consider WebP format for web assets
3. Remove unnecessary metadata
4. Optimize color palette for smaller files
5. Use appropriate compression levels

## Brand Guidelines Integration

The new assets should align with:

### Logo/Icon Design
- Incorporate food/restaurant theme
- Use brand green (#16a34a) as primary color
- Maintain professional, trustworthy appearance
- Ensure scalability and memorability

### Visual Identity
- Consistent with existing UI components
- Professional color palette usage
- Clean, minimalist design approach
- Modern, accessible design principles

## Next Steps

1. **Design Phase** (3-5 days):
   - Create initial logo/icon concepts
   - Develop brand guidelines
   - Design splash screen concepts

2. **Asset Creation** (2-3 days):
   - Export all required sizes
   - Create platform-specific variations
   - Optimize file sizes

3. **Integration** (1 day):
   - Update app.json configuration
   - Test on all platforms
   - Verify display quality

4. **Quality Assurance** (1 day):
   - Cross-platform testing
   - Performance validation
   - Accessibility verification

## Professional Asset Creation Tools

### Recommended Design Tools
- **Figma**: Vector design, export automation
- **Adobe Illustrator**: Professional logo design
- **Sketch**: UI design and asset export
- **Canva**: Quick asset creation (for simple designs)

### Optimization Tools
- **TinyPNG**: PNG compression
- **ImageOptim**: Mac image optimization
- **Squoosh**: Web-based compression
- **Ezgif**: GIF optimization

## File Delivery Specifications

When assets are ready for integration:

1. **Source Files**: Provide vector source files (AI/SVG)
2. **Export Files**: All PNG exports in organized folders
3. **Documentation**: Asset usage guidelines
4. **Testing**: Assets tested across platforms
5. **Optimization**: Files optimized for web and mobile

## Budget Considerations

- **Professional Design**: $500-1000 for complete asset creation
- **Designer Time**: 5-7 days for full asset package
- **Tools**: $20-50/month for design software
- **Quality Assurance**: 1-2 days testing and optimization

This guide ensures professional, high-quality assets that enhance the Restaurant Partner App's brand identity and user experience across all platforms.