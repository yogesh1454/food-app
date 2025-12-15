// Add global polyfills for Expo's Winter runtime
if (typeof global.structuredClone === 'undefined') {
    global.structuredClone = (obj) => JSON.parse(JSON.stringify(obj));
}

// Mock Expo modules and runtime features for Jest
global.__ExpoImportMetaRegistry = undefined;

// Mock expo-font
jest.mock('expo-font');

// Mock expo-asset
jest.mock('expo-asset', () => ({
    Asset: {
        fromModule: jest.fn(() => ({ uri: 'mocked-asset-uri' })),
        loadAsync: jest.fn(() => Promise.resolve()),
    },
}));

// Mock expo-linear-gradient
jest.mock('expo-linear-gradient', () => ({
    LinearGradient: 'LinearGradient',
}));

// Mock AsyncStorage
jest.mock('@react-native-async-storage/async-storage', () =>
    require('@react-native-async-storage/async-storage/jest/async-storage-mock')
);

// Mock expo-image-picker
jest.mock('expo-image-picker', () => ({
    launchImageLibraryAsync: jest.fn(),
    MediaTypeOptions: {
        Images: 'Images',
    },
}));

// Mock expo-document-picker
jest.mock('expo-document-picker', () => ({
    getDocumentAsync: jest.fn(),
}));

// Mock Firebase
jest.mock('firebase/app', () => ({
    initializeApp: jest.fn(),
    getApps: jest.fn(() => []),
    getApp: jest.fn(),
}));

jest.mock('firebase/auth', () => ({
    getAuth: jest.fn(),
    signInWithEmailAndPassword: jest.fn(),
    createUserWithEmailAndPassword: jest.fn(),
    signOut: jest.fn(),
    onAuthStateChanged: jest.fn(),
}));
