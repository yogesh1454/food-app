import { initializeApp } from "firebase/app";
import { getFirestore, doc, getDoc } from "firebase/firestore";

const firebaseConfig = {
    apiKey: "AIzaSyDAOUROmucGAXYdS0BCFjnXMtE2KD_QzFo",
    authDomain: "food-app-1feee.firebaseapp.com",
    projectId: "food-app-1feee",
    storageBucket: "food-app-1feee.firebasestorage.app",
    messagingSenderId: "1089443901948",
    appId: "1:1089443901948:web:eb455744a8f9e06f8f8279",
    measurementId: "G-P912GRBX58"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

async function checkVendor() {
    const uid = "IvcYWKHtW7TXcZ8voU6UzZXCa3l1";
    console.log(`Checking Firestore for vendor with UID: ${uid}`);

    try {
        const docRef = doc(db, "vendors", uid);
        const docSnap = await getDoc(docRef);

        if (docSnap.exists()) {
            console.log("Vendor found in Firestore:", docSnap.data());
        } else {
            console.log("No vendor found in Firestore for this UID.");
        }
    } catch (error) {
        console.error("Error checking Firestore:", error);
    }
}

checkVendor();
