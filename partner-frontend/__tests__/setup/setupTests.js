// Test setup file for React Native Testing Library
import 'react-native-gesture-handler/jestSetup';
import mockAsyncStorage from '@react-native-async-storage/async-storage/jest/async-storage-mock';

// Mock AsyncStorage
jest.mock('@react-native-async-storage/async-storage', () => mockAsyncStorage);

// Mock React Navigation
jest.mock('@react-navigation/native', () => {
  return {
    useNavigation: () => ({
      navigate: jest.fn(),
      goBack: jest.fn(),
      push: jest.fn(),
      replace: jest.fn(),
      reset: jest.fn(),
      setOptions: jest.fn(),
    }),
    useRoute: () => ({
      params: {},
    }),
  };
});

// Mock Bottom Tabs Navigation
jest.mock('@react-navigation/bottom-tabs', () => ({
  createBottomTabNavigator: jest.fn(),
  BottomTabNavigationProp: jest.fn(),
}));

// Mock Stack Navigation
jest.mock('@react-navigation/stack', () => ({
  createStackNavigator: jest.fn(),
  StackNavigationProp: jest.fn(),
}));

// Mock React Native components that might cause issues
jest.mock('react-native/Libraries/Animated/NativeAnimatedHelper');

// Mock Expo StatusBar
jest.mock('expo-status-bar', () => ({
  StatusBar: 'div', // Mock as div element for web
}));

// Mock Linear Gradient
jest.mock('expo-linear-gradient', () => ({
  LinearGradient: 'LinearGradient', // Mock as string
}));

// Mock Vector Icons
jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons', // Mock as string
}));

// Mock React Native Vector Icons
jest.mock('react-native-vector-icons/Ionicons', () => ({
  Ionicons: 'Ionicons', // Mock as string
}));

// Mock Image Picker
jest.mock('expo-image-picker', () => ({
  launchImageLibraryAsync: jest.fn(),
  launchCameraAsync: jest.fn(),
  requestCameraPermissionsAsync: jest.fn(),
  requestMediaLibraryPermissionsAsync: jest.fn(),
  MediaTypeOptions: {
    Images: 'Images',
    Videos: 'Videos',
    All: 'All',
  },
}));

// Mock Document Picker
jest.mock('expo-document-picker', () => ({
  getDocumentAsync: jest.fn(),
  DocumentPicker: {
    getDocumentAsync: jest.fn(),
  },
}));

// Mock Redux
jest.mock('react-redux', () => ({
  Provider: ({ children }) => children,
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));

// Global test setup
global.console = {
  ...console,
  // Suppress specific warnings during tests
  warn: jest.fn(),
  error: jest.fn(),
};

// Suppress React 18 warnings in tests
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: ReactDOM.render is no longer supported')
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

// Clean up after each test
afterEach(() => {
  jest.clearAllMocks();
});

// Setup test environment variables
process.env.NODE_ENV = 'test';