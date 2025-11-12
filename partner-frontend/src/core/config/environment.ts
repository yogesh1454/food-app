// Environment configuration for different deployment stages
export interface EnvironmentConfig {
  apiUrl: string;
  apiTimeout: number;
  enableLogging: boolean;
  enableMockData: boolean;
  imageUploadUrl: string;
  documentUploadUrl: string;
}

const development: EnvironmentConfig = {
  apiUrl: 'http://localhost:8082/api/v1',
  apiTimeout: 30000,
  enableLogging: true,
  enableMockData: false,
  imageUploadUrl: 'http://localhost:8082/api/v1/upload/image',
  documentUploadUrl: 'http://localhost:8082/api/v1/upload/document',
};

const staging: EnvironmentConfig = {
  apiUrl: 'https://staging-api.nashtto.com/api/v1',
  apiTimeout: 30000,
  enableLogging: true,
  enableMockData: false,
  imageUploadUrl: 'https://staging-api.nashtto.com/api/v1/upload/image',
  documentUploadUrl: 'https://staging-api.nashtto.com/api/v1/upload/document',
};

const production: EnvironmentConfig = {
  apiUrl: 'https://api.nashtto.com/api/v1',
  apiTimeout: 30000,
  enableLogging: false,
  enableMockData: false,
  imageUploadUrl: 'https://api.nashtto.com/api/v1/upload/image',
  documentUploadUrl: 'https://api.nashtto.com/api/v1/upload/document',
};

const getEnvironment = (): EnvironmentConfig => {
  // In a real app, this would be determined by build-time variables
  // For now, we'll use a simple check or default to development
  const env = process.env.NODE_ENV || 'development';
  
  switch (env) {
    case 'production':
      return production;
    case 'staging':
      return staging;
    case 'development':
    default:
      return development;
  }
};

export const config = getEnvironment();

export default config;