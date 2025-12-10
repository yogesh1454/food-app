import React from 'react';
import { Text, TextProps, StyleSheet, TextStyle } from 'react-native';
import { textStyles } from '../constants/typography';
import { colors } from '../constants/colors';

export type TypographyVariant = keyof typeof textStyles;

export interface TypographyProps extends TextProps {
    variant?: TypographyVariant;
    color?: keyof typeof colors | string;
    align?: TextStyle['textAlign'];
    weight?: TextStyle['fontWeight'];
    size?: number;
    style?: TextStyle;
    children?: React.ReactNode;
}

export const Typography: React.FC<TypographyProps> = ({
    variant = 'body',
    color = 'text',
    align,
    weight,
    size,
    style,
    children,
    ...props
}) => {
    const variantStyle = textStyles[variant];

    const customStyle: TextStyle = {
        color: colors[color as keyof typeof colors] || color,
        textAlign: align,
        ...(weight && { fontWeight: weight }),
        ...(size && { fontSize: size }),
    };

    return (
        <Text style={[variantStyle, customStyle, style]} {...props}>
            {children}
        </Text>
    );
};
