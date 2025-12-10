import React from 'react';
import { View, ViewProps, StyleSheet, ViewStyle } from 'react-native';
import { spacing } from '../constants/spacing';
import { colors } from '../constants/colors';

export interface BoxProps extends ViewProps {
    flex?: number;
    row?: boolean;
    center?: boolean;
    align?: ViewStyle['alignItems'];
    justify?: ViewStyle['justifyContent'];
    padding?: keyof typeof spacing | number;
    margin?: keyof typeof spacing | number;
    bg?: keyof typeof colors | string;
    style?: ViewStyle;
    children?: React.ReactNode;
}

export const Box: React.FC<BoxProps> = ({
    flex,
    row,
    center,
    align,
    justify,
    padding,
    margin,
    bg,
    style,
    children,
    ...props
}) => {
    const baseStyle: ViewStyle = {
        flex,
        flexDirection: row ? 'row' : 'column',
        alignItems: center ? 'center' : align,
        justifyContent: center ? 'center' : justify,
        padding: typeof padding === 'string' ? spacing[padding as keyof typeof spacing] : padding,
        margin: typeof margin === 'string' ? spacing[margin as keyof typeof spacing] : margin,
        backgroundColor: bg && (colors[bg as keyof typeof colors] || bg),
    };

    return (
        <View style={[baseStyle, style]} {...props}>
            {children}
        </View>
    );
};
