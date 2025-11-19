import React from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { BodyText } from './Typography';

interface FeatureRowProps {
    iconName: keyof typeof Ionicons.glyphMap;
    text: string;
    style?: ViewStyle;
}

export const FeatureRow: React.FC<FeatureRowProps> = ({ iconName, text, style }) => {
    return (
        <View style={[styles.container, style]}>
            <View style={styles.iconContainer}>
                <Ionicons name={iconName} size={20} color="white" />
            </View>
            <BodyText>{text}</BodyText>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 16,
    },
    iconContainer: {
        width: 32,
        height: 32,
        backgroundColor: 'rgba(255, 255, 255, 0.2)',
        borderRadius: 16,
        alignItems: 'center',
        justifyContent: 'center',
        marginRight: 16,
    },
});
