export interface FeatureFlag {
  key: string;
  enabled: boolean;
  description?: string;
}

export class FeatureFlagService {
  private static flags: Record<string, boolean> = {
    // Add your feature flags here
    NEW_MENU_UI: false,
    ADVANCED_SEARCH: true,
    ORDER_TRACKING: false,
    PAYMENT_INTEGRATION: false,
  };

  static isEnabled(flagKey: string): boolean {
    return this.flags[flagKey] || false;
  }

  static setFlag(flagKey: string, enabled: boolean): void {
    this.flags[flagKey] = enabled;
  }

  static getAllFlags(): Record<string, boolean> {
    return { ...this.flags };
  }

  static getFlagInfo(): FeatureFlag[] {
    return Object.entries(this.flags).map(([key, enabled]) => ({
      key,
      enabled,
      description: this.getFlagDescription(key),
    }));
  }

  private static getFlagDescription(flagKey: string): string {
    const descriptions: Record<string, string> = {
      NEW_MENU_UI: 'Enable new menu user interface',
      ADVANCED_SEARCH: 'Enable advanced search functionality',
      ORDER_TRACKING: 'Enable real-time order tracking',
      PAYMENT_INTEGRATION: 'Enable payment gateway integration',
    };
    return descriptions[flagKey] || 'No description available';
  }
}

export default FeatureFlagService;