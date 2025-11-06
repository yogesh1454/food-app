import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import featureFlags, { FeatureFlags } from '../services/featureFlags';

interface FeatureGateProps {
  feature: keyof FeatureFlags;
  fallback?: React.ReactNode;
  children: React.ReactNode;
  showDisabledMessage?: boolean;
}

/**
 * FeatureGate component - conditionally renders children based on feature flags
 */
export default function FeatureGate({
  feature,
  fallback,
  children,
  showDisabledMessage = false,
}: FeatureGateProps) {
  const isEnabled = featureFlags.isEnabled(feature);

  if (!isEnabled) {
    if (fallback) {
      return <>{fallback}</>;
    }
    
    return null;
  }

  return <>{children}</>;
}

const styles = StyleSheet.create({
  comingSoon: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#f9fafb',
    borderRadius: 8,
    marginVertical: 8,
  },
  comingSoonText: {
    marginLeft: 8,
    color: '#6b7280',
    fontSize: 14,
  },
});

/**
 * Higher-order component version for conditional rendering
 */
export function withFeatureFlag<P extends object>(
  feature: keyof FeatureFlags,
  Component: React.ComponentType<P>
) {
  return function FeatureFlaggedComponent(props: P) {
    const isEnabled = featureFlags.isEnabled(feature);
    
    if (!isEnabled) {
      return null;
    }
    
    return <Component {...props} />;
  };
}