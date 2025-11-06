import { useState, useEffect } from 'react';
import featureFlags, { FeatureFlags } from '../services/featureFlags';

interface UseFeatureFlagsReturn {
  flags: FeatureFlags;
  isEnabled: (feature: keyof FeatureFlags) => boolean;
  areEnabled: (features: Array<keyof FeatureFlags>) => boolean[];
  enabledFeatures: string[];
  loading: boolean;
  error: string | null;
}

export const useFeatureFlags = (): UseFeatureFlagsReturn => {
  const [flags, setFlags] = useState<FeatureFlags>({} as FeatureFlags);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const initializeFlags = async () => {
      try {
        await featureFlags.initialize();
        setFlags(featureFlags.getFlags());
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    initializeFlags();
  }, []);

  const isEnabled = (feature: keyof FeatureFlags): boolean => {
    return featureFlags.isEnabled(feature);
  };

  const areEnabled = (features: Array<keyof FeatureFlags>): boolean[] => {
    return featureFlags.areEnabled(features);
  };

  const enabledFeatures = featureFlags.getEnabledFeatures();

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