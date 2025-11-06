import React from 'react';
import {
  TouchableOpacity,
  TouchableOpacityProps,
  Text,
  StyleSheet,
  ViewStyle,
  TextStyle,
  ActivityIndicator,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '../constants/colors';
import { spacing } from '../constants/spacing';
import { textStyles } from '../constants/typography';

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

interface BaseButtonProps extends Omit<TouchableOpacityProps, 'style'> {
  title: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  disabled?: boolean;
  leftIcon?: keyof typeof Ionicons.glyphMap;
  rightIcon?: keyof typeof Ionicons.glyphMap;
  fullWidth?: boolean;
  style?: ViewStyle;
  textStyle?: TextStyle;
}

export const Button: React.FC<BaseButtonProps> = ({
  title,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  leftIcon,
  rightIcon,
  fullWidth = false,
  style,
  textStyle,
  ...props
}) => {
  const isDisabled = disabled || loading;

  const buttonStyle = [
    styles.base,
    styles[size],
    styles[variant],
    fullWidth && styles.fullWidth,
    isDisabled && styles.disabled,
    style,
  ];

  const textStyleCombined = [
    styles.text,
    styles[`${size}Text`],
    styles[`${variant}Text`],
    isDisabled && styles.disabledText,
    textStyle,
  ];

  return (
    <TouchableOpacity
      style={buttonStyle}
      disabled={isDisabled}
      activeOpacity={0.8}
      {...props}
    >
      <View style={styles.content}>
        {loading ? (
          <ActivityIndicator 
            size="small" 
            color={getTextColor(variant, isDisabled)} 
            style={styles.loader}
          />
        ) : (
          <>
            {leftIcon && (
              <Ionicons 
                name={leftIcon} 
                size={getIconSize(size)} 
                color={getTextColor(variant, isDisabled)} 
                style={styles.leftIcon}
              />
            )}
            
            <Text style={textStyleCombined}>
              {title}
            </Text>
            
            {rightIcon && (
              <Ionicons 
                name={rightIcon} 
                size={getIconSize(size)} 
                color={getTextColor(variant, isDisabled)} 
                style={styles.rightIcon}
              />
            )}
          </>
        )}
      </View>
    </TouchableOpacity>
  );
};

// Helper function to get text color based on variant and disabled state
const getTextColor = (variant: ButtonVariant, disabled: boolean): string => {
  if (disabled) return colors.disabledText;
  
  switch (variant) {
    case 'primary':
      return colors.textWhite;
    case 'secondary':
      return colors.textWhite;
    case 'outline':
      return colors.primary;
    case 'ghost':
      return colors.primary;
    case 'danger':
      return colors.textWhite;
    default:
      return colors.textWhite;
  }
};

// Helper function to get icon size based on button size
const getIconSize = (size: ButtonSize): number => {
  switch (size) {
    case 'sm':
      return 14;
    case 'md':
      return 18;
    case 'lg':
      return 22;
    default:
      return 18;
  }
};

// Specialized button variants
export const PrimaryButton: React.FC<Omit<BaseButtonProps, 'variant'>> = (props) => {
  return <Button {...props} variant="primary" />;
};

export const SecondaryButton: React.FC<Omit<BaseButtonProps, 'variant'>> = (props) => {
  return <Button {...props} variant="secondary" />;
};

export const OutlineButton: React.FC<Omit<BaseButtonProps, 'variant'>> = (props) => {
  return <Button {...props} variant="outline" />;
};

export const GhostButton: React.FC<Omit<BaseButtonProps, 'variant'>> = (props) => {
  return <Button {...props} variant="ghost" />;
};

export const DangerButton: React.FC<Omit<BaseButtonProps, 'variant'>> = (props) => {
  return <Button {...props} variant="danger" />;
};

// Icon-only button for actions
interface IconButtonProps extends Omit<TouchableOpacityProps, 'style' | 'children'> {
  icon: keyof typeof Ionicons.glyphMap;
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
  badge?: number;
}

export const IconButton: React.FC<IconButtonProps> = ({
  icon,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  style,
  badge,
  ...props
}) => {
  const isDisabled = disabled || loading;

  const buttonStyle = [
    styles.iconButton,
    styles[size],
    styles[variant],
    isDisabled && styles.disabled,
    style,
  ];

  return (
    <TouchableOpacity
      style={buttonStyle}
      disabled={isDisabled}
      activeOpacity={0.8}
      {...props}
    >
      <View style={styles.iconButtonContent}>
        {loading ? (
          <ActivityIndicator 
            size="small" 
            color={getTextColor(variant, isDisabled)} 
          />
        ) : (
          <>
            <Ionicons 
              name={icon} 
              size={getIconSize(size)} 
              color={getTextColor(variant, isDisabled)} 
            />
            {badge && badge > 0 && (
              <View style={styles.badge}>
                <Text style={styles.badgeText}>
                  {badge > 99 ? '99+' : badge}
                </Text>
              </View>
            )}
          </>
        )}
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  base: {
    borderRadius: spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  fullWidth: {
    width: '100%',
  },
  
  // Size variants
  sm: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    minHeight: 36,
  },
  md: {
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    minHeight: 44,
  },
  lg: {
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.lg,
    minHeight: 52,
  },
  
  // Variant styles
  primary: {
    backgroundColor: colors.primary,
  },
  secondary: {
    backgroundColor: colors.secondary,
  },
  outline: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: colors.primary,
  },
  ghost: {
    backgroundColor: 'transparent',
  },
  danger: {
    backgroundColor: colors.error,
  },
  
  // Text styles
  text: {
    fontWeight: textStyles.button.fontWeight,
  },
  smText: {
    fontSize: textStyles.small.fontSize,
  },
  mdText: {
    fontSize: textStyles.button.fontSize,
  },
  lgText: {
    fontSize: textStyles.body.fontSize,
  },
  primaryText: {
    color: colors.textWhite,
  },
  secondaryText: {
    color: colors.textWhite,
  },
  outlineText: {
    color: colors.primary,
  },
  ghostText: {
    color: colors.primary,
  },
  dangerText: {
    color: colors.textWhite,
  },
  
  // Icons
  leftIcon: {
    marginRight: spacing.sm,
  },
  rightIcon: {
    marginLeft: spacing.sm,
  },
  loader: {
    marginRight: spacing.sm,
  },
  
  // Disabled state
  disabled: {
    backgroundColor: colors.disabled,
    borderColor: colors.disabled,
  },
  disabledText: {
    color: colors.disabledText,
  },
  
  // Icon button styles
  iconButton: {
    borderRadius: spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  iconButtonContent: {
    position: 'relative',
  },
  badge: {
    position: 'absolute',
    top: -8,
    right: -8,
    backgroundColor: colors.error,
    borderRadius: 10,
    minWidth: 20,
    height: 20,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: colors.surface,
  },
  badgeText: {
    color: colors.textWhite,
    fontSize: 10,
    fontWeight: '700',
  },
});

export default Button;