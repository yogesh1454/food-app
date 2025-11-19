import { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  RefreshControl,
  TouchableOpacity,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchOrders,
  updateOrderStatus,
  setFilter,
  clearError,
} from '../store/slices/ordersSlice';
import { RootState } from '../store';
import { Order } from '../core/types/api';
import ScreenLayout, { Section, EmptyState } from '../core/components/ScreenLayout';
import { Card, OrderCard } from '../core/components/Card';
import { Button } from '../core/components/Button';
import { LoadingSpinner, ListSkeleton } from '../core/components/LoadingSpinner';
import { colors, spacing } from '../core/constants';

// Mock current branch ID - in real app this would come from auth or context
const CURRENT_BRANCH_ID = 1;

const OrdersScreenImproved = () => {
  const dispatch = useDispatch();
  const { orders, isLoading, error, filter } = useSelector((state: RootState) => state.orders);

  // Local state
  const [refreshing, setRefreshing] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [showStatusModal, setShowStatusModal] = useState(false);

  // Status options
  const statusOptions = [
    { key: 'all', label: 'All Orders', icon: 'list-outline' },
    { key: 'PENDING', label: 'Pending', icon: 'time-outline' },
    { key: 'CONFIRMED', label: 'Confirmed', icon: 'checkmark-circle-outline' },
    { key: 'PREPARING', label: 'Preparing', icon: 'restaurant-outline' },
    { key: 'READY', label: 'Ready', icon: 'checkmark-done-outline' },
    { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: 'bicycle-outline' },
    { key: 'DELIVERED', label: 'Delivered', icon: 'home-outline' },
    { key: 'CANCELLED', label: 'Cancelled', icon: 'close-circle-outline' },
  ];

  // Load data on component mount
  useEffect(() => {
    loadOrders();
  }, [filter]);

  // Load orders
  const loadOrders = async () => {
    try {
      await dispatch(fetchOrders({
        branchId: CURRENT_BRANCH_ID,
        status: filter === 'all' ? undefined : filter,
        size: 50,
      })).unwrap();
    } catch (error) {
      console.error('Failed to load orders:', error);
    }
  };

  // Handle refresh
  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await loadOrders();
    } finally {
      setRefreshing(false);
    }
  };

  // Handle filter change
  const handleFilterChange = (newFilter: string) => {
    dispatch(setFilter(newFilter as any));
  };

  // Handle order status update
  const handleStatusUpdate = async (order: Order, newStatus: Order['status']) => {
    try {
      await dispatch(updateOrderStatus({
        orderId: order.orderId,
        statusData: { status: newStatus },
      })).unwrap();
      setShowStatusModal(false);
      setSelectedOrder(null);
    } catch (error) {
      console.error('Failed to update order status:', error);
    }
  };

  // Get next status options for an order
  const getNextStatusOptions = (currentStatus: Order['status']) => {
    const statusFlow: Record<string, Order['status'][]> = {
      'PENDING': ['CONFIRMED', 'CANCELLED'],
      'CONFIRMED': ['PREPARING', 'CANCELLED'],
      'PREPARING': ['READY'],
      'READY': ['OUT_FOR_DELIVERY'],
      'OUT_FOR_DELIVERY': ['DELIVERED'],
      'DELIVERED': [],
      'CANCELLED': [],
    };

    return statusFlow[currentStatus] || [];
  };

  // Render status filter
  const renderStatusFilter = () => (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      style={styles.filterContainer}
      contentContainerStyle={styles.filterContent}
    >
      {statusOptions.map((option) => (
        <TouchableOpacity
          key={option.key}
          style={[
            styles.filterButton,
            filter === option.key && styles.filterButtonActive,
          ]}
          onPress={() => handleFilterChange(option.key)}
        >
          <Ionicons
            name={option.icon}
            size={16}
            color={filter === option.key ? colors.textWhite : colors.textSecondary}
            style={styles.filterIcon}
          />
          <Text
            style={[
              styles.filterText,
              filter === option.key && styles.filterTextActive,
            ]}
          >
            {option.label}
          </Text>
        </TouchableOpacity>
      ))}
    </ScrollView>
  );

  // Render status update modal
  const renderStatusModal = () => {
    if (!selectedOrder) return null;

    const nextStatuses = getNextStatusOptions(selectedOrder.status);

    return (
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Update Order Status</Text>
            <TouchableOpacity
              style={styles.closeButton}
              onPress={() => {
                setShowStatusModal(false);
                setSelectedOrder(null);
              }}
            >
              <Ionicons name="close" size={24} color={colors.text} />
            </TouchableOpacity>
          </View>

          <Text style={styles.orderInfo}>
            Order #{selectedOrder.orderId} - {selectedOrder.customerName}
          </Text>

          <View style={styles.statusOptions}>
            {nextStatuses.map((status) => (
              <TouchableOpacity
                key={status}
                style={styles.statusOption}
                onPress={() => handleStatusUpdate(selectedOrder, status)}
              >
                <Text style={styles.statusOptionText}>
                  {status.replace('_', ' ')}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      </View>
    );
  };

  // Render orders
  const renderOrders = () => {
    if (!orders.length) {
      return (
        <EmptyState
          title="No orders found"
          subtitle={
            filter === 'all'
              ? 'You haven\'t received any orders yet'
              : `No ${filter.toLowerCase()} orders`
          }
          icon="receipt-outline"
        />
      );
    }

    return orders.map((order) => (
      <OrderCard
        key={order.orderId}
        order={order}
        onPress={() => {
          // Navigate to order details
        }}
        onUpdateStatus={() => {
          if (getNextStatusOptions(order.status).length > 0) {
            setSelectedOrder(order);
            setShowStatusModal(true);
          }
        }}
      />
    ));
  };

  if (isLoading && !refreshing) {
    return (
      <ScreenLayout loading>
        <ListSkeleton count={5} />
      </ScreenLayout>
    );
  }

  return (
    <ScreenLayout
      title="Orders"
      error={error}
      onRetry={loadOrders}
    >
      <ScrollView
        style={styles.container}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />
        }
        showsVerticalScrollIndicator={false}
      >
        {/* Status Filter */}
        <Section>
          {renderStatusFilter()}
        </Section>

        {/* Orders List */}
        <Section>
          {renderOrders()}
        </Section>
      </ScrollView>

      {/* Status Update Modal */}
      {showStatusModal && renderStatusModal()}
    </ScreenLayout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  filterContainer: {
    marginBottom: spacing.md,
  },
  filterContent: {
    paddingHorizontal: spacing.md,
  },
  filterButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surfaceSecondary,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: spacing.lg,
    marginRight: spacing.sm,
  },
  filterButtonActive: {
    backgroundColor: colors.primary,
  },
  filterIcon: {
    marginRight: spacing.xs,
  },
  filterText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.textSecondary,
  },
  filterTextActive: {
    color: colors.textWhite,
  },
  modalOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  modalContent: {
    backgroundColor: colors.surface,
    borderRadius: spacing.lg,
    padding: spacing.lg,
    margin: spacing.md,
    width: '100%',
    maxWidth: 400,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.text,
  },
  closeButton: {
    padding: spacing.xs,
  },
  orderInfo: {
    fontSize: 16,
    color: colors.textSecondary,
    marginBottom: spacing.lg,
    textAlign: 'center',
  },
  statusOptions: {
    gap: spacing.sm,
  },
  statusOption: {
    backgroundColor: colors.primary,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.lg,
    borderRadius: spacing.md,
    alignItems: 'center',
  },
  statusOptionText: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.textWhite,
  },
});

export default OrdersScreenImproved;