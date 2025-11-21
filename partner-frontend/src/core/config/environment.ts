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

const config: EnvironmentConfig = {
  apiUrl: API_URL,
  apiTimeout: parseInt(API_TIMEOUT, 10) || 30000,
  enableLogging: ENABLE_LOGGING === 'true',
  enableMockData: ENABLE_MOCK_DATA === 'true',
  imageUploadUrl: IMAGE_UPLOAD_URL,
  documentUploadUrl: DOCUMENT_UPLOAD_URL,
};

export { config };
export default config;