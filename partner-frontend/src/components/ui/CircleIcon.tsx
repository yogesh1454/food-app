import React from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface CircleIconProps {
    iconName: keyof typeof Ionicons.glyphMap;
    size?: number;
    iconSize?: number;
    backgroundColor?: string;
    iconColor?: string;
    style?: ViewStyle;
}

export const CircleIcon: React.FC<CircleIconProps> = ({
    iconName,
    size = 96,
    iconSize = 48,
    backgroundColor = 'white',
    iconColor = '#16a34a',
    style,
}) => {
    return (
        <View
            style={[
                styles.container,
                {
                    width: size,
                    height: size,
                    borderRadius: size / 2,
                    backgroundColor,
                },
                style,
            ]}
        >
            <Ionicons name={iconName} size={iconSize} color={iconColor} />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 32,
    },
});
