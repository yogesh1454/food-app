import { initializeApp } from "firebase/app";
// @ts-ignore - getReactNativePersistence exists at runtime but not in TS types for Firebase v12
import { initializeAuth, getReactNativePersistence, Auth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import AsyncStorage from '@react-native-async-storage/async-storage';

const firebaseConfig = {
  apiKey: "AIzaSyDAOUROmucGAXYdS0BCFjnXMtE2KD_QzFo",
  authDomain: "food-app-1feee.firebaseapp.com",
  projectId: "food-app-1feee",
  storageBucket: "food-app-1feee.firebasestorage.app",
  messagingSenderId: "1089443901948",
  appId: "1:1089443901948:web:eb455744a8f9e06f8f8279",
  measurementId: "G-P912GRBX58"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Auth with AsyncStorage persistence for React Native
let auth: Auth;
try {
  auth = initializeAuth(app, {
    persistence: getReactNativePersistence(AsyncStorage)
  });
} catch (error) {
  // Auth might already be initialized (hot reload scenario)
  const { getAuth } = require("firebase/auth");
  auth = getAuth(app);
}

const db = getFirestore(app);

export { app, auth, db, firebaseConfig };