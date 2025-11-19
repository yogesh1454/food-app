interface FeatureFlags {
  // Core features
  imageUpload: boolean;
  documentUpload: boolean;
  notifications: boolean;
  
  // Beta features
  voiceOrders: boolean;
  analytics: boolean;
  
  // Restaurant-specific features
  multiLocation: boolean;
  staffManagement: boolean;
  customMenuThemes: boolean;
}

const DEFAULT_FLAGS: FeatureFlags = {
  // Core features - enabled by default
  imageUpload: true,
  documentUpload: true,
  notifications: true,
  
  // Beta features - enabled by default for testing
  voiceOrders: true,
  analytics: true,
  
  // Restaurant features - enabled
  multiLocation: true,
  staffManagement: true,
  customMenuThemes: true,
};

class FeatureFlagService {
  private flags: FeatureFlags = DEFAULT_FLAGS;
  private isInitialized = false;

  /**
   * Initialize feature flags (could fetch from API)
   */
  async initialize(): Promise<void> {
    try {
      // In production, fetch from backend/config
      // const response = await fetch('/api/feature-flags');
      // const remoteFlags = await response.json();
      // this.flags = { ...DEFAULT_FLAGS, ...remoteFlags };
      
      // For now, use environment-based flags
      this.flags = {
        ...DEFAULT_FLAGS,
        voiceOrders: __DEV__, // Enable in development
        analytics: process.env.EXPO_PUBLIC_ENABLE_ANALYTICS === 'true',
      };
      
      this.isInitialized = true;
    } catch (error) {
      console.warn('Failed to load feature flags, using defaults:', error);
      this.flags = DEFAULT_FLAGS;
      this.isInitialized = true;
    }
  }

  /**
   * Check if a feature is enabled
   */
  isEnabled(feature: keyof FeatureFlags): boolean {
    if (!this.isInitialized) {
      // Return default flags before initialization
      return DEFAULT_FLAGS[feature];
    }
    return this.flags[feature];
  }

  /**
   * Get all current flags
   */
  getFlags(): FeatureFlags {
    return { ...this.flags };
  }

  /**
   * Enable a feature flag (for testing/admin)
   */
  enableFeature(feature: keyof FeatureFlags): void {
    this.flags[feature] = true;
  }

  /**
   * Disable a feature flag (for testing/admin)
   */
  disableFeature(feature: keyof FeatureFlags): void {
    this.flags[feature] = false;
  }

  /**
   * Check multiple features
   */
  areEnabled(features: Array<keyof FeatureFlags>): boolean[] {
    return features.map(feature => this.isEnabled(feature));
  }

  /**
   * Get enabled features list
   */
  getEnabledFeatures(): string[] {
    return Object.entries(this.flags)
      .filter(([, enabled]) => enabled)
      .map(([feature]) => feature);
  }
}

export const featureFlags = new FeatureFlagService();
export type { FeatureFlags };
export default featureFlags;