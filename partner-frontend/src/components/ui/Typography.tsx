import React from 'react';
import { Text, StyleSheet, TextStyle, TextProps } from 'react-native';

interface TypographyProps extends TextProps {
    children: React.ReactNode;
    style?: TextStyle;
}

export const Title: React.FC<TypographyProps> = ({ children, style, ...props }) => (
    <Text style={[styles.title, style]} {...props}>
        {children}
    </Text>
);

export const Subtitle: React.FC<TypographyProps> = ({ children, style, ...props }) => (
    <Text style={[styles.subtitle, style]} {...props}>
        {children}
    </Text>
);

export const BodyText: React.FC<TypographyProps> = ({ children, style, ...props }) => (
    <Text style={[styles.bodyText, style]} {...props}>
        {children}
    </Text>
);

export const Caption: React.FC<TypographyProps> = ({ children, style, ...props }) => (
    <Text style={[styles.caption, style]} {...props}>
        {children}
    </Text>
);

const styles = StyleSheet.create({
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
    bodyText: {
        color: 'white',
        fontSize: 18,
    },
    caption: {
        color: 'rgba(255, 255, 255, 0.7)',
        textAlign: 'center',
        marginTop: 32,
        fontSize: 14,
    },
});
