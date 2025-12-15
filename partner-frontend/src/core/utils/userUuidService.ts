/**
 * User UUID Service
 * 
 * Persists the user's UUID in Firestore so it survives app reinstalls
 * and AsyncStorage clears. This UUID is used to authenticate with the
 * backend and maintain the user-vendor association.
 */

import { doc, getDoc, setDoc } from 'firebase/firestore';
import { db, auth } from '../config/firebase';
import AsyncStorage from '@react-native-async-storage/async-storage';

const USER_UUID_COLLECTION = 'userMappings';

/**
 * Generate a new UUID v4
 */
function generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

/**
 * Get or create the user's UUID.
 * 
 * Priority order:
 * 1. Firestore (source of truth)
 * 2. AsyncStorage (local cache)
 * 3. Generate new and save to both
 * 
 * @returns The user's backend UUID
 * @throws Error if user is not authenticated
 */
export async function getUserUUID(): Promise<string> {
    const firebaseUid = auth.currentUser?.uid;

    if (!firebaseUid) {
        throw new Error('User not authenticated. Please sign in first.');
    }

    console.log('[UserUUID] Getting UUID for Firebase user:', firebaseUid);

    // Try to get from Firestore first (source of truth)
    try {
        const docRef = doc(db, USER_UUID_COLLECTION, firebaseUid);
        const docSnap = await getDoc(docRef);

        if (docSnap.exists()) {
            const storedUUID = docSnap.data()?.uuid;
            console.log('[UserUUID] Found UUID in Firestore:', storedUUID);

            // Also update AsyncStorage as local cache
            await AsyncStorage.setItem(`user_uuid_${firebaseUid}`, storedUUID);

            return storedUUID;
        }
    } catch (error) {
        console.warn('[UserUUID] Error reading from Firestore:', error);
        // Fall through to check AsyncStorage
    }

    // Try AsyncStorage as fallback (for users who registered before Firestore migration)
    const asyncStorageKey = `user_uuid_${firebaseUid}`;
    const cachedUUID = await AsyncStorage.getItem(asyncStorageKey);

    if (cachedUUID) {
        console.log('[UserUUID] Found UUID in AsyncStorage, migrating to Firestore:', cachedUUID);

        // Migrate to Firestore
        try {
            const docRef = doc(db, USER_UUID_COLLECTION, firebaseUid);
            await setDoc(docRef, {
                uuid: cachedUUID,
                firebaseUid: firebaseUid,
                createdAt: new Date().toISOString(),
                migratedFromAsyncStorage: true,
            });
            console.log('[UserUUID] Successfully migrated UUID to Firestore');
        } catch (error) {
            console.warn('[UserUUID] Failed to migrate UUID to Firestore:', error);
        }

        return cachedUUID;
    }

    // No UUID found anywhere, generate a new one
    const newUUID = generateUUID();
    console.log('[UserUUID] Generated new UUID:', newUUID);

    // Save to Firestore (primary storage)
    try {
        const docRef = doc(db, USER_UUID_COLLECTION, firebaseUid);
        await setDoc(docRef, {
            uuid: newUUID,
            firebaseUid: firebaseUid,
            createdAt: new Date().toISOString(),
        });
        console.log('[UserUUID] Saved UUID to Firestore');
    } catch (error) {
        console.error('[UserUUID] Failed to save UUID to Firestore:', error);
        // Continue anyway - save to AsyncStorage as fallback
    }

    // Also save to AsyncStorage as local cache
    await AsyncStorage.setItem(asyncStorageKey, newUUID);
    console.log('[UserUUID] Saved UUID to AsyncStorage');

    return newUUID;
}

/**
 * Check if the current user has a UUID stored
 */
export async function hasUserUUID(): Promise<boolean> {
    const firebaseUid = auth.currentUser?.uid;

    if (!firebaseUid) {
        return false;
    }

    try {
        const docRef = doc(db, USER_UUID_COLLECTION, firebaseUid);
        const docSnap = await getDoc(docRef);
        return docSnap.exists();
    } catch (error) {
        console.warn('[UserUUID] Error checking Firestore:', error);

        // Fallback to AsyncStorage
        const cachedUUID = await AsyncStorage.getItem(`user_uuid_${firebaseUid}`);
        return !!cachedUUID;
    }
}

/**
 * Clear the user's UUID (for logout/reset scenarios)
 */
export async function clearUserUUID(): Promise<void> {
    const firebaseUid = auth.currentUser?.uid;

    if (!firebaseUid) {
        return;
    }

    console.log('[UserUUID] Clearing UUID for user:', firebaseUid);

    // Clear from AsyncStorage
    await AsyncStorage.removeItem(`user_uuid_${firebaseUid}`);

    // Note: We intentionally DON'T delete from Firestore
    // This allows the user to recover their vendor association after re-login
    console.log('[UserUUID] Cleared local UUID cache (Firestore retained for recovery)');
}

export default {
    getUserUUID,
    hasUserUUID,
    clearUserUUID,
};
