import React from 'react';
import {
  TouchableOpacity,
  TouchableOpacityProps,
  View,
  ViewProps,
  StyleSheet,
  ViewStyle,
} from 'react-native';
import { colors } from '../constants/colors';
import { spacing } from '../constants/spacing';

interface BaseWrapperProps {
  children: React.ReactNode;
  style?: ViewStyle;
  testID?: string;
}

// Simple wrapper component that becomes touchable when onPress is provided
export const Wrapper: React.FC<BaseWrapperProps & TouchableOpacityProps> = ({
  children,
  style,
  onPress,
  testID,
  ...props
}) => {
  const Component = onPress ? TouchableOpacity : View;
  
  return (
    <Component 
      style={[styles.base, style]} 
      testID={testID}
      onPress={onPress}
      {...props}
    >
      {children}
    </Component>
  );
};

// Card wrapper with consistent styling
export const Card: React.FC<BaseWrapperProps & TouchableOpacityProps> = ({
  children,
  style,
  onPress,
  testID,
  ...props
}) => {
  const Component = onPress ? TouchableOpacity : View;
  
  return (
    <Component 
      style={[styles.card, style]} 
      testID={testID}
      onPress={onPress}
      activeOpacity={onPress ? 0.9 : 1}
      {...props}
    >
      {children}
    </Component>
  );
};

// Container wrapper
export const Container: React.FC<BaseWrapperProps> = ({
  children,
  style,
  testID,
}) => {
  return (
    <View style={[styles.container, style]} testID={testID}>
      {children}
    </View>
  );
};

// Center wrapper
export const Center: React.FC<BaseWrapperProps> = ({
  children,
  style,
  testID,
}) => {
  return (
    <View style={[styles.center, style]} testID={testID}>
      {children}
    </View>
  );
};

// Row wrapper
export const Row: React.FC<BaseWrapperProps> = ({
  children,
  style,
  testID,
}) => {
  return (
    <View style={[styles.row, style]} testID={testID}>
      {children}
    </View>
  );
};

// Column wrapper
export const Column: React.FC<BaseWrapperProps> = ({
  children,
  style,
  testID,
}) => {
  return (
    <View style={[styles.column, style]} testID={testID}>
      {children}
    </View>
  );
};

const styles = StyleSheet.create({
  base: {
    flex: 1,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: spacing.md,
    padding: spacing.lg,
    marginVertical: spacing.sm,
    shadowColor: colors.text,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  container: {
    flex: 1,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  column: {
    flexDirection: 'column',
  },
});

export default Wrapper;