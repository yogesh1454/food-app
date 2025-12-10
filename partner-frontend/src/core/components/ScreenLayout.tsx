import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  TouchableOpacity,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, textStyles } from '../constants';
import { Button } from './Button';

interface ScreenLayoutProps {
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
  showBackButton?: boolean;
  onBackPress?: () => void;
  rightComponent?: React.ReactNode;
  scrollable?: boolean;
  loading?: boolean;
  error?: string | null;
  onRetry?: () => void;
  backgroundColor?: string;
  headerBackgroundColor?: string;
  padding?: number;
}

export const ScreenLayout: React.FC<ScreenLayoutProps> = ({
  children,
  title,
  subtitle,
  showBackButton = false,
  onBackPress,
  rightComponent,
  scrollable = true,
  loading = false,
  error = null,
  onRetry,
  backgroundColor = colors.background,
  headerBackgroundColor = colors.surface,
  padding = spacing.md,
}) => {
  const Content = () => (
    <View style={[styles.container, { backgroundColor }]}>
      {title && (
        <View style={[styles.header, { backgroundColor: headerBackgroundColor }]}>
          <View style={styles.headerContent}>
            {showBackButton && (
              <TouchableOpacity
                style={styles.backButton}
                onPress={onBackPress}
                hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              >
                <Ionicons name="chevron-back" size={24} color={colors.text} />
              </TouchableOpacity>
            )}
            <View style={styles.titleContainer}>
              <Text style={styles.title}>{title}</Text>
              {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
            </View>
            {rightComponent && <View style={styles.rightComponent}>{rightComponent}</View>}
          </View>
        </View>
      )}

      <View style={[styles.content, { padding }]}>
        {loading ? (
          <View style={styles.loadingContainer}>
            <Text style={styles.loadingText}>Loading...</Text>
          </View>
        ) : error ? (
          <View style={styles.errorContainer}>
            <Text style={styles.errorText}>{error}</Text>
            {onRetry && (
              <Button
                title="Retry"
                variant="outline"
                onPress={onRetry}
                style={styles.retryButton}
              />
            )}
          </View>
        ) : (
          children
        )}
      </View>
    </View>
  );

  if (scrollable) {
    return (
      <SafeAreaView style={styles.safeArea}>
        <ScrollView
          style={styles.scrollView}
          contentContainerStyle={styles.scrollViewContent}
          showsVerticalScrollIndicator={false}
        >
          <Content />
        </ScrollView>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <Content />
    </SafeAreaView>
  );
};

interface SectionProps {
  title?: string;
  subtitle?: string;
  children: React.ReactNode;
  style?: any;
  headerStyle?: any;
  contentStyle?: any;
}

export const Section: React.FC<SectionProps> = ({
  title,
  subtitle,
  children,
  style,
  headerStyle,
  contentStyle,
}) => {
  return (
    <View style={[styles.section, style]}>
      {(title || subtitle) && (
        <View style={[styles.sectionHeader, headerStyle]}>
          {title && <Text style={styles.sectionTitle}>{title}</Text>}
          {subtitle && <Text style={styles.sectionSubtitle}>{subtitle}</Text>}
        </View>
      )}
      <View style={[styles.sectionContent, contentStyle]}>{children}</View>
    </View>
  );
};

interface EmptyStateProps {
  title: string;
  subtitle?: string;
  icon?: keyof typeof Ionicons.glyphMap;
  actionText?: string;
  onAction?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  subtitle,
  icon = 'cube-outline',
  actionText,
  onAction,
}) => {
  return (
    <View style={styles.emptyState}>
      <Ionicons name={icon} size={64} color={colors.textMuted} />
      <Text style={styles.emptyStateTitle}>{title}</Text>
      {subtitle && <Text style={styles.emptyStateSubtitle}>{subtitle}</Text>}
      {actionText && onAction && (
        <Button
          title={actionText}
          variant="outline"
          onPress={onAction}
          style={styles.emptyStateButton}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.background,
  },
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  scrollViewContent: {
    flexGrow: 1,
  },
  header: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  headerContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  backButton: {
    marginRight: spacing.sm,
    padding: spacing.xs,
  },
  titleContainer: {
    flex: 1,
  },
  title: {
    fontSize: 20,
    fontWeight: '600',
    color: colors.text,
  },
  subtitle: {
    fontSize: 14,
    color: colors.textSecondary,
    marginTop: spacing.xs / 2,
  },
  rightComponent: {
    marginLeft: spacing.sm,
  },
  content: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  loadingText: {
    fontSize: 16,
    color: colors.textSecondary,
    marginTop: spacing.sm,
  },
  errorContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.lg,
  },
  errorText: {
    fontSize: 16,
    color: colors.error,
    textAlign: 'center',
    marginBottom: spacing.md,
  },
  retryButton: {
    marginTop: spacing.sm,
  },
  section: {
    marginBottom: spacing.lg,
  },
  sectionHeader: {
    marginBottom: spacing.md,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  sectionSubtitle: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  sectionContent: {
    // Content styles handled by children
  },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.xl,
  },
  emptyStateTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.text,
    marginTop: spacing.md,
    marginBottom: spacing.sm,
    textAlign: 'center',
  },
  emptyStateSubtitle: {
    fontSize: 14,
    color: colors.textSecondary,
    textAlign: 'center',
    marginBottom: spacing.lg,
  },
  emptyStateButton: {
    marginTop: spacing.md,
  },
});

export default ScreenLayout;