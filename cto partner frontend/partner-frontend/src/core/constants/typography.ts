// Typography system following design principles
import { spacing } from './spacing';

// Font families
export const fontFamilies = {
  primary: 'System',
  monospace: 'System',
  primaryBold: 'System',
  primarySemibold: 'System',
} as const;

// Font sizes following consistent scale
export const fontSizes = {
  xs: 12,    // Caption, helper text
  sm: 14,    // Small text, labels
  base: 16,  // Body text, default
  lg: 18,    // Large text, emphasis
  xl: 20,    // Small headings
  '2xl': 24, // Medium headings
  '3xl': 30, // Large headings
  '4xl': 36, // Display text
} as const;

// Font weights
export const fontWeights = {
  normal: '400' as const,
  medium: '500' as const,
  semibold: '600' as const,
  bold: '700' as const,
} as const;

// Line heights matching font sizes for optimal readability
export const lineHeights = {
  xs: 16,    // 1.33
  sm: 20,    // 1.43
  base: 24,  // 1.5
  lg: 28,    // 1.56
  xl: 28,    // 1.4
  '2xl': 32, // 1.33
  '3xl': 36, // 1.2
  '4xl': 40, // 1.11
} as const;

// Letter spacing for better readability
export const letterSpacing = {
  tight: -0.5,
  normal: 0,
  wide: 0.5,
} as const;

// Text style presets for consistent typography
export const textStyles = {
  // Body text
  body: {
    fontSize: fontSizes.base,
    lineHeight: lineHeights.base,
    fontWeight: fontWeights.normal,
  },
  bodyMedium: {
    fontSize: fontSizes.base,
    lineHeight: lineHeights.base,
    fontWeight: fontWeights.medium,
  },
  
  // Small text
  small: {
    fontSize: fontSizes.sm,
    lineHeight: lineHeights.sm,
    fontWeight: fontWeights.normal,
  },
  smallMedium: {
    fontSize: fontSizes.sm,
    lineHeight: lineHeights.sm,
    fontWeight: fontWeights.medium,
  },
  
  // Labels
  label: {
    fontSize: fontSizes.sm,
    lineHeight: lineHeights.sm,
    fontWeight: fontWeights.medium,
  },
  
  // Headings
  heading1: {
    fontSize: fontSizes['4xl'],
    lineHeight: lineHeights['4xl'],
    fontWeight: fontWeights.bold,
  },
  heading2: {
    fontSize: fontSizes['3xl'],
    lineHeight: lineHeights['3xl'],
    fontWeight: fontWeights.bold,
  },
  heading3: {
    fontSize: fontSizes['2xl'],
    lineHeight: lineHeights['2xl'],
    fontWeight: fontWeights.semibold,
  },
  heading4: {
    fontSize: fontSizes.xl,
    lineHeight: lineHeights.xl,
    fontWeight: fontWeights.semibold,
  },
  
  // Interactive elements
  button: {
    fontSize: fontSizes.base,
    lineHeight: lineHeights.base,
    fontWeight: fontWeights.semibold,
  },
  caption: {
    fontSize: fontSizes.xs,
    lineHeight: lineHeights.xs,
    fontWeight: fontWeights.normal,
  },
} as const;