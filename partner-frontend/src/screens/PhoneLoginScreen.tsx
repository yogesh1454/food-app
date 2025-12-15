import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  StyleSheet,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { sendOTP, verifyOTP } from '../core/services/phoneAuthservice';
import { useAppDispatch } from '../store';
import { hydrateRestaurant } from '../store/slices/restaurantSlice';
import { FirebaseRecaptchaVerifierModal } from 'expo-firebase-recaptcha';
import { firebaseConfig } from '../core/config/firebase';

type PhoneLoginNavigationProp = StackNavigationProp<RootStackParamList, 'PhoneLogin'>;
type PhoneLoginRouteProp = RouteProp<RootStackParamList, 'PhoneLogin'>;

export default function PhoneLoginScreen() {
  const navigation = useNavigation<PhoneLoginNavigationProp>();
  const route = useRoute<PhoneLoginRouteProp>();
  const { intent } = route.params;
  const dispatch = useAppDispatch();

  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [confirmation, setConfirmation] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const recaptchaVerifier = useRef<FirebaseRecaptchaVerifierModal>(null);

  const handleSendOTP = async () => {
    if (!phone || phone.length < 10) {
      Alert.alert('Error', 'Please enter a valid phone number');
      return;
    }

    setLoading(true);
    try {
      const formattedPhone = phone.startsWith('+91') ? phone : `+91${phone}`;
      // @ts-ignore - Firebase types mismatch with Expo Recaptcha
      const confirmationResult = await sendOTP(formattedPhone, recaptchaVerifier.current!);
      setConfirmation(confirmationResult);
      Alert.alert('Success', 'OTP sent to your phone!');
    } catch (error: any) {
      console.error(error);
      Alert.alert('Error', error.message || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOTP = async () => {
    if (!otp || otp.length !== 6) {
      Alert.alert('Error', 'Please enter a valid 6-digit OTP');
      return;
    }

    setLoading(true);
    try {
      const userCredential = await verifyOTP(confirmation, otp);
      Alert.alert('Success', 'Phone verified successfully!');
      console.log('User:', userCredential.user);

      if (intent === 'signup') {
        navigation.navigate('Onboarding');
      } else {
        // Try to hydrate restaurant data
        try {
          // @ts-ignore - Dispatching thunk
          await dispatch(hydrateRestaurant());
        } catch (e) {
          console.log('Hydration failed (expected if first login on new device):', e);
        }
        navigation.navigate('Main');
      }
    } catch (error: any) {
      console.error(error);
      Alert.alert('Error', 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <LinearGradient
        colors={['#16a34a', '#15803d']}
        style={styles.gradient}
      >
        <FirebaseRecaptchaVerifierModal
          ref={recaptchaVerifier}
          firebaseConfig={firebaseConfig}
          // @ts-ignore
          attemptInvisibleVerification={true}
        />

        <View style={styles.content}>
          <TouchableOpacity
            style={styles.backButton}
            onPress={() => navigation.goBack()}
          >
            <Ionicons name="arrow-back" size={24} color="white" />
          </TouchableOpacity>

          <Ionicons name="phone-portrait" size={64} color="white" />
          <Text style={styles.title}>
            {intent === 'signup' ? 'Create Account' : 'Welcome Back'}
          </Text>
          <Text style={styles.subtitle}>
            {!confirmation
              ? 'Enter your phone number to receive OTP'
              : 'Enter the OTP sent to your phone'}
          </Text>

          {!confirmation ? (
            <>
              <TextInput
                style={styles.input}
                placeholder="Phone Number"
                placeholderTextColor="rgba(255,255,255,0.6)"
                value={phone}
                onChangeText={setPhone}
                keyboardType="phone-pad"
                maxLength={13}
              />
              <TouchableOpacity
                style={styles.button}
                onPress={handleSendOTP}
                disabled={loading}
              >
                {loading ? (
                  <ActivityIndicator color="#16a34a" />
                ) : (
                  <Text style={styles.buttonText}>Send OTP</Text>
                )}
              </TouchableOpacity>
            </>
          ) : (
            <>
              <TextInput
                style={styles.input}
                placeholder="Enter OTP"
                placeholderTextColor="rgba(255,255,255,0.6)"
                value={otp}
                onChangeText={setOtp}
                keyboardType="number-pad"
                maxLength={6}
              />
              <TouchableOpacity
                style={styles.button}
                onPress={handleVerifyOTP}
                disabled={loading}
              >
                {loading ? (
                  <ActivityIndicator color="#16a34a" />
                ) : (
                  <Text style={styles.buttonText}>Verify OTP</Text>
                )}
              </TouchableOpacity>
            </>
          )}
        </View>
      </LinearGradient>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  gradient: { flex: 1, justifyContent: 'center', padding: 24 },
  content: { alignItems: 'center', width: '100%' },
  backButton: {
    position: 'absolute',
    top: -40,
    left: 0,
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: 20,
  },
  title: { fontSize: 32, fontWeight: 'bold', color: 'white', marginTop: 16 },
  subtitle: { fontSize: 16, color: 'rgba(255,255,255,0.8)', textAlign: 'center', marginTop: 8, marginBottom: 32 },
  input: { backgroundColor: 'rgba(255,255,255,0.2)', width: '100%', padding: 16, borderRadius: 12, color: 'white', fontSize: 16, marginBottom: 16 },
  button: { backgroundColor: 'white', paddingHorizontal: 48, paddingVertical: 16, borderRadius: 24, width: '100%', alignItems: 'center' },
  buttonText: { color: '#16a34a', fontSize: 18, fontWeight: '600' },
});