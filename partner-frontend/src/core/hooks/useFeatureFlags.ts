import { useState, useEffect } from 'react';
import { FeatureFlagService } from '../services/featureFlags';

interface UseFeatureFlagsReturn {
  flags: Record<string, boolean>;
  isEnabled: (feature: string) => boolean;
  areEnabled: (features: string[]) => boolean[];
  enabledFeatures: string[];
  loading: boolean;
  error: string | null;
}

export const useFeatureFlags = (): UseFeatureFlagsReturn => {
  const [flags, setFlags] = useState<Record<string, boolean>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const initializeFlags = async () => {
      try {
        // FeatureFlagService doesn't need initialization
        setFlags(FeatureFlagService.getAllFlags());
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    initializeFlags();
  }, []);

  const isEnabled = (feature: string): boolean => {
    return FeatureFlagService.isEnabled(feature);
  };

  const areEnabled = (features: string[]): boolean[] => {
    return features.map(feature => FeatureFlagService.isEnabled(feature));
  };

  const enabledFeatures = Object.entries(flags)
    .filter(([_, enabled]) => enabled)
    .map(([feature]) => feature);

  return {
    flags,
    isEnabled,
    areEnabled,
    enabledFeatures,
    loading,
    error,
  };
};

export default useFeatureFlags;