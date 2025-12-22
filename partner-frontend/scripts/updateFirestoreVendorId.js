/**
 * One-time script to update Firestore userMappings document with vendorId
 * Run with: node scripts/updateFirestoreVendorId.js
 */

const { initializeApp } = require('firebase/app');
const { getFirestore, doc, setDoc } = require('firebase/firestore');

// Firebase config from the project
const firebaseConfig = {
    apiKey: "AIzaSyDAOUROmucGAXYdS0BCFjnXMtE2KD_QzFo",
    authDomain: "food-app-1feee.firebaseapp.com",
    projectId: "food-app-1feee",
    storageBucket: "food-app-1feee.appspot.com",
    messagingSenderId: "YOUR_SENDER_ID",
    appId: "YOUR_APP_ID"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// Configuration - UPDATE THESE VALUES
const FIREBASE_UID = 'x0IudnlZY5cYTqUXEDqtJQobd3N2';
const VENDOR_ID = 6;

async function updateVendorId() {
    try {
        console.log(`Updating Firestore document for user: ${FIREBASE_UID}`);
        console.log(`Setting vendorId: ${VENDOR_ID}`);

        const docRef = doc(db, 'userMappings', FIREBASE_UID);
        await setDoc(docRef, {
            vendorId: VENDOR_ID,
            vendorIdSavedAt: new Date().toISOString(),
        }, { merge: true });

        console.log('✅ Successfully updated vendorId in Firestore!');
        console.log('You can now log in to the app and your vendor should load.');
        process.exit(0);
    } catch (error) {
        console.error('❌ Error updating Firestore:', error);
        process.exit(1);
    }
}

updateVendorId();
