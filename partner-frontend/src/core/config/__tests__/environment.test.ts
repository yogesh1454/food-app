import config from '../environment';

describe('Environment Configuration', () => {
  const originalEnv = process.env.NODE_ENV;

  afterEach(() => {
    process.env.NODE_ENV = originalEnv;
  });

  it('should return development config by default', () => {
    delete process.env.NODE_ENV;
    const envConfig = config;
    
    expect(envConfig.apiUrl).toBe('http://localhost:8082/api/v1');
    expect(envConfig.enableLogging).toBe(true);
    expect(envConfig.enableMockData).toBe(false);
  });

  it('should return development config for development env', () => {
    process.env.NODE_ENV = 'development';
    const envConfig = config;
    
    expect(envConfig.apiUrl).toBe('http://localhost:8082/api/v1');
    expect(envConfig.enableLogging).toBe(true);
    expect(envConfig.enableMockData).toBe(false);
  });

  it('should return staging config for staging env', () => {
    process.env.NODE_ENV = 'staging';
    const envConfig = config;
    
    expect(envConfig.apiUrl).toBe('https://staging-api.nashtto.com/api/v1');
    expect(envConfig.enableLogging).toBe(true);
    expect(envConfig.enableMockData).toBe(false);
  });

  it('should return production config for production env', () => {
    process.env.NODE_ENV = 'production';
    const envConfig = config;
    
    expect(envConfig.apiUrl).toBe('https://api.nashtto.com/api/v1');
    expect(envConfig.enableLogging).toBe(false);
    expect(envConfig.enableMockData).toBe(false);
  });

  it('should have consistent timeout across environments', () => {
    const devConfig = require('../environment').config;
    process.env.NODE_ENV = 'staging';
    const stagingConfig = require('../environment').config;
    process.env.NODE_ENV = 'production';
    const prodConfig = require('../environment').config;
    
    expect(devConfig.apiTimeout).toBe(30000);
    expect(stagingConfig.apiTimeout).toBe(30000);
    expect(prodConfig.apiTimeout).toBe(30000);
  });
});