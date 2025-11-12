// Consistent spacing scale following 8px grid system
export const spacing = {
  // Base spacing unit (4px)
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
  
  // Multiples for larger spacing
  '2xl': 32,
  '3xl': 48,
  '4xl': 64,
  '5xl': 80,
  
  // Specific spacing patterns
  section: 24,
  container: 16,
  component: 12,
  element: 8,
  
  // Edge cases
  none: 0,
  hairline: 1,
} as const;

// Type helper for spacing values
export type Spacing = keyof typeof spacing | number;

// Pre-calculated spacing values for performance
export const spacingValues = {
  xs: spacing.xs,
  sm: spacing.sm,
  md: spacing.md,
  lg: spacing.lg,
  xl: spacing.xl,
  xxl: spacing.xxl,
  xxxl: spacing.xxxl,
  '2xl': spacing['2xl'],
  '3xl': spacing['3xl'],
  '4xl': spacing['4xl'],
  '5xl': spacing['5xl'],
};