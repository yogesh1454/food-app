import React from 'react';
import {
  TouchableOpacity,
  SafeAreaView,
  StyleSheet,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { Box } from '../core/components/Box';
import { Typography } from '../core/components/Typography';
import { Gradient } from '../core/components/Gradient';

type WelcomeScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Welcome'>;

export default function WelcomeScreen() {
  const navigation = useNavigation<WelcomeScreenNavigationProp>();

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <Box flex={1}>
      <Gradient
        variant="primary"
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={styles.gradient}
      >
        {/* Logo/Icon */}
        <Box
          bg="surface"
          padding="xxl"
          style={styles.logoContainer}
        >
          <Ionicons name="restaurant" size={48} color="#16a34a" />
        </Box>

        {/* Title */}
        <Typography
          variant="heading1"
          color="textWhite"
          align="center"
          style={styles.marginBottom}
        >
          Welcome to Nashtto
        </Typography>

        {/* Subtitle */}
        <Typography
          variant="heading4"
          color="rgba(255, 255, 255, 0.9)"
          align="center"
          style={styles.subtitle}
        >
          Your restaurant's command center for managing orders, menu, and growth
        </Typography>

        {/* Features */}
        <Box style={styles.featuresContainer}>
          <FeatureItem icon="analytics" text="Real-time Analytics" />
          <FeatureItem icon="menu" text="Smart Menu Management" />
          <FeatureItem icon="notifications" text="Instant Order Updates" />
        </Box>

        {/* Get Started Button */}
        <TouchableOpacity
          style={styles.button}
          onPress={() => navigation.navigate('Onboarding')}
          activeOpacity={0.9}
        >
          <Typography
            variant="button"
            color="#16a34a"
            style={styles.buttonText}
          >
            Get Started
          </Typography>
          <Ionicons name="arrow-forward" size={20} color="#16a34a" />
        </TouchableOpacity>

        {/* Bottom Text */}
        <Typography
          variant="small"
          color="rgba(255, 255, 255, 0.7)"
          align="center"
          style={styles.marginTop}
        >
          Join thousands of restaurants growing with Nashtto
        </Typography>
      </Gradient>
    </Box>
  );
}

const FeatureItem = ({ icon, text }: { icon: keyof typeof Ionicons.glyphMap; text: string }) => (
  <Box row center style={styles.featureItem}>
    <Box style={styles.featureIcon}>
      <Ionicons name={icon} size={20} color="white" />
    </Box>
    <Typography variant="body" size={18} color="textWhite">
      {text}
    </Typography>
  </Box>
);

const styles = StyleSheet.create({
  gradient: {
    paddingHorizontal: 24,
    justifyContent: 'center',
    alignItems: 'center',
  },
  logoContainer: {
    width: 96,
    height: 96,
    borderRadius: 48,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 32,
  },
  marginBottom: {
    marginBottom: 16,
  },
  subtitle: {
    marginBottom: 48,
    lineHeight: 24,
  },
  featuresContainer: {
    marginBottom: 48,
    width: '100%',
  },
  featureItem: {
    marginBottom: 16,
    justifyContent: 'flex-start',
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
  button: {
    backgroundColor: 'white',
    paddingHorizontal: 32,
    paddingVertical: 16,
    borderRadius: 24,
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    justifyContent: 'center',
  },
  buttonText: {
    marginRight: 8,
  },
  marginTop: {
    marginTop: 32,
  },
});