import { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  RefreshControl,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchOrders,
  fetchDashboardStats,
  fetchTopItems,
  clearError,
} from '../store/slices/ordersSlice';
import { RootState } from '../store';
import { Order } from '../core/types/api';
import ScreenLayout, { Section } from '../core/components/ScreenLayout';
import { Card, OrderCard } from '../core/components/Card';
import { Button } from '../core/components/Button';
import { LoadingSpinner, ListSkeleton } from '../core/components/LoadingSpinner';
import { colors, spacing } from '../core/constants';

// Mock current branch ID - in real app this would come from auth or context
const CURRENT_BRANCH_ID = 1;

const DashboardScreenImproved = () => {
  const dispatch = useDispatch();
  const { orders, dashboardStats, isLoading, error } = useSelector((state: RootState) => state.orders);

  // Local state
  const [refreshing, setRefreshing] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState('today');

  // Load data on component mount
  useEffect(() => {
    loadDashboardData();
  }, [selectedPeriod]);

  // Load dashboard data
  const loadDashboardData = async () => {
    try {
      await Promise.all([
        dispatch(fetchOrders({ branchId: CURRENT_BRANCH_ID, size: 5 })).unwrap(),
        dispatch(fetchDashboardStats({ branchId: CURRENT_BRANCH_ID, dateRange: selectedPeriod })).unwrap(),
        dispatch(fetchTopItems({ branchId: CURRENT_BRANCH_ID, period: 'month', limit: 5 })).unwrap(),
      ]);
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    }
  };

  // Handle refresh
  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await loadDashboardData();
    } finally {
      setRefreshing(false);
    }
  };

  // Format currency
  const formatCurrency = (amount: number) => {
    return `₹${amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  // Render stat card
  const renderStatCard = (
    title: string,
    value: string,
    change?: number,
    icon?: keyof typeof Ionicons.glyphMap,
    color?: string
  ) => (
    <Card style={styles.statCard}>
      <View style={styles.statHeader}>
        <View style={styles.statIconContainer}>
          <Ionicons 
            name={icon || 'stats-chart-outline'} 
            size={24} 
            color={color || colors.primary} 
          />
        </View>
        {change !== undefined && (
          <View style={[
            styles.changeContainer,
            change >= 0 ? styles.changePositive : styles.changeNegative
          ]}>
            <Ionicons 
              name={change >= 0 ? 'trending-up' : 'trending-down'} 
              size={16} 
              color={change >= 0 ? colors.success : colors.error} 
            />
            <Text style={[
              styles.changeText,
              change >= 0 ? styles.changePositiveText : styles.changeNegativeText
            ]}>
              {Math.abs(change)}%
            </Text>
          </View>
        )}
      </View>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statTitle}>{title}</Text>
    </Card>
  );

  // Render recent orders
  const renderRecentOrders = () => {
    if (!orders.length) {
      return (
        <View style={styles.emptyContainer}>
          <Ionicons name="receipt-outline" size={48} color={colors.textMuted} />
          <Text style={styles.emptyText}>No recent orders</Text>
        </View>
      );
    }

    return orders.slice(0, 5).map((order) => (
      <OrderCard
        key={order.orderId}
        order={order}
        onPress={() => {
          // Navigate to order details
        }}
      />
    ));
  };

  // Render top items
  const renderTopItems = () => {
    const topItems = dashboardStats?.topItems || [];
    
    if (!topItems.length) {
      return (
        <View style={styles.emptyContainer}>
          <Ionicons name="star-outline" size={48} color={colors.textMuted} />
          <Text style={styles.emptyText}>No top items yet</Text>
        </View>
      );
    }

    return topItems.map((item: any, index: number) => (
      <Card key={index} style={styles.topItemCard}>
        <View style={styles.topItemContent}>
          <View style={styles.topItemRank}>
            <Text style={styles.rankText}>{index + 1}</Text>
          </View>
          <View style={styles.topItemInfo}>
            <Text style={styles.topItemName}>{item.name}</Text>
            <Text style={styles.topItemStats}>
              {item.orders} orders • {formatCurrency(item.revenue)}
            </Text>
          </View>
        </View>
      </Card>
    ));
  };

  // Period selector
  const renderPeriodSelector = () => (
    <View style={styles.periodSelector}>
      {['today', 'week', 'month'].map((period) => (
        <TouchableOpacity
          key={period}
          style={[
            styles.periodButton,
            selectedPeriod === period && styles.periodButtonActive,
          ]}
          onPress={() => setSelectedPeriod(period)}
        >
          <Text
            style={[
              styles.periodButtonText,
              selectedPeriod === period && styles.periodButtonTextActive,
            ]}
          >
            {period.charAt(0).toUpperCase() + period.slice(1)}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  if (isLoading && !refreshing) {
    return (
      <ScreenLayout loading>
        <ListSkeleton count={3} />
      </ScreenLayout>
    );
  }

  return (
    <ScreenLayout
      title="Dashboard"
      error={error}
      onRetry={loadDashboardData}
    >
      <ScrollView
        style={styles.container}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />
        }
        showsVerticalScrollIndicator={false}
      >
        {/* Period Selector */}
        <Section>
          {renderPeriodSelector()}
        </Section>

        {/* Stats Cards */}
        <Section title="Overview">
          <View style={styles.statsGrid}>
            {renderStatCard(
              'Revenue',
              formatCurrency(dashboardStats?.revenue?.today || 0),
              dashboardStats?.revenue?.growth || 0,
              'cash-outline',
              colors.success
            )}
            {renderStatCard(
              'Orders',
              (dashboardStats?.orders?.today || 0).toString(),
              dashboardStats?.orders?.growth || 0,
              'receipt-outline',
              colors.primary
            )}
            {renderStatCard(
              'Avg Order Value',
              formatCurrency(dashboardStats?.avgOrderValue?.today || 0),
              dashboardStats?.avgOrderValue?.growth || 0,
              'cart-outline',
              colors.secondary
            )}
            {renderStatCard(
              'Active Items',
              (dashboardStats?.activeItems || 0).toString(),
              undefined,
              'restaurant-outline',
              colors.info
            )}
          </View>
        </Section>

        {/* Quick Actions */}
        <Section title="Quick Actions">
          <View style={styles.quickActions}>
            <Button
              title="New Order"
              icon="add-circle-outline"
              style={styles.quickActionButton}
            />
            <Button
              title="View Orders"
              variant="outline"
              icon="list-outline"
              style={styles.quickActionButton}
            />
            <Button
              title="Menu"
              variant="outline"
              icon="restaurant-outline"
              style={styles.quickActionButton}
            />
          </View>
        </Section>

        {/* Recent Orders */}
        <Section title="Recent Orders" subtitle="Latest orders from your restaurant">
          {renderRecentOrders()}
        </Section>

        {/* Top Items */}
        <Section title="Top Items" subtitle="Most popular items this month">
          {renderTopItems()}
        </Section>
      </ScrollView>
    </ScreenLayout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  periodSelector: {
    flexDirection: 'row',
    backgroundColor: colors.surfaceSecondary,
    borderRadius: spacing.md,
    padding: spacing.xs,
  },
  periodButton: {
    flex: 1,
    paddingVertical: spacing.sm,
    alignItems: 'center',
    borderRadius: spacing.sm,
  },
  periodButtonActive: {
    backgroundColor: colors.primary,
  },
  periodButtonText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.textSecondary,
  },
  periodButtonTextActive: {
    color: colors.textWhite,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.md,
  },
  statCard: {
    flex: 1,
    minWidth: '45%',
    alignItems: 'center',
    padding: spacing.md,
  },
  statHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
    marginBottom: spacing.sm,
  },
  statIconContainer: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.surfaceSecondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  changeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  changePositive: {
    backgroundColor: colors.successLight,
    paddingHorizontal: spacing.xs,
    paddingVertical: spacing.xs / 2,
    borderRadius: spacing.xs,
  },
  changeNegative: {
    backgroundColor: colors.errorLight,
    paddingHorizontal: spacing.xs,
    paddingVertical: spacing.xs / 2,
    borderRadius: spacing.xs,
  },
  changeText: {
    fontSize: 12,
    fontWeight: '600',
    marginLeft: spacing.xs / 2,
  },
  changePositiveText: {
    color: colors.success,
  },
  changeNegativeText: {
    color: colors.error,
  },
  statValue: {
    fontSize: 24,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  statTitle: {
    fontSize: 12,
    color: colors.textSecondary,
    textAlign: 'center',
  },
  quickActions: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  quickActionButton: {
    flex: 1,
  },
  emptyContainer: {
    alignItems: 'center',
    padding: spacing.xl,
  },
  emptyText: {
    fontSize: 16,
    color: colors.textSecondary,
    marginTop: spacing.sm,
  },
  topItemCard: {
    marginBottom: spacing.sm,
  },
  topItemContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  topItemRank: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  rankText: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.textWhite,
  },
  topItemInfo: {
    flex: 1,
  },
  topItemName: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  topItemStats: {
    fontSize: 14,
    color: colors.textSecondary,
  },
});

export default DashboardScreenImproved;