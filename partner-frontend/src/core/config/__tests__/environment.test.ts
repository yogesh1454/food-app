import config from '../environment';
import * as Env from '@env';

describe('Environment Configuration', () => {
  it('should map values from @env correctly', () => {
    expect(config.apiUrl).toBe(Env.API_URL);
    expect(config.apiTimeout).toBe(parseInt(Env.API_TIMEOUT, 10));
    expect(config.enableLogging).toBe(Env.ENABLE_LOGGING === 'true');
    expect(config.enableMockData).toBe(Env.ENABLE_MOCK_DATA === 'true');
    expect(config.imageUploadUrl).toBe(Env.IMAGE_UPLOAD_URL);
    expect(config.documentUploadUrl).toBe(Env.DOCUMENT_UPLOAD_URL);
  });
});