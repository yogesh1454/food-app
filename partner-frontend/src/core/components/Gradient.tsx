import React from 'react';
import { LinearGradient, LinearGradientProps } from 'expo-linear-gradient';
import { StyleSheet, ViewStyle } from 'react-native';
import { colors } from '../constants/colors';

export interface GradientProps extends Omit<LinearGradientProps, 'colors'> {
    colors?: [string, string, ...string[]];
    variant?: 'primary' | 'secondary' | 'surface';
    style?: ViewStyle;
    children?: React.ReactNode;
}

export const Gradient: React.FC<GradientProps> = ({
    colors: customColors,
    variant = 'primary',
    style,
    children,
    ...props
}) => {
    let gradientColors: string[] = [];

    if (customColors) {
        gradientColors = customColors;
    } else {
        switch (variant) {
            case 'primary':
                gradientColors = [colors.primary, colors.primaryDark];
                break;
            case 'secondary':
                gradientColors = [colors.secondary, colors.secondaryDark];
                break;
            case 'surface':
                gradientColors = [colors.surface, colors.surfaceSecondary];
                break;
            default:
                gradientColors = [colors.primary, colors.primaryDark];
        }
    }

    return (
        <LinearGradient
            colors={gradientColors}
            style={[styles.container, style]}
            {...props}
        >
            {children}
        </LinearGradient>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
});
