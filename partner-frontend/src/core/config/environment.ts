// Environment configuration for different deployment stages
import {
  API_URL,
  API_TIMEOUT,
  ENABLE_LOGGING,
  ENABLE_MOCK_DATA,
  IMAGE_UPLOAD_URL,
  DOCUMENT_UPLOAD_URL,
} from '@env';

export interface EnvironmentConfig {
  apiUrl: string;
  apiTimeout: number;
  enableLogging: boolean;
  enableMockData: boolean;
  imageUploadUrl: string;
  documentUploadUrl: string;
}

const config: EnvironmentConfig = (() => {
  if (!API_URL) {
    throw new Error('Missing required environment variable: API_URL');
  }
  if (!IMAGE_UPLOAD_URL) {
    throw new Error('Missing required environment variable: IMAGE_UPLOAD_URL');
  }
  if (!DOCUMENT_UPLOAD_URL) {
    throw new Error('Missing required environment variable: DOCUMENT_UPLOAD_URL');
  }

  return {
    apiUrl: API_URL,
    apiTimeout: parseInt(API_TIMEOUT, 10) || 30000,
    enableLogging: ENABLE_LOGGING === 'true',
    enableMockData: ENABLE_MOCK_DATA === 'true',
    imageUploadUrl: IMAGE_UPLOAD_URL,
    documentUploadUrl: DOCUMENT_UPLOAD_URL,
  };
})();

export { config };
export default config;