import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Modal,
  TouchableWithoutFeedback,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';

type WelcomeScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Welcome'>;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  gradient: {
    flex: 1,
    paddingHorizontal: 24,
    justifyContent: 'center',
    alignItems: 'center',
  },
  logoContainer: {
    width: 96,
    height: 96,
    backgroundColor: 'white',
    borderRadius: 48,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 32,
  },
  title: {
    fontSize: 36,
    fontWeight: 'bold',
    color: 'white',
    textAlign: 'center',
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 20,
    color: 'rgba(255, 255, 255, 0.9)',
    textAlign: 'center',
    marginBottom: 48,
    lineHeight: 24,
  },
  featuresContainer: {
    marginBottom: 48,
  },
  featureItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  featureIcon: {
    width: 32,
    height: 32,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 16,
  },
  featureText: {
    color: 'white',
    fontSize: 18,
  },
  buttonsContainer: {
    width: '100%',
    gap: 16,
  },
  button: {
    backgroundColor: 'white',
    paddingHorizontal: 32,
    paddingVertical: 16,
    borderRadius: 24,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
  },
  buttonText: {
    color: '#16a34a',
    fontSize: 18,
    fontWeight: '600',
    marginRight: 8,
  },
  secondaryButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    paddingHorizontal: 32,
    paddingVertical: 16,
    borderRadius: 24,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    borderWidth: 1,
    borderColor: 'white',
  },
  secondaryButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: '600',
    marginRight: 8,
  },
  bottomText: {
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    marginTop: 32,
    fontSize: 14,
  },
  // Modal Styles
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: 'white',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 48,
  },
  modalTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: 8,
    textAlign: 'center',
  },
  modalSubtitle: {
    fontSize: 16,
    color: '#6b7280',
    marginBottom: 24,
    textAlign: 'center',
  },
  optionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#f3f4f6',
    borderRadius: 12,
    marginBottom: 12,
  },
  optionIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'white',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 16,
  },
  optionText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1f2937',
  },
});

export default function WelcomeScreen() {
  const navigation = useNavigation<WelcomeScreenNavigationProp>();
  const [modalVisible, setModalVisible] = useState(false);
  const [authIntent, setAuthIntent] = useState<'login' | null>(null);

  const handleAuthPress = (intent: 'login') => {
    setAuthIntent(intent);
    setModalVisible(true);
  };

  const handleOptionSelect = (method: 'email' | 'phone') => {
    setModalVisible(false);
    if (!authIntent) return;

    if (method === 'email') {
      navigation.navigate('EmailAuth', { intent: authIntent });
    } else {
      navigation.navigate('PhoneLogin', { intent: authIntent });
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <LinearGradient
        colors={['#16a34a', '#15803d']}
        style={styles.gradient}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
      >
        {/* Logo/Icon */}
        <View style={styles.logoContainer}>
          <Ionicons name="restaurant" size={48} color="#16a34a" />
        </View>

        {/* Title */}
        <Text style={styles.title}>
          Welcome to Nashtto
        </Text>

        {/* Subtitle */}
        <Text style={styles.subtitle}>
          Your restaurant's command center for managing orders, menu, and growth
        </Text>

        {/* Features */}
        <View style={styles.featuresContainer}>
          <View style={styles.featureItem}>
            <View style={styles.featureIcon}>
              <Ionicons name="analytics" size={20} color="white" />
            </View>
            <Text style={styles.featureText}>Real-time Analytics</Text>
          </View>

          <View style={styles.featureItem}>
            <View style={styles.featureIcon}>
              <Ionicons name="menu" size={20} color="white" />
            </View>
            <Text style={styles.featureText}>Smart Menu Management</Text>
          </View>

          <View style={styles.featureItem}>
            <View style={styles.featureIcon}>
              <Ionicons name="notifications" size={20} color="white" />
            </View>
            <Text style={styles.featureText}>Instant Order Updates</Text>
          </View>
        </View>

        {/* Auth Buttons */}
        <View style={styles.buttonsContainer}>
          <TouchableOpacity
            style={styles.button}
            onPress={() => navigation.navigate('Onboarding')}
          >
            <Text style={styles.buttonText}>
              Sign Up
            </Text>
            <Ionicons name="person-add" size={20} color="#16a34a" />
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.secondaryButton}
            onPress={() => handleAuthPress('login')}
          >
            <Text style={styles.secondaryButtonText}>
              Sign In
            </Text>
            <Ionicons name="log-in" size={20} color="white" />
          </TouchableOpacity>
        </View>

        {/* Bottom Text */}
        <Text style={styles.bottomText}>
          Join thousands of restaurants growing with Nashtto
        </Text>

        {/* Selection Modal */}
        <Modal
          animationType="slide"
          transparent={true}
          visible={modalVisible}
          onRequestClose={() => setModalVisible(false)}
        >
          <TouchableWithoutFeedback onPress={() => setModalVisible(false)}>
            <View style={styles.modalOverlay}>
              <TouchableWithoutFeedback>
                <View style={styles.modalContent}>
                  <Text style={styles.modalTitle}>
                    Welcome Back
                  </Text>
                  <Text style={styles.modalSubtitle}>
                    Choose how you want to sign in
                  </Text>

                  <TouchableOpacity
                    style={styles.optionButton}
                    onPress={() => handleOptionSelect('email')}
                  >
                    <View style={styles.optionIcon}>
                      <Ionicons name="mail" size={24} color="#16a34a" />
                    </View>
                    <Text style={styles.optionText}>Continue with Email</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.optionButton}
                    onPress={() => handleOptionSelect('phone')}
                  >
                    <View style={styles.optionIcon}>
                      <Ionicons name="phone-portrait" size={24} color="#16a34a" />
                    </View>
                    <Text style={styles.optionText}>Continue with Phone</Text>
                  </TouchableOpacity>
                </View>
              </TouchableWithoutFeedback>
            </View>
          </TouchableWithoutFeedback>
        </Modal>

      </LinearGradient>
    </SafeAreaView>
  );
}