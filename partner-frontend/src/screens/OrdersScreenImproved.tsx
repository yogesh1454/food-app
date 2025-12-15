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
  acceptOrder,
  rejectOrder,
  markOrderReady,
  setFilter,
  clearError,
} from '../store/slices/ordersSlice';
import { RootState } from '../store';
import { OrderResponse, OrderState, canAcceptOrder, canMarkOrderReady, getOrderStateDescription } from '../core/api';
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
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
  const [showStatusModal, setShowStatusModal] = useState(false);

  // Status options using new OrderState enum
  const statusOptions = [
    { key: 'all', label: 'All Orders', icon: 'list-outline' },
    { key: OrderState.PENDING_ACCEPTANCE, label: 'Pending', icon: 'time-outline' },
    { key: OrderState.ACCEPTED, label: 'Accepted', icon: 'checkmark-circle-outline' },
    { key: OrderState.PREPARING, label: 'Preparing', icon: 'restaurant-outline' },
    { key: OrderState.READY_FOR_PICKUP, label: 'Ready', icon: 'checkmark-done-outline' },
    { key: OrderState.PICKED_UP, label: 'Out for Delivery', icon: 'bicycle-outline' },
    { key: OrderState.DELIVERED, label: 'Delivered', icon: 'home-outline' },
    { key: OrderState.CANCELLED, label: 'Cancelled', icon: 'close-circle-outline' },
  ];

  // Load data on component mount
  useEffect(() => {
    loadOrders();
  }, [filter]);

  // Load orders
  const loadOrders = async () => {
    try {
      // New fetchOrders doesn't take parameters - it calls vendor orders API
      await dispatch(fetchOrders()).unwrap();
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

  // Handle accept order
  const handleAcceptOrder = async (order: OrderResponse) => {
    try {
      await dispatch(acceptOrder({
        orderId: order.orderId,
        estimatedPrepTime: 15, // Default 15 minutes
      })).unwrap();
      setShowStatusModal(false);
      setSelectedOrder(null);
    } catch (error) {
      console.error('Failed to accept order:', error);
    }
  };

  // Handle reject order
  const handleRejectOrder = async (order: OrderResponse) => {
    try {
      await dispatch(rejectOrder({
        orderId: order.orderId,
        reason: 'Not available',
      })).unwrap();
      setShowStatusModal(false);
      setSelectedOrder(null);
    } catch (error) {
      console.error('Failed to reject order:', error);
    }
  };

  // Handle mark order ready
  const handleMarkReady = async (order: OrderResponse) => {
    try {
      await dispatch(markOrderReady(order.orderId)).unwrap();
      setShowStatusModal(false);
      setSelectedOrder(null);
    } catch (error) {
      console.error('Failed to mark order ready:', error);
    }
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

  // Render action modal
  const renderActionModal = () => {
    if (!selectedOrder) return null;

    const canAccept = canAcceptOrder(selectedOrder.state);
    const canMarkReady = canMarkOrderReady(selectedOrder.state);

    return (
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Order Actions</Text>
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
            Order #{selectedOrder.orderId}
          </Text>
          <Text style={styles.orderState}>
            {getOrderStateDescription(selectedOrder.state)}
          </Text>

          <View style={styles.statusOptions}>
            {canAccept && (
              <>
                <TouchableOpacity
                  style={[styles.statusOption, styles.acceptButton]}
                  onPress={() => handleAcceptOrder(selectedOrder)}
                >
                  <Text style={styles.statusOptionText}>Accept Order (15 min)</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.statusOption, styles.rejectButton]}
                  onPress={() => handleRejectOrder(selectedOrder)}
                >
                  <Text style={styles.statusOptionText}>Reject Order</Text>
                </TouchableOpacity>
              </>
            )}
            {canMarkReady && (
              <TouchableOpacity
                style={styles.statusOption}
                onPress={() => handleMarkReady(selectedOrder)}
              >
                <Text style={styles.statusOptionText}>Mark Ready for Pickup</Text>
              </TouchableOpacity>
            )}
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
          // Show actions if order can be accepted or marked ready
          if (canAcceptOrder(order.state) || canMarkOrderReady(order.state)) {
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

      {/* Action Modal */}
      {showStatusModal && renderActionModal()}
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
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
    textAlign: 'center',
  },
  orderState: {
    fontSize: 14,
    color: colors.textSecondary,
    marginBottom: spacing.lg,
    textAlign: 'center',
  },
  acceptButton: {
    backgroundColor: colors.success,
  },
  rejectButton: {
    backgroundColor: colors.error,
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