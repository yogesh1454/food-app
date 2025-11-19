import React from 'react';
import { TouchableOpacity, Text, StyleSheet, ViewStyle, TouchableOpacityProps } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface ActionButtonProps extends TouchableOpacityProps {
    title: string;
    iconName?: keyof typeof Ionicons.glyphMap;
    style?: ViewStyle;
}

export const ActionButton: React.FC<ActionButtonProps> = ({
    title,
    iconName,
    style,
    ...props
}) => {
    return (
        <TouchableOpacity style={[styles.button, style]} activeOpacity={0.8} {...props}>
            <Text style={styles.text}>{title}</Text>
            {iconName && <Ionicons name={iconName} size={20} color="#16a34a" />}
        </TouchableOpacity>
    );
};

const styles = StyleSheet.create({
    button: {
        backgroundColor: 'white',
        paddingHorizontal: 32,
        paddingVertical: 16,
        borderRadius: 24,
        flexDirection: 'row',
        alignItems: 'center',
    },
    text: {
        color: '#16a34a',
        fontSize: 18,
        fontWeight: '600',
        marginRight: 8,
    },
});
