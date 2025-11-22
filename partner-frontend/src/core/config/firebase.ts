// src/core/config/firebase.ts
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

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
const auth = getAuth(app);

export { app, auth };