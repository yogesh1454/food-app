import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Alert,
  Modal,
  FlatList,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useDispatch } from 'react-redux';
import { RootStackParamList } from '../navigation/AppNavigator';
import { useAppDispatch } from '../store';
import { setRestaurant, registerVendor, createBranch, uploadVendorMedia } from '../store/slices/restaurantSlice';
import { createMenuItem } from '../store/slices/menuSlice';
import { setFirstTime } from '../store/slices/authSlice';
import { vendorApiService } from '../core/api/vendorApiService';
import { commonStyles } from '../core/styles/commonStyles';
import { colors } from '../core/constants/colors';
import ImageUploadButton from '../core/components/ImageUploadButton';
import DocumentUploadButton from '../core/components/DocumentUploadButton';
import { sendOTP, verifyOTP } from '../core/services/phoneAuthservice';
import { sendEmailVerification, updatePassword, linkWithCredential, EmailAuthProvider } from 'firebase/auth';
import { auth } from '../core/config/firebase';
import { FirebaseRecaptchaVerifierModal } from 'expo-firebase-recaptcha';
import { useRef } from 'react';

type OnboardingScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Onboarding'>;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  gradient: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    ...commonStyles.p6,
    ...commonStyles.px6,
  },
  header: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
    ...commonStyles.my4,
  },
  skipText: {
    color: 'rgba(255, 255, 255, 0.8)',
    ...commonStyles.textBase,
    ...commonStyles.fontMedium,
  },
  indicators: {
    ...commonStyles.row,
    gap: 12,
    ...commonStyles.itemsCenter,
  },
  indicator: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
  },
  indicatorActive: {
    backgroundColor: 'white',
    width: 20,
  },
  pageContent: {
    flex: 1,
  },
  title: {
    ...commonStyles.text3xl,
    ...commonStyles.fontBold,
    color: 'white',
    ...commonStyles.textCenter,
    ...commonStyles.my4,
    lineHeight: 36,
  },
  subtitle: {
    ...commonStyles.textBase,
    color: 'rgba(255, 255, 255, 0.9)',
    ...commonStyles.textCenter,
    ...commonStyles.my4,
    lineHeight: 24,
  },
  section: {
    ...commonStyles.my4,
  },
  sectionTitle: {
    ...commonStyles.textLg,
    ...commonStyles.fontSemibold,
    color: 'white',
    ...commonStyles.my2,
  },
  input: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.my2,
    ...commonStyles.textBase,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    color: 'white',
  },
  rowInputs: {
    flexDirection: 'row',
    gap: 12,
  },
  halfInput: {
    flex: 1,
  },
  inputFocused: {
    borderColor: 'white',
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
  },
  textArea: {
    height: 120,
    textAlignVertical: 'top',
  },
  inputField: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.textBase,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    color: 'white',
  },
  dropdown: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.my2,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
  },
  dropdownText: {
    ...commonStyles.textBase,
    color: 'white',
    ...commonStyles.fontMedium,
  },
  dropdownPlaceholder: {
    ...commonStyles.textBase,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  timeButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.my2,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
  },
  timeText: {
    ...commonStyles.textBase,
    color: 'white',
    ...commonStyles.fontMedium,
  },
  timePlaceholder: {
    ...commonStyles.textBase,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  uploadButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    ...commonStyles.my2,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    borderStyle: 'dashed',
  },
  uploadText: {
    color: 'white',
    ...commonStyles.textBase,
    ...commonStyles.fontMedium,
  },
  extractedItems: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    ...commonStyles.roundedLg,
    ...commonStyles.p4,
    ...commonStyles.my3,
  },
  item: {
    ...commonStyles.py3,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.1)',
  },
  itemText: {
    ...commonStyles.textBase,
    color: 'white',
    ...commonStyles.fontMedium,
  },
  navigation: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.my4,
    gap: 16,
  },
  button: {
    backgroundColor: 'white',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
    ...commonStyles.shadowLg,
  },
  buttonText: {
    color: '#16a34a',
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
  },
  buttonDisabled: {
    backgroundColor: 'rgba(255, 255, 255, 0.5)',
    shadowOpacity: 0.05,
  },
  buttonTextDisabled: {
    color: 'rgba(22, 163, 74, 0.5)',
  },
  otpButton: {
    backgroundColor: 'white',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    ...commonStyles.shadowLg,
  },
  primaryButton: {
    backgroundColor: 'white',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
    ...commonStyles.shadowLg,
  },
  primaryButtonText: {
    color: '#16a34a',
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
  },
  primaryButtonDisabled: {
    backgroundColor: 'rgba(255, 255, 255, 0.5)',
    shadowOpacity: 0.05,
  },
  primaryButtonTextDisabled: {
    color: 'rgba(22, 163, 74, 0.5)',
  },
  secondaryButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderWidth: 1,
    borderColor: 'white',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
  },
  secondaryButtonText: {
    color: 'white',
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
  },
  secondaryButtonDisabled: {
    borderColor: 'rgba(255, 255, 255, 0.3)',
    backgroundColor: 'transparent',
  },
  secondaryButtonTextDisabled: {
    color: 'rgba(255, 255, 255, 0.3)',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    ...commonStyles.justifyCenter,
    ...commonStyles.itemsCenter,
  },
  modalContent: {
    ...commonStyles.bgWhite,
    ...commonStyles.roundedXl,
    ...commonStyles.p6,
    width: '85%',
    maxHeight: '70%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.25,
    shadowRadius: 20,
    elevation: 10,
  },
  modalTitle: {
    ...commonStyles.text2xl,
    ...commonStyles.fontBold,
    color: colors.text,
    ...commonStyles.my4,
    ...commonStyles.textCenter,
  },
  otpInput: {
    backgroundColor: colors.background,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.textLg,
    ...commonStyles.textCenter,
    ...commonStyles.my4,
    borderWidth: 1,
    borderColor: colors.border,
    ...commonStyles.fontSemibold,
    color: colors.text,
  },
  menuOption: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.my3,
    gap: 12,
  },
  menuOptionButton: {
    ...commonStyles.px4,
    ...commonStyles.py3,
    ...commonStyles.roundedLg,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    flex: 1,
    ...commonStyles.itemsCenter,
  },
  menuOptionButtonActive: {
    borderColor: 'white',
    backgroundColor: 'rgba(255, 255, 255, 0.25)',
  },
  menuOptionText: {
    color: 'rgba(255, 255, 255, 0.7)',
    ...commonStyles.textSm,
    ...commonStyles.fontMedium,
  },
  menuOptionTextActive: {
    color: 'white',
    ...commonStyles.fontSemibold,
  },
  inputContainer: {
    ...commonStyles.row,
    ...commonStyles.itemsCenter,
    ...commonStyles.my4,
    gap: 12,
  },
  verifyButton: {
    backgroundColor: 'white',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.shadow,
  },
  verifiedButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderWidth: 1,
    borderColor: 'white',
  },
  verifyText: {
    color: '#16a34a',
    ...commonStyles.textSm,
    ...commonStyles.fontSemibold,
  },
  verifiedText: {
    color: 'white',
  },
  headerTitle: {
    ...commonStyles.text2xl,
    ...commonStyles.fontBold,
    color: 'white',
    ...commonStyles.textCenter,
  },
  dropdownItem: {
    ...commonStyles.px6,
    ...commonStyles.py4,
    borderBottomWidth: 1,
    borderBottomColor: colors.borderLight,
  },
  dropdownItemText: {
    ...commonStyles.textLg,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  timeItem: {
    ...commonStyles.px6,
    ...commonStyles.py4,
    borderBottomWidth: 1,
    borderBottomColor: colors.borderLight,
  },
  timeItemText: {
    ...commonStyles.textLg,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  progressContainer: {
    ...commonStyles.itemsCenter,
    ...commonStyles.my4,
  },
  progressText: {
    color: 'rgba(255, 255, 255, 0.8)',
    ...commonStyles.textSm,
    ...commonStyles.my1,
  },
  modalHeader: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
    ...commonStyles.px6,
    ...commonStyles.py4,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    ...commonStyles.bgWhite,
  },
  modalHeaderTitle: {
    ...commonStyles.textLg,
    ...commonStyles.fontSemibold,
    color: colors.text,
  },
});

interface DocumentUpload {
  uri: string;
  type: 'gst' | 'fssai';
  documentNumber: string;
}

interface OnboardingData {
  name: string;
  cuisineType: string;
  address: string;
  openingTime: string;
  closingTime: string;
  phone: string;
  email: string;
  gstNumber: string;
  fssaiNumber: string;
  licenseDocuments: DocumentUpload[];
  logoUrl: string;
  coverPhotoUrl: string;
  menuText: string;
  extractedItems: string[];
  panNumber: string;
  legalEntityName: string;
  phoneVerified: boolean;
  emailVerified: boolean;
  password?: string;
  confirmPassword?: string;
  // Branch details
  branchCity: string;
  branchState: string;
  branchPincode: string;
  branchArea: string;
}

const cuisines = [
  'Italian', 'Chinese', 'Indian', 'Mexican', 'Japanese', 'Thai', 'French', 'Mediterranean', 'American', 'Other'
];

export default function OnboardingScreen() {
  const [confirmation, setConfirmation] = useState<any>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [cuisineDropdownVisible, setCuisineDropdownVisible] = useState(false);
  const [timePickerVisible, setTimePickerVisible] = useState(false);
  const [timeType, setTimeType] = useState<'opening' | 'closing'>('opening');
  const [otpModalVisible, setOtpModalVisible] = useState(false);
  const [otpType, setOtpType] = useState<'phone' | 'email'>('phone');
  const [otp, setOtp] = useState('');
  const [menuOption, setMenuOption] = useState<'photo' | 'pdf' | 'manual'>('manual');
  const [verificationId, setVerificationId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [data, setData] = useState<OnboardingData>({
    name: '',
    cuisineType: '',
    address: '',
    openingTime: '',
    closingTime: '',
    phone: '',
    email: '',
    gstNumber: '',
    fssaiNumber: '',
    licenseDocuments: [],
    logoUrl: '',
    coverPhotoUrl: '',
    menuText: '',
    extractedItems: [],
    panNumber: '',
    legalEntityName: '',
    phoneVerified: false,
    emailVerified: false,
    password: '',
    confirmPassword: '',
    // Branch details
    branchCity: '',
    branchState: '',
    branchPincode: '',
    branchArea: '',
  });
  const navigation = useNavigation<OnboardingScreenNavigationProp>();
  const dispatch = useAppDispatch();
  const recaptchaVerifier = useRef(null);

  const pages = [
    { title: 'Restaurant Details' },
    { title: 'Contact Information' },
    { title: 'License & Documents' },
    { title: 'Photos' },
    { title: 'Menu Upload' },
  ];

  const handleNext = async () => {
    if (currentPage === 1) {
      // Validation for Contact Information Page
      if (!data.phoneVerified) {
        Alert.alert('Error', 'Please verify your phone number first.');
        return;
      }
      if (!data.email || !data.email.includes('@')) {
        Alert.alert('Error', 'Please enter a valid email address.');
        return;
      }
      if (!data.password || data.password.length < 6) {
        Alert.alert('Error', 'Please set a password (min 6 chars).');
        return;
      }
      if (data.password !== data.confirmPassword) {
        Alert.alert('Error', 'Passwords do not match.');
        return;
      }

      // Link Email and Set Password
      try {
        const user = auth.currentUser;
        if (user) {
          // Check if email is already linked or set
          if (user.email !== data.email) {
            // Create a credential with the email and password
            const credential = EmailAuthProvider.credential(data.email, data.password);

            // Link this credential to the current user
            // This adds the email/password provider to the account AND sets the email address on the user object
            await linkWithCredential(user, credential);

            // Send verification email
            await sendEmailVerification(user);
            Alert.alert('Success', 'Account linked! A verification email has been sent.');
          } else {
            // If email is already set (maybe they came back), just update password if needed
            await updatePassword(user, data.password);
          }
        }
      } catch (error: any) {
        console.error('Failed to setup account:', error);
        if (error.code === 'auth/email-already-in-use') {
          Alert.alert('Error', 'This email is already in use by another account.');
        } else if (error.code === 'auth/credential-already-in-use') {
          Alert.alert('Error', 'This email is already linked to another account.');
        } else if (error.code === 'auth/requires-recent-login') {
          Alert.alert('Error', 'Security update required. Please sign in again.');
        } else if (error.code === 'auth/provider-already-linked') {
          // If already linked, maybe just update password?
          try {
            if (auth.currentUser) {
              await updatePassword(auth.currentUser, data.password);
              Alert.alert('Success', 'Password updated.');
            }
          } catch (pwError: any) {
            Alert.alert('Error', 'Failed to update password: ' + pwError.message);
          }
        } else {
          Alert.alert('Error', 'Failed to setup account. ' + error.message);
        }
        return;
      }
    }

    if (currentPage < pages.length - 1) {
      setCurrentPage(currentPage + 1);
    } else {
      // Last page - complete onboarding
      if (isSubmitting) {
        console.log('[Onboarding] Already submitting, ignoring duplicate click');
        return;
      }
      setIsSubmitting(true);
      try {
        await handleComplete();
      } catch (error: any) {
        console.error('[Onboarding] handleComplete failed:', error);
        Alert.alert('Error', error?.message || 'Failed to complete onboarding. Please try again.');
      } finally {
        setIsSubmitting(false);
      }
    }
  };

  const handlePrevious = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleComplete = async () => {
    // Validate PAN Format
    const panRegex = /^[A-Z]{5}[0-9]{4}[A-Z]{1}$/;
    if (data.panNumber && !panRegex.test(data.panNumber)) {
      Alert.alert('Invalid Input', 'Please enter a valid PAN Number (e.g., ABCDE1234F)');
      return;
    }

    // Validate GST Format
    const gstRegex = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;
    if (data.gstNumber && !gstRegex.test(data.gstNumber)) {
      Alert.alert('Invalid Input', 'Please enter a valid GST Number (e.g., 29ABCDE1234F1Z5)');
      return;
    }

    try {
      let vendorId: number;

      // 1. Try to register vendor OR handle if already exists
      try {
        console.log('[Onboarding] Attempting to register vendor...');
        const vendorResult = await dispatch(registerVendor({
          companyName: data.name,
          brandName: data.name,
          legalEntityName: data.legalEntityName || undefined,
          companyEmail: data.email,
          companyPhone: data.phone,
          panNumber: data.panNumber || undefined,
          gstNumber: data.gstNumber || undefined,
        })).unwrap();

        console.log('[Onboarding] Vendor result:', JSON.stringify(vendorResult, null, 2));
        vendorId = vendorResult.vendorId;

        if (!vendorId) {
          console.error('[Onboarding] vendorId not found in response, trying to extract...');
          // Try alternate field names (use any to bypass type checking for unknown fields)
          vendorId = (vendorResult as any).id || (vendorResult as any).vendor_id;
        }

        console.log('[Onboarding] Vendor created with ID:', vendorId);

        // Save vendorId to AsyncStorage immediately
        if (vendorId) {
          await AsyncStorage.setItem('vendorId', vendorId.toString());
        } else {
          throw new Error('Vendor created but vendorId not found in response');
        }

      } catch (vendorError: any) {
        // Check if vendor already exists (409 error)
        // Redux Toolkit unwrap() throws serialized error, check message for 409 indicators
        const errorMessage = vendorError?.message || JSON.stringify(vendorError) || '';
        const is409Error = errorMessage.includes('409') ||
          errorMessage.includes('already') ||
          errorMessage.includes('Conflict') ||
          errorMessage.includes('User already has a vendor');

        console.log('[Onboarding] Vendor error:', errorMessage, 'is409:', is409Error);

        if (is409Error) {
          console.log('[Onboarding] Vendor already exists, continuing with existing vendor...');

          // Try to get vendorId from AsyncStorage
          const storedVendorId = await AsyncStorage.getItem('vendorId');
          if (storedVendorId) {
            vendorId = parseInt(storedVendorId, 10);
            console.log('[Onboarding] Using stored vendorId:', vendorId);
          } else {
            // Fallback to vendorId 1 for dev (or could try to fetch from API)
            vendorId = 1;
            await AsyncStorage.setItem('vendorId', '1');
            console.warn('[Onboarding] Could not find stored vendorId, using default: 1');
          }
        } else {
          // Re-throw if it's a different error
          throw vendorError;
        }
      }

      console.log('[Onboarding] Proceeding to branch creation with vendorId:', vendorId);

      // 2. Create Branch (always attempt, will fail if already exists)
      let branchId: number;
      try {
        const branchResult = await dispatch(createBranch({
          vendorId: vendorId,
          branchData: {
            branchName: data.name || 'Main Branch',
            branchPhone: data.phone,
            branchEmail: data.email,
            city: data.branchCity,
            address: {
              street: data.address || 'Street not provided',
              area: data.branchArea || '',
              city: data.branchCity,
              state: data.branchState,
              pincode: data.branchPincode,
            },
            latitude: 0, // Can be updated via profile later
            longitude: 0,
          }
        })).unwrap();

        branchId = branchResult.branchId;
        console.log('[Onboarding] Branch created with ID:', branchId);

        // Save branchId to AsyncStorage
        await AsyncStorage.setItem('branchId', branchId.toString());

      } catch (branchError: any) {
        const branchErrorStatus = branchError?.response?.status || branchError?.status;

        if (branchErrorStatus === 409) {
          // Branch already exists, try to get from storage
          const storedBranchId = await AsyncStorage.getItem('branchId');
          if (storedBranchId) {
            branchId = parseInt(storedBranchId, 10);
            console.log('[Onboarding] Using stored branchId:', branchId);
          } else {
            branchId = 1; // Default fallback
            console.warn('[Onboarding] Could not find stored branchId, using default: 1');
          }
        } else {
          console.error('[Onboarding] Branch creation failed:', branchError);
          throw branchError;
        }
      }

      console.log('[Onboarding] Branch created/found, branchId:', branchId);

      // CRITICAL: Activate the branch so vendor is visible on sign-in
      try {
        console.log('[Onboarding] Activating branch to make vendor visible...');
        await vendorApiService.activateBranch(branchId);
        console.log('[Onboarding] Branch activated successfully');
      } catch (activateError: any) {
        console.error('[Onboarding] Failed to activate branch:', activateError);
        // Non-blocking - continue with onboarding
      }

      // Also toggle the branch to open so it accepts orders
      try {
        console.log('[Onboarding] Setting branch to OPEN...');
        await vendorApiService.toggleBranchStatus(branchId, { isOpen: true });
        console.log('[Onboarding] Branch set to OPEN');
      } catch (toggleError: any) {
        console.error('[Onboarding] Failed to toggle branch open:', toggleError);
        // Non-blocking - continue with onboarding
      }

      console.log('[Onboarding] Now running optional steps (non-blocking)...');

      // 3-5. Run optional uploads in background (don't block navigation)
      // Using Promise.allSettled to ensure we continue even if uploads fail
      const optionalUploads: Promise<any>[] = [];

      // 3. Upload Media (Logo) - optional
      if (data.logoUrl) {
        optionalUploads.push(
          dispatch(uploadVendorMedia({
            vendorId: vendorId,
            file: { uri: data.logoUrl, name: 'logo.jpg', type: 'image/jpeg' },
            target: 'VENDOR',
            fileType: 'logo'
          })).catch(err => console.warn('[Onboarding] Logo upload failed:', err))
        );
      }

      // 4. Upload Media (Cover) - optional
      if (data.coverPhotoUrl) {
        optionalUploads.push(
          dispatch(uploadVendorMedia({
            vendorId: vendorId,
            file: { uri: data.coverPhotoUrl, name: 'cover.jpg', type: 'image/jpeg' },
            target: 'VENDOR',
            fileType: 'cover'
          })).catch(err => console.warn('[Onboarding] Cover upload failed:', err))
        );
      }

      // 5. Upload Documents (GST and FSSAI) - optional
      for (const doc of data.licenseDocuments) {
        optionalUploads.push(
          dispatch(uploadVendorMedia({
            vendorId: vendorId,
            file: { uri: doc.uri, name: `${doc.type}_document.jpg`, type: 'image/jpeg' },
            target: 'branch',
            fileType: doc.type,
            branchId: branchId,
            additionalData: { documentNumber: doc.documentNumber }
          })).catch(err => console.warn(`[Onboarding] Document upload failed for ${doc.type}:`, err))
        );
      }

      // 6. Create Menu Items - optional
      if (data.extractedItems.length > 0) {
        for (const itemName of data.extractedItems) {
          optionalUploads.push(
            dispatch(createMenuItem({
              branchId: branchId,
              menuItemData: {
                name: itemName,
                price: 0,
                category: 'Main Course',
                preparationTimeMinutes: 15,
              }
            })).catch(err => console.warn(`[Onboarding] Menu item creation failed for ${itemName}:`, err))
          );
        }
      }

      // Wait for optional uploads with 10 second timeout, then navigate
      if (optionalUploads.length > 0) {
        console.log(`[Onboarding] Waiting up to 10s for ${optionalUploads.length} optional uploads...`);
        await Promise.race([
          Promise.allSettled(optionalUploads),
          new Promise(resolve => setTimeout(resolve, 10000))
        ]);
        console.log('[Onboarding] Optional uploads complete or timed out');
      }

      console.log('[Onboarding] SUCCESS! Navigating to Main screen...');
      dispatch(setFirstTime(false));
      // Navigate directly to Main, skipping PostOnboarding loading screen
      navigation.reset({
        index: 0,
        routes: [{ name: 'Main' }],
      });

    } catch (error: any) {
      console.error('Onboarding failed:', error);
      Alert.alert('Registration Failed', error.message || 'Please try again.');
    }
  };

  const handleSkip = () => {
    navigation.reset({
      index: 0,
      routes: [{ name: 'Main' }],
    });
  };

  const extractMenuItems = () => {
    // Mock extraction: split by lines and filter non-empty
    const items = data.menuText.split('\n').filter(item => item.trim() !== '');
    setData({ ...data, extractedItems: items });
  };

  const handleSendOTP = async () => {
    if (!data.phone || data.phone.length < 10) {
      Alert.alert('Error', 'Please enter a valid phone number');
      return;
    }
    try {
      // @ts-ignore
      const confirmation = await sendOTP(`+91${data.phone}`, recaptchaVerifier.current);
      setConfirmation(confirmation);
      setVerificationId(confirmation.verificationId);
      setOtpModalVisible(true);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const handleOtpSubmit = async () => {
    if (!otp || otp.length !== 6) {
      Alert.alert('Error', 'Please enter a valid 6-digit OTP');
      return;
    }

    // For phone verification with Firebase
    if (otpType === 'phone' && confirmation) {
      try {
        const userCredential = await verifyOTP(confirmation, otp);
        setData({ ...data, phoneVerified: true });
        setOtpModalVisible(false);
        setOtp('');
        setConfirmation(null);
        // clearRecaptcha(); // Not needed with modal
        Alert.alert('Success', 'Phone verified successfully!');
        console.log('User:', userCredential.user);
      } catch (error: any) {
        console.error(error);
        Alert.alert('Error', 'Invalid OTP. Please try again.');
      }
    }
  };

  const handleTimeSelect = (time: string) => {
    if (timeType === 'opening') {
      setData({ ...data, openingTime: time });
    } else {
      setData({ ...data, closingTime: time });
    }
    setTimePickerVisible(false);
  };

  const renderPage = () => {
    switch (currentPage) {
      case 0:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Basic Information</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your restaurant's full name"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.name}
              onChangeText={(text) => setData({ ...data, name: text })}
              accessibilityLabel="Restaurant Name Input"
            />
            <TouchableOpacity
              style={styles.dropdown}
              onPress={() => setCuisineDropdownVisible(true)}
              accessibilityLabel="Select Cuisine Type"
            >
              <Text style={data.cuisineType ? styles.dropdownText : styles.dropdownPlaceholder}>
                {data.cuisineType || 'Select Cuisine Type'}
              </Text>
              <Ionicons name="chevron-down" size={20} color="rgba(255, 255, 255, 0.6)" />
            </TouchableOpacity>
            <TextInput
              style={styles.input}
              placeholder="Enter complete restaurant address with landmark"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.address}
              onChangeText={(text) => setData({ ...data, address: text })}
              accessibilityLabel="Restaurant Address Input"
            />
            <Text style={styles.sectionTitle}>Branch Location</Text>
            <TextInput
              style={styles.input}
              placeholder="City (e.g., Bangalore)"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.branchCity}
              onChangeText={(text) => setData({ ...data, branchCity: text })}
              accessibilityLabel="City Input"
            />
            <View style={styles.rowInputs}>
              <TextInput
                style={[styles.input, styles.halfInput]}
                placeholder="State"
                placeholderTextColor="rgba(255, 255, 255, 0.6)"
                value={data.branchState}
                onChangeText={(text) => setData({ ...data, branchState: text })}
                accessibilityLabel="State Input"
              />
              <TextInput
                style={[styles.input, styles.halfInput]}
                placeholder="Pincode"
                placeholderTextColor="rgba(255, 255, 255, 0.6)"
                value={data.branchPincode}
                onChangeText={(text) => setData({ ...data, branchPincode: text })}
                keyboardType="numeric"
                maxLength={6}
                accessibilityLabel="Pincode Input"
              />
            </View>
            <TextInput
              style={styles.input}
              placeholder="Area/Locality (optional)"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.branchArea}
              onChangeText={(text) => setData({ ...data, branchArea: text })}
              accessibilityLabel="Area Input"
            />
            <Text style={styles.sectionTitle}>Operating Hours</Text>
            <TouchableOpacity
              style={styles.timeButton}
              onPress={() => {
                setTimeType('opening');
                setTimePickerVisible(true);
              }}
              accessibilityLabel="Select Opening Time"
            >
              <Text style={data.openingTime ? styles.timeText : styles.timePlaceholder}>
                {data.openingTime || 'Select Opening Time'}
              </Text>
              <Ionicons name="time" size={20} color="rgba(255, 255, 255, 0.6)" />
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.timeButton}
              onPress={() => {
                setTimeType('closing');
                setTimePickerVisible(true);
              }}
              accessibilityLabel="Select Closing Time"
            >
              <Text style={data.closingTime ? styles.timeText : styles.timePlaceholder}>
                {data.closingTime || 'Select Closing Time'}
              </Text>
              <Ionicons name="time" size={20} color="rgba(255, 255, 255, 0.6)" />
            </TouchableOpacity>
          </View>
        );
      case 1:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Contact Information</Text>
            <Text style={styles.subtitle}>We'll verify these details for security</Text>
            <View style={styles.inputContainer}>
              <TextInput
                style={styles.inputField}
                placeholder="Phone Number"
                placeholderTextColor="rgba(255, 255, 255, 0.6)"
                value={data.phone}
                onChangeText={(text) => setData({ ...data, phone: text })}
                keyboardType="phone-pad"
                accessibilityLabel="Phone Number Input"
              />
              <TouchableOpacity
                style={[styles.verifyButton, data.phoneVerified && styles.verifiedButton]}
                onPress={handleSendOTP}
                disabled={data.phoneVerified}
                accessibilityLabel="Verify Phone Number"
              >
                <Text style={[styles.verifyText, data.phoneVerified && styles.verifiedText]}>
                  {data.phoneVerified ? '✓' : 'Verify'}
                </Text>
              </TouchableOpacity>
            </View>
            <View style={styles.inputContainer}>
              <TextInput
                style={styles.inputField}
                placeholder="Email Address"
                placeholderTextColor="rgba(255, 255, 255, 0.6)"
                value={data.email}
                onChangeText={(text) => setData({ ...data, email: text })}
                keyboardType="email-address"
                accessibilityLabel="Email Address Input"
              />
            </View>

            {/* Password Fields - Only show after Phone Verification (Account Created) */}
            {data.phoneVerified && (
              <>
                <Text style={[styles.sectionTitle, { marginTop: 16 }]}>Create Password</Text>
                <Text style={styles.subtitle}>Set a password to login with email later</Text>
                <TextInput
                  style={styles.input}
                  placeholder="Password"
                  placeholderTextColor="rgba(255, 255, 255, 0.6)"
                  value={data.password}
                  onChangeText={(text) => setData({ ...data, password: text })}
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  accessibilityLabel="Password Input"
                />
                <TextInput
                  style={styles.input}
                  placeholder="Confirm Password"
                  placeholderTextColor="rgba(255, 255, 255, 0.6)"
                  value={data.confirmPassword}
                  onChangeText={(text) => setData({ ...data, confirmPassword: text })}
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  accessibilityLabel="Confirm Password Input"
                />
              </>
            )}
          </View>
        );
      case 2:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>License Information</Text>
            <Text style={styles.subtitle}>Enter your business registration details</Text>
            <TextInput
              style={styles.input}
              placeholder="Legal Entity Name"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.legalEntityName}
              onChangeText={(text) => setData({ ...data, legalEntityName: text })}
              accessibilityLabel="Legal Entity Name Input"
            />
            <TextInput
              style={styles.input}
              placeholder="PAN Number"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.panNumber}
              onChangeText={(text) => setData({ ...data, panNumber: text })}
              accessibilityLabel="PAN Number Input"
              autoCapitalize="characters"
            />
            <TextInput
              style={styles.input}
              placeholder="GST Number"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.gstNumber}
              onChangeText={(text) => setData({ ...data, gstNumber: text })}
              accessibilityLabel="GST Number Input"
            />
            <TextInput
              style={styles.input}
              placeholder="FSSAI License Number"
              placeholderTextColor="rgba(255, 255, 255, 0.6)"
              value={data.fssaiNumber}
              onChangeText={(text) => setData({ ...data, fssaiNumber: text })}
              accessibilityLabel="FSSAI License Number Input"
            />
            <Text style={styles.sectionTitle}>Supporting Documents</Text>
            <DocumentUploadButton
              onDocumentUploaded={(uri, fileName) => {
                const newDoc: DocumentUpload = { uri, type: 'gst', documentNumber: data.gstNumber };
                setData({ ...data, licenseDocuments: [...data.licenseDocuments, newDoc] });
              }}
              buttonText="Upload GST Document"
              style={{ marginBottom: 16 }}
            />
            <DocumentUploadButton
              onDocumentUploaded={(uri, fileName) => {
                const newDoc: DocumentUpload = { uri, type: 'fssai', documentNumber: data.fssaiNumber };
                setData({ ...data, licenseDocuments: [...data.licenseDocuments, newDoc] });
              }}
              buttonText="Upload FSSAI Document"
              style={{ marginBottom: 16 }}
            />
          </View>
        );
      case 3:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Restaurant Photos</Text>
            <Text style={styles.subtitle}>Add visual elements to make your restaurant stand out</Text>
            <ImageUploadButton
              onImageUploaded={(uri) => {
                setData({ ...data, logoUrl: uri });
              }}
              buttonText="Upload Restaurant Logo (Optional)"
              iconName="image-outline"
              style={{ marginBottom: 16 }}
            />
            <ImageUploadButton
              onImageUploaded={(uri) => {
                setData({ ...data, coverPhotoUrl: uri });
              }}
              buttonText="Upload Cover Photo (Optional)"
              iconName="images-outline"
              style={{ marginBottom: 16 }}
            />
          </View>
        );
      case 4:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Menu Setup</Text>
            <Text style={styles.subtitle}>Choose how you'd like to add your menu</Text>
            <View style={styles.menuOption}>
              <TouchableOpacity
                style={[styles.menuOptionButton, menuOption === 'photo' && styles.menuOptionButtonActive]}
                onPress={() => setMenuOption('photo')}
                accessibilityLabel="Select Menu from Photo"
              >
                <Text style={[styles.menuOptionText, menuOption === 'photo' && styles.menuOptionTextActive]}>
                  From Photo
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.menuOptionButton, menuOption === 'pdf' && styles.menuOptionButtonActive]}
                onPress={() => setMenuOption('pdf')}
                accessibilityLabel="Select Menu from PDF"
              >
                <Text style={[styles.menuOptionText, menuOption === 'pdf' && styles.menuOptionTextActive]}>
                  From PDF
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.menuOptionButton, menuOption === 'manual' && styles.menuOptionButtonActive]}
                onPress={() => setMenuOption('manual')}
                accessibilityLabel="Select Manual Menu Entry"
              >
                <Text style={[styles.menuOptionText, menuOption === 'manual' && styles.menuOptionTextActive]}>
                  Manual
                </Text>
              </TouchableOpacity>
            </View>
            {menuOption === 'manual' && (
              <TextInput
                style={[styles.input, styles.textArea]}
                placeholder="Paste or type your menu here..."
                placeholderTextColor="rgba(255, 255, 255, 0.6)"
                value={data.menuText}
                onChangeText={(text) => setData({ ...data, menuText: text })}
                multiline
                accessibilityLabel="Manual Menu Text Input"
              />
            )}
            {menuOption !== 'manual' && (
              menuOption === 'photo' ? (
                <ImageUploadButton
                  onImageUploaded={(uri) => {
                    setData({ ...data, menuText: `Menu photo uploaded: ${uri}` });
                  }}
                  buttonText="Upload Menu Photo"
                  iconName="camera-outline"
                  style={{ marginBottom: 16 }}
                />
              ) : (
                <DocumentUploadButton
                  onDocumentUploaded={(uri, fileName) => {
                    setData({ ...data, menuText: `Menu PDF uploaded: ${fileName}` });
                  }}
                  buttonText="Upload Menu PDF"
                  iconName="document-outline"
                  style={{ marginBottom: 16 }}
                />
              )
            )}
            <TouchableOpacity style={styles.button} onPress={extractMenuItems} accessibilityLabel="Extract Menu Items">
              <Text style={styles.buttonText}>Extract Menu Items</Text>
            </TouchableOpacity>
            {data.extractedItems.length > 0 && (
              <View style={styles.extractedItems}>
                <Text style={styles.sectionTitle}>Extracted Items:</Text>
                {data.extractedItems.map((item, index) => (
                  <View key={index} style={styles.item}>
                    <Text style={styles.itemText}>{item}</Text>
                  </View>
                ))}
              </View>
            )}
          </View>
        );
      default:
        return null;
    }
  };

  const isFormValid = () => {
    switch (currentPage) {
      case 0:
        return data.name && data.cuisineType && data.address && data.openingTime && data.closingTime && data.branchCity && data.branchState && data.branchPincode;
      case 1:
        return data.phone && data.email && data.phoneVerified && data.password && data.confirmPassword;
      case 2:
        return data.gstNumber && data.fssaiNumber && data.panNumber && data.legalEntityName;
      case 3:
        return true; // Optional
      case 4:
        return menuOption === 'manual' ? data.menuText : true;
      default:
        return false;
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <LinearGradient colors={['#16a34a', '#15803d']} style={styles.gradient}>
        <KeyboardAvoidingView
          style={{ flex: 1 }}
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
          keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
        >
          <ScrollView
            style={styles.scrollView}
            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
          >
            <View style={styles.content}>
              <View style={styles.header}>
                <TouchableOpacity onPress={handleSkip} accessibilityLabel="Skip Onboarding">
                  <Text style={styles.skipText}>Skip</Text>
                </TouchableOpacity>
                <View style={styles.indicators}>
                  {pages.map((_, index) => (
                    <View
                      key={index}
                      style={[
                        styles.indicator,
                        index === currentPage && styles.indicatorActive,
                      ]}
                    />
                  ))}
                </View>
              </View>
              <View style={styles.progressContainer}>
                <Text style={styles.title}>{pages[currentPage].title}</Text>
                <Text style={styles.progressText}>
                  Step {currentPage + 1} of {pages.length}
                </Text>
              </View>
              <View style={styles.pageContent}>{renderPage()}</View>
              <View style={styles.navigation}>
                <TouchableOpacity
                  style={[
                    styles.secondaryButton,
                    currentPage === 0 && styles.secondaryButtonDisabled
                  ]}
                  onPress={handlePrevious}
                  disabled={currentPage === 0}
                  accessibilityLabel="Previous Step"
                >
                  <Text style={[
                    styles.secondaryButtonText,
                    currentPage === 0 && styles.secondaryButtonTextDisabled
                  ]}>
                    Previous
                  </Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[
                    styles.primaryButton,
                    (!isFormValid() || isSubmitting) && styles.primaryButtonDisabled
                  ]}
                  onPress={() => { handleNext(); }}
                  disabled={!isFormValid() || isSubmitting}
                  accessibilityLabel={currentPage === pages.length - 1 ? "Complete Onboarding" : "Next Step"}
                >
                  <Text style={[
                    styles.primaryButtonText,
                    (!isFormValid() || isSubmitting) && styles.primaryButtonTextDisabled
                  ]}>
                    {isSubmitting ? 'Please wait...' : (currentPage === pages.length - 1 ? 'Complete' : 'Next')}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          </ScrollView>
        </KeyboardAvoidingView>
      </LinearGradient>

      {/* Cuisine Dropdown Modal */}
      <Modal visible={cuisineDropdownVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setCuisineDropdownVisible(false)} accessibilityLabel="Close Cuisine Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Select Cuisine Type</Text>
              <View />
            </View>
            <FlatList
              data={cuisines}
              keyExtractor={(item) => item}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={styles.dropdownItem}
                  onPress={() => {
                    setData({ ...data, cuisineType: item });
                    setCuisineDropdownVisible(false);
                  }}
                  accessibilityLabel={`Select ${item} Cuisine`}
                >
                  <Text style={styles.dropdownItemText}>{item}</Text>
                </TouchableOpacity>
              )}
            />
          </View>
        </View>
      </Modal>

      {/* Time Picker Modal */}
      <Modal visible={timePickerVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setTimePickerVisible(false)} accessibilityLabel="Close Time Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Select Time</Text>
              <View />
            </View>
            <Text style={styles.sectionTitle}>Select {timeType === 'opening' ? 'Opening' : 'Closing'} Time</Text>
            <FlatList
              data={Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)}
              keyExtractor={(item) => item}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={styles.timeItem}
                  onPress={() => handleTimeSelect(item)}
                  accessibilityLabel={`Select Time ${item}`}
                >
                  <Text style={styles.timeItemText}>{item}</Text>
                </TouchableOpacity>
              )}
            />
          </View>
        </View>
      </Modal>
      {/* Recaptcha Modal */}
      <FirebaseRecaptchaVerifierModal
        ref={recaptchaVerifier}
        firebaseConfig={auth.app.options}
      />

      {/* OTP Modal */}
      <Modal visible={otpModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setOtpModalVisible(false)} accessibilityLabel="Close OTP Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Verify {otpType === 'phone' ? 'Phone' : 'Email'}</Text>
              <View />
            </View>
            <Text style={styles.sectionTitle}>Enter OTP</Text>
            {/* <Text style={styles.subtitle}>Mock OTP: 1234</Text> */}
            <TextInput
              style={styles.otpInput}
              placeholder="Enter OTP"
              placeholderTextColor="#6b7280"
              value={otp}
              onChangeText={setOtp}
              keyboardType="numeric"
              accessibilityLabel="OTP Input"
            />
            <TouchableOpacity style={styles.otpButton} onPress={handleOtpSubmit} accessibilityLabel="Submit OTP">
              <Text style={styles.buttonText}>Verify</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}