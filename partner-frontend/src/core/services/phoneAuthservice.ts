import { auth } from '../config/firebase';
import { signInWithPhoneNumber, ApplicationVerifier } from 'firebase/auth';

// We no longer need setupRecaptcha here as it will be handled by the UI component
// using FirebaseRecaptchaVerifierModal from expo-firebase-recaptcha

export const sendOTP = async (phoneNumber: string, appVerifier: ApplicationVerifier) => {
  try {
    const confirmationResult = await signInWithPhoneNumber(auth, phoneNumber, appVerifier);
    return confirmationResult;
  } catch (error) {
    console.error('Error sending OTP:', error);
    throw error;
  }
};

export const verifyOTP = async (confirmationResult: any, otpCode: string) => {
  try {
    return await confirmationResult.confirm(otpCode);
  } catch (error) {
    console.error('Error verifying OTP:', error);
    throw error;
  }
};

export const signOut = async () => {
  return await auth.signOut();
};