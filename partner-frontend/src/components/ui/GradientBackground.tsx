import React from 'react';
import { StyleSheet, ViewStyle } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';

interface GradientBackgroundProps {
    children: React.ReactNode;
    colors?: readonly [string, string, ...string[]];
    style?: ViewStyle;
}

export const GradientBackground: React.FC<GradientBackgroundProps> = ({
    children,
    colors = ['#16a34a', '#15803d'],
    style,
}) => {
    return (
        <SafeAreaView style={styles.container}>
            <LinearGradient
                colors={colors}
                style={[styles.gradient, style]}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 1 }}
            >
                {children}
            </LinearGradient>
        </SafeAreaView>
    );
};

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
});
