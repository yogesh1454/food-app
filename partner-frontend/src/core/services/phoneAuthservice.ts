import { auth } from '../config/firebase';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';

export const setupRecaptcha = () => {
  // Clear existing verifier first
  if (global.recaptchaVerifier) {
    try {
      global.recaptchaVerifier.clear();
    } catch (e) {
      console.log('Error clearing recaptcha:', e);
    }
  }
  
  global.recaptchaVerifier = new RecaptchaVerifier(
    auth,
    'recaptcha-container',
    {
      size: 'invisible',
    }
  );
};

export const sendOTP = async (phoneNumber: string) => {
  setupRecaptcha();
  const appVerifier = global.recaptchaVerifier;
  return signInWithPhoneNumber(auth, phoneNumber, appVerifier);
};

export const verifyOTP = async (confirmationResult: any, otpCode: string) => {
  return await confirmationResult.confirm(otpCode);
};

export const signOut = async () => {
  return await auth.signOut();
};

export const clearRecaptcha = () => {
  if (global.recaptchaVerifier) {
    try {
      global.recaptchaVerifier.clear();
      global.recaptchaVerifier = null;
    } catch (e) {
      console.log('Error clearing recaptcha:', e);
    }
  }
};