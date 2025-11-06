import axios from 'axios';

// Optional: function to get token securely (mock for now)
async function getTokenFromStorage() {
  // Replace with your AsyncStorage or secure storage logic
  return null;
}

// Create axios instance
const apiClient = axios.create({
  baseURL: 'https://your-default-api-url.com', // You can override this later
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor (for attaching tokens, etc.)
apiClient.interceptors.request.use(
  async config => {
    const token = await getTokenFromStorage();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  error => Promise.reject(error)
);

// Response interceptor (for global error handling)
apiClient.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response) {
      console.error(
        `API Error [${error.response.status}]:`,
        error.response.data?.message || error.response.statusText
      );
    } else {
      console.error('Network or timeout error:', error.message);
    }
    return Promise.reject(error);
  }
);

// Generic HTTP client
const httpClient = {
  get: (url, params = {}, config = {}) =>
    apiClient.get(url, { params, ...config }),

  post: (url, data = {}, config = {}) =>
    apiClient.post(url, data, config),

  put: (url, data = {}, config = {}) =>
    apiClient.put(url, data, config),

  patch: (url, data = {}, config = {}) =>
    apiClient.patch(url, data, config),

  delete: (url, config = {}) =>
    apiClient.delete(url, config),

  // Optional: create client dynamically for other APIs
  createCustomClient: (baseURL) => {
    const customClient = axios.create({
      baseURL,
      timeout: 10000,
      headers: { 'Content-Type': 'application/json' },
    });
    return {
      get: (url, params = {}, config = {}) =>
        customClient.get(url, { params, ...config }),
      post: (url, data = {}, config = {}) =>
        customClient.post(url, data, config),
      put: (url, data = {}, config = {}) =>
        customClient.put(url, data, config),
      patch: (url, data = {}, config = {}) =>
        customClient.patch(url, data, config),
      delete: (url, config = {}) =>
        customClient.delete(url, config),
    };
  },
};

export default httpClient;
