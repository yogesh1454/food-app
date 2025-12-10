import { StyleSheet } from 'react-native';
import { spacing } from '../constants/spacing';
import { colors } from '../constants/colors';
import { textStyles } from '../constants/typography';

export const commonStyles = StyleSheet.create({
  // Container styles
  container: {
    flex: 1,
  },
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  
  // Layout styles
  row: {
    flexDirection: 'row',
  },
  column: {
    flexDirection: 'column',
  },
  
  // Flex utilities
  flex1: { flex: 1 },
  flex2: { flex: 2 },
  flex3: { flex: 3 },
  
  // Alignment utilities
  itemsStart: { alignItems: 'flex-start' },
  itemsCenter: { alignItems: 'center' },
  itemsEnd: { alignItems: 'flex-end' },
  itemsStretch: { alignItems: 'stretch' },
  
  justifyStart: { justifyContent: 'flex-start' },
  justifyCenter: { justifyContent: 'center' },
  justifyEnd: { justifyContent: 'flex-end' },
  justifyBetween: { justifyContent: 'space-between' },
  justifyAround: { justifyContent: 'space-around' },
  justifyEvenly: { justifyContent: 'space-evenly' },
  
  // Text alignment
  textLeft: { textAlign: 'left' },
  textCenter: { textAlign: 'center' },
  textRight: { textAlign: 'right' },
  textJustify: { textAlign: 'justify' },
  
  // Border utilities
  border: { borderWidth: 1 },
  borderTop: { borderTopWidth: 1 },
  borderRight: { borderRightWidth: 1 },
  borderBottom: { borderBottomWidth: 1 },
  borderLeft: { borderLeftWidth: 1 },
  
  // Spacing utilities (padding)
  p1: { padding: spacing.xs },
  p2: { padding: spacing.sm },
  p3: { padding: spacing.md },
  p4: { padding: spacing.lg },
  p6: { padding: spacing.xl },
  p8: { padding: spacing.xxl },
  p12: { padding: spacing.xxxl },
  
  px1: { paddingHorizontal: spacing.xs },
  px2: { paddingHorizontal: spacing.sm },
  px3: { paddingHorizontal: spacing.md },
  px4: { paddingHorizontal: spacing.lg },
  px6: { paddingHorizontal: spacing.xl },
  px8: { paddingHorizontal: spacing.xxl },
  px12: { paddingHorizontal: spacing.xxxl },
  
  py1: { paddingVertical: spacing.xs },
  py2: { paddingVertical: spacing.sm },
  py3: { paddingVertical: spacing.md },
  py4: { paddingVertical: spacing.lg },
  py6: { paddingVertical: spacing.xl },
  py8: { paddingVertical: spacing.xxl },
  py12: { paddingVertical: spacing.xxxl },
  
  pt1: { paddingTop: spacing.xs },
  pt2: { paddingTop: spacing.sm },
  pt3: { paddingTop: spacing.md },
  pt4: { paddingTop: spacing.lg },
  pt6: { paddingTop: spacing.xl },
  pt8: { paddingTop: spacing.xxl },
  pt12: { paddingTop: spacing.xxxl },
  
  pr1: { paddingRight: spacing.xs },
  pr2: { paddingRight: spacing.sm },
  pr3: { paddingRight: spacing.md },
  pr4: { paddingRight: spacing.lg },
  pr6: { paddingRight: spacing.xl },
  pr8: { paddingRight: spacing.xxl },
  pr12: { paddingRight: spacing.xxxl },
  
  pb1: { paddingBottom: spacing.xs },
  pb2: { paddingBottom: spacing.sm },
  pb3: { paddingBottom: spacing.md },
  pb4: { paddingBottom: spacing.lg },
  pb6: { paddingBottom: spacing.xl },
  pb8: { paddingBottom: spacing.xxl },
  pb12: { paddingBottom: spacing.xxxl },
  
  pl1: { paddingLeft: spacing.xs },
  pl2: { paddingLeft: spacing.sm },
  pl3: { paddingLeft: spacing.md },
  pl4: { paddingLeft: spacing.lg },
  pl6: { paddingLeft: spacing.xl },
  pl8: { paddingLeft: spacing.xxl },
  pl12: { paddingLeft: spacing.xxxl },
  
  // Spacing utilities (margin)
  m1: { margin: spacing.xs },
  m2: { margin: spacing.sm },
  m3: { margin: spacing.md },
  m4: { margin: spacing.lg },
  m6: { margin: spacing.xl },
  m8: { margin: spacing.xxl },
  m12: { margin: spacing.xxxl },
  
  mx1: { marginHorizontal: spacing.xs },
  mx2: { marginHorizontal: spacing.sm },
  mx3: { marginHorizontal: spacing.md },
  mx4: { marginHorizontal: spacing.lg },
  mx6: { marginHorizontal: spacing.xl },
  mx8: { marginHorizontal: spacing.xxl },
  mx12: { marginHorizontal: spacing.xxxl },
  
  my1: { marginVertical: spacing.xs },
  my2: { marginVertical: spacing.sm },
  my3: { marginVertical: spacing.md },
  my4: { marginVertical: spacing.lg },
  my6: { marginVertical: spacing.xl },
  my8: { marginVertical: spacing.xxl },
  my12: { marginVertical: spacing.xxxl },
  
  mt1: { marginTop: spacing.xs },
  mt2: { marginTop: spacing.sm },
  mt3: { marginTop: spacing.md },
  mt4: { marginTop: spacing.lg },
  mt6: { marginTop: spacing.xl },
  mt8: { marginTop: spacing.xxl },
  mt12: { marginTop: spacing.xxxl },
  
  mr1: { marginRight: spacing.xs },
  mr2: { marginRight: spacing.sm },
  mr3: { marginRight: spacing.md },
  mr4: { marginRight: spacing.lg },
  mr6: { marginRight: spacing.xl },
  mr8: { marginRight: spacing.xxl },
  mr12: { marginRight: spacing.xxxl },
  
  mb1: { marginBottom: spacing.xs },
  mb2: { marginBottom: spacing.sm },
  mb3: { marginBottom: spacing.md },
  mb4: { marginBottom: spacing.lg },
  mb6: { marginBottom: spacing.xl },
  mb8: { marginBottom: spacing.xxl },
  mb12: { marginBottom: spacing.xxxl },
  
  ml1: { marginLeft: spacing.xs },
  ml2: { marginLeft: spacing.sm },
  ml3: { marginLeft: spacing.md },
  ml4: { marginLeft: spacing.lg },
  ml6: { marginLeft: spacing.xl },
  ml8: { marginLeft: spacing.xxl },
  ml12: { marginLeft: spacing.xxxl },
  
  // Border radius
  roundedNone: { borderRadius: 0 },
  rounded: { borderRadius: spacing.sm },
  roundedSm: { borderRadius: spacing.sm },
  roundedMd: { borderRadius: spacing.md },
  roundedLg: { borderRadius: spacing.lg },
  roundedXl: { borderRadius: spacing.xl },
  rounded2xl: { borderRadius: spacing.xxl },
  rounded3xl: { borderRadius: spacing.xxxl },
  roundedFull: { borderRadius: 9999 },
  
  // Shadow utilities
  shadow: {
    shadowColor: colors.text,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  shadowLg: {
    shadowColor: colors.text,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 6,
  },
  shadowXl: {
    shadowColor: colors.text,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
    elevation: 8,
  },
  
  // Background colors
  bgWhite: { backgroundColor: colors.surface },
  bgGray50: { backgroundColor: colors.background },
  bgGray100: { backgroundColor: colors.surfaceSecondary },
  bgPrimary: { backgroundColor: colors.primary },
  bgSuccess: { backgroundColor: colors.success },
  bgWarning: { backgroundColor: colors.warning },
  bgError: { backgroundColor: colors.error },
  bgInfo: { backgroundColor: colors.info },
  
  // Text colors
  textWhite: { color: colors.textWhite },
  textBlack: { color: colors.text },
  textPrimary: { color: colors.primary },
  textSecondary: { color: colors.textSecondary },
  textMuted: { color: colors.textMuted },
  textSuccess: { color: colors.success },
  textWarning: { color: colors.warning },
  textError: { color: colors.error },
  
  // Font styles
  fontNormal: { fontWeight: '400' },
  fontMedium: { fontWeight: '500' },
  fontSemibold: { fontWeight: '600' },
  fontBold: { fontWeight: '700' },
  
  // Font size utilities
  textXs: { fontSize: textStyles.small.fontSize },
  textSm: { fontSize: textStyles.small.fontSize },
  textBase: { fontSize: textStyles.body.fontSize },
  textLg: { fontSize: textStyles.bodyMedium.fontSize },
  textXl: { fontSize: textStyles.heading4.fontSize },
  text2xl: { fontSize: textStyles.heading3.fontSize },
  text3xl: { fontSize: textStyles.heading2.fontSize },
  
  // Width and height utilities
  wAuto: { width: 'auto' },
  wFull: { width: '100%' },
  hAuto: { height: 'auto' },
  hFull: { height: '100%' },
  
  // Position utilities
  relative: { position: 'relative' },
  absolute: { position: 'absolute' },
  
  // Preset text styles from typography constants
  ...textStyles,
});