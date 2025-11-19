import React from 'react';
import { View, ActivityIndicator, Text, StyleSheet } from 'react-native';
import { colors, spacing } from '../constants';

interface LoadingSpinnerProps {
  size?: 'small' | 'large';
  color?: string;
  text?: string;
  overlay?: boolean;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'large',
  color = colors.primary,
  text,
  overlay = false,
}) => {
  const content = (
    <View style={[styles.container, overlay && styles.overlay]}>
      <ActivityIndicator size={size} color={color} />
      {text && <Text style={[styles.text, { color }]}>{text}</Text>}
    </View>
  );

  if (overlay) {
    return <View style={styles.overlayContainer}>{content}</View>;
  }

  return content;
};

// Loading overlay for full screen loading
export const LoadingOverlay: React.FC<{ text?: string }> = ({ text }) => {
  return (
    <View style={styles.overlayContainer}>
      <LoadingSpinner size="large" text={text} overlay />
    </View>
  );
};

// Loading skeleton for content placeholders
export const LoadingSkeleton: React.FC<{
  width?: number | string;
  height?: number;
  style?: any;
}> = ({ width = '100%', height = 20, style }) => {
  return (
    <View
      style={[
        styles.skeleton,
        {
          width,
          height,
        },
        style,
      ]}
    />
  );
};

// Card skeleton for list items
export const CardSkeleton: React.FC = () => {
  return (
    <View style={styles.cardSkeleton}>
      <LoadingSkeleton width={60} height={60} style={styles.skeletonAvatar} />
      <View style={styles.skeletonContent}>
        <LoadingSkeleton width="80%" height={16} style={styles.skeletonLine} />
        <LoadingSkeleton width="60%" height={14} style={styles.skeletonLine} />
        <LoadingSkeleton width="40%" height={12} style={styles.skeletonLine} />
      </View>
    </View>
  );
};

// List skeleton for multiple items
export const ListSkeleton: React.FC<{ count?: number }> = ({ count = 3 }) => {
  return (
    <View>
      {Array.from({ length: count }).map((_, index) => (
        <CardSkeleton key={index} />
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.md,
  },
  overlay: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: spacing.md,
  },
  overlayContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  text: {
    marginTop: spacing.sm,
    fontSize: 14,
    fontWeight: '500',
  },
  skeleton: {
    backgroundColor: colors.border,
    borderRadius: spacing.sm,
  },
  skeletonAvatar: {
    borderRadius: spacing.lg,
  },
  skeletonContent: {
    flex: 1,
    marginLeft: spacing.md,
  },
  skeletonLine: {
    marginBottom: spacing.sm,
  },
  cardSkeleton: {
    flexDirection: 'row',
    backgroundColor: colors.surface,
    padding: spacing.md,
    marginBottom: spacing.sm,
    borderRadius: spacing.md,
    alignItems: 'center',
  },
});

export default LoadingSpinner;