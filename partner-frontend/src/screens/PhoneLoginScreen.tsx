import React, { useState } from 'react';
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
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { sendOTP, verifyOTP } from '../core/services/phoneAuthservice';

type PhoneLoginNavigationProp = StackNavigationProp<RootStackParamList, 'PhoneLogin'>;

export default function PhoneLoginScreen() {
  const navigation = useNavigation<PhoneLoginNavigationProp>();
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [confirmation, setConfirmation] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleSendOTP = async () => {
    if (!phone || phone.length < 10) {
      Alert.alert('Error', 'Please enter a valid phone number');
      return;
    }
    
    setLoading(true);
    try {
      const formattedPhone = phone.startsWith('+91') ? phone : `+91${phone}`;
      const confirmationResult = await sendOTP(formattedPhone);
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
      // Navigate to main app
      navigation.navigate('Main');
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
        <View id="recaptcha-container" />

        <View style={styles.content}>
          <Ionicons name="phone-portrait" size={64} color="white" />
          <Text style={styles.title}>Phone Verification</Text>
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
  content: { alignItems: 'center' },
  title: { fontSize: 32, fontWeight: 'bold', color: 'white', marginTop: 16 },
  subtitle: { fontSize: 16, color: 'rgba(255,255,255,0.8)', textAlign: 'center', marginTop: 8, marginBottom: 32 },
  input: { backgroundColor: 'rgba(255,255,255,0.2)', width: '100%', padding: 16, borderRadius: 12, color: 'white', fontSize: 16, marginBottom: 16 },
  button: { backgroundColor: 'white', paddingHorizontal: 48, paddingVertical: 16, borderRadius: 24, width: '100%', alignItems: 'center' },
  buttonText: { color: '#16a34a', fontSize: 18, fontWeight: '600' },
});