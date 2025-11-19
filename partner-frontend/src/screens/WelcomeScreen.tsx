import React from 'react';
import { View, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { GradientBackground } from '../components/ui/GradientBackground';
import { CircleIcon } from '../components/ui/CircleIcon';
import { Title, Subtitle, Caption } from '../components/ui/Typography';
import { FeatureRow } from '../components/ui/FeatureRow';
import { ActionButton } from '../components/ui/ActionButton';

type WelcomeScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Welcome'>;

export default function WelcomeScreen() {
  const navigation = useNavigation<WelcomeScreenNavigationProp>();

  return (
    <GradientBackground>
      {/* Logo/Icon */}
      <CircleIcon iconName="restaurant" />

      {/* Title */}
      <Title>
        Welcome to Nashtto
      </Title>

      {/* Subtitle */}
      <Subtitle>
        Your restaurant's command center for managing orders, menu, and growth
      </Subtitle>

      {/* Features */}
      <View style={styles.featuresContainer}>
        <FeatureRow iconName="analytics" text="Real-time Analytics" />
        <FeatureRow iconName="menu" text="Smart Menu Management" />
        <FeatureRow iconName="notifications" text="Instant Order Updates" />
      </View>

      {/* Get Started Button */}
      <ActionButton
        title="Get Started"
        iconName="arrow-forward"
        onPress={() => navigation.navigate('Onboarding')}
      />

      {/* Bottom Text */}
      <Caption>
        Join thousands of restaurants growing with Nashtto
      </Caption>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  featuresContainer: {
    marginBottom: 48,
  },
});