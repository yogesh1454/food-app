import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ViewStyle,
  TextStyle,
} from 'react-native';
import { colors, spacing, textStyles } from '../constants';

interface CardProps {
  children: React.ReactNode;
  style?: ViewStyle;
  onPress?: () => void;
  padding?: number;
  margin?: number;
  elevation?: number;
  shadow?: boolean;
}

interface CardHeaderProps {
  title: string;
  subtitle?: string;
  rightComponent?: React.ReactNode;
  style?: ViewStyle;
  titleStyle?: TextStyle;
  subtitleStyle?: TextStyle;
}

interface CardContentProps {
  children: React.ReactNode;
  style?: ViewStyle;
  padding?: number;
}

interface CardFooterProps {
  children: React.ReactNode;
  style?: ViewStyle;
  padding?: number;
}

export const Card: React.FC<CardProps> = ({
  children,
  style,
  onPress,
  padding = spacing.md,
  margin = 0,
  elevation = 2,
  shadow = true,
}) => {
  const cardStyle = [
    styles.card,
    {
      padding,
      margin,
      elevation,
    },
    shadow && styles.shadow,
    style,
  ];

  if (onPress) {
    return (
      <TouchableOpacity style={cardStyle} onPress={onPress} activeOpacity={0.8}>
        {children}
      </TouchableOpacity>
    );
  }

  return <View style={cardStyle}>{children}</View>;
};

export const CardHeader: React.FC<CardHeaderProps> = ({
  title,
  subtitle,
  rightComponent,
  style,
  titleStyle,
  subtitleStyle,
}) => {
  return (
    <View style={[styles.header, style]}>
      <View style={styles.headerContent}>
        <Text style={[styles.title, titleStyle]}>{title}</Text>
        {subtitle && (
          <Text style={[styles.subtitle, subtitleStyle]}>{subtitle}</Text>
        )}
      </View>
      {rightComponent && <View>{rightComponent}</View>}
    </View>
  );
};

export const CardContent: React.FC<CardContentProps> = ({
  children,
  style,
  padding = spacing.md,
}) => {
  return (
    <View style={[styles.content, { padding }, style]}>
      {children}
    </View>
  );
};

export const CardFooter: React.FC<CardFooterProps> = ({
  children,
  style,
  padding = spacing.md,
}) => {
  return (
    <View style={[styles.footer, { padding }, style]}>
      {children}
    </View>
  );
};

// Specialized card components
interface MenuItemCardProps {
  item: {
    menuItemId: string;
    name: string;
    description: string;
    price: number;
    category: string;
    imageUrl?: string;
    isAvailable: boolean;
    preparationTimeMinutes: number;
    tags?: string[];
  };
  onPress?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  showActions?: boolean;
}

export const MenuItemCard: React.FC<MenuItemCardProps> = ({
  item,
  onPress,
  onEdit,
  onDelete,
  showActions = false,
}) => {
  return (
    <Card style={styles.menuItemCard} onPress={onPress}>
      <CardHeader
        title={item.name}
        subtitle={`${item.category} • ${item.preparationTimeMinutes} min`}
        rightComponent={
          <View style={styles.statusIndicator}>
            <View
              style={[
                styles.statusDot,
                { backgroundColor: item.isAvailable ? colors.success : colors.error },
              ]}
            />
          </View>
        }
      />
      <CardContent>
        <Text style={styles.description} numberOfLines={2}>
          {item.description}
        </Text>
        <Text style={styles.price}>₹{item.price.toFixed(2)}</Text>
        {item.tags && item.tags.length > 0 && (
          <View style={styles.tagsContainer}>
            {item.tags.slice(0, 3).map((tag, index) => (
              <View key={index} style={styles.tag}>
                <Text style={styles.tagText}>{tag}</Text>
              </View>
            ))}
          </View>
        )}
      </CardContent>
      {showActions && (
        <CardFooter>
          <View style={styles.actionButtons}>
            {onEdit && (
              <Text style={styles.actionButton} onPress={onEdit}>
                Edit
              </Text>
            )}
            {onDelete && (
              <Text style={[styles.actionButton, styles.deleteButton]} onPress={onDelete}>
                Delete
              </Text>
            )}
          </View>
        </CardFooter>
      )}
    </Card>
  );
};

interface OrderCardProps {
  order: {
    orderId: string;
    customerName: string;
    customerPhone?: string;
    items: Array<{
      name: string;
      quantity: number;
      unitPrice: number;
    }>;
    totalAmount: number;
    status: string;
    createdAt: string;
    estimatedDeliveryTime?: string;
  };
  onPress?: () => void;
  onUpdateStatus?: () => void;
}

export const OrderCard: React.FC<OrderCardProps> = ({
  order,
  onPress,
  onUpdateStatus,
}) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return colors.warning;
      case 'CONFIRMED':
        return colors.info;
      case 'PREPARING':
        return colors.primary;
      case 'READY':
        return colors.success;
      case 'OUT_FOR_DELIVERY':
        return colors.secondary;
      case 'DELIVERED':
        return colors.success;
      case 'CANCELLED':
        return colors.error;
      default:
        return colors.textSecondary;
    }
  };

  return (
    <Card style={styles.orderCard} onPress={onPress}>
      <CardHeader
        title={`Order #${order.orderId}`}
        subtitle={new Date(order.createdAt).toLocaleString()}
        rightComponent={
          <View style={styles.statusBadge}>
            <Text style={[styles.statusText, { color: getStatusColor(order.status) }]}>
              {order.status.replace('_', ' ')}
            </Text>
          </View>
        }
      />
      <CardContent>
        <Text style={styles.customerName}>{order.customerName}</Text>
        <Text style={styles.itemsText}>
          {order.items.length} items • Total: ₹{order.totalAmount.toFixed(2)}
        </Text>
      </CardContent>
      {onUpdateStatus && (
        <CardFooter>
          <Text style={styles.updateButton} onPress={onUpdateStatus}>
            Update Status
          </Text>
        </CardFooter>
      )}
    </Card>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: spacing.md,
    marginBottom: spacing.sm,
  },
  shadow: {
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.sm,
  },
  headerContent: {
    flex: 1,
  },
  title: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs / 2,
  },
  subtitle: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  content: {
    flex: 1,
  },
  footer: {
    borderTopWidth: 1,
    borderTopColor: colors.border,
    paddingTop: spacing.sm,
  },
  statusIndicator: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  menuItemCard: {
    marginBottom: spacing.md,
  },
  description: {
    fontSize: 14,
    color: colors.textSecondary,
    marginBottom: spacing.sm,
    lineHeight: 20,
  },
  price: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.primary,
    marginBottom: spacing.sm,
  },
  tagsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs,
  },
  tag: {
    backgroundColor: colors.surfaceSecondary,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs / 2,
    borderRadius: spacing.xs,
  },
  tagText: {
    fontSize: 12,
    color: colors.textSecondary,
  },
  actionButtons: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: spacing.md,
  },
  actionButton: {
    fontSize: 14,
    color: colors.primary,
    fontWeight: '500',
  },
  deleteButton: {
    color: colors.error,
  },
  orderCard: {
    marginBottom: spacing.md,
  },
  customerName: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  itemsText: {
    fontSize: 14,
    color: colors.textSecondary,
    marginBottom: spacing.sm,
  },
  statusBadge: {
    backgroundColor: colors.surfaceSecondary,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs / 2,
    borderRadius: spacing.xs,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
  },
  updateButton: {
    fontSize: 14,
    color: colors.primary,
    fontWeight: '500',
    alignSelf: 'flex-end',
  },
});

export default Card;