import { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Modal,
  Alert,
  TextInput,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSelector } from 'react-redux';
import { useAppDispatch } from '../store';
import { RootState } from '../store';
import {
  fetchOrders,
  acceptOrder,
  rejectOrder,
  markOrderReady,
  setFilter,
} from '../store/slices/ordersSlice';
import { OrderResponse } from '../core/types/api';
import FeatureGate from '../core/components/FeatureGate';
import useFeatureFlags from '../core/hooks/useFeatureFlags';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  scrollView: {
    flex: 1,
  },
  header: {
    backgroundColor: 'white',
    paddingHorizontal: 24,
    paddingVertical: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
  },
  headerSubtitle: {
    color: '#6b7280',
    fontSize: 16,
    marginTop: 4,
  },
  filtersContainer: {
    backgroundColor: 'white',
    paddingHorizontal: 24,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  filtersScroll: {
    marginHorizontal: -24,
    paddingHorizontal: 24,
  },
  filtersContent: {
    flexDirection: 'row',
  },
  filterButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    marginRight: 12,
    backgroundColor: '#f3f4f6',
  },
  filterButtonActive: {
    backgroundColor: '#16a34a',
  },
  filterText: {
    fontWeight: '500',
    color: '#374151',
  },
  filterTextActive: {
    color: 'white',
  },
  content: {
    paddingHorizontal: 24,
    paddingVertical: 24,
  },
  ordersContainer: {
    gap: 16,
  },
  orderCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  orderHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  orderInfo: {
    flex: 1,
  },
  orderId: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  orderCustomer: {
    color: '#6b7280',
    fontSize: 16,
  },
  orderDetails: {
    color: '#6b7280',
    fontSize: 14,
  },
  orderMeta: {
    alignItems: 'flex-end',
  },
  orderTotal: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#111827',
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    marginTop: 4,
  },
  statusNew: {
    backgroundColor: '#dbeafe',
  },
  statusPreparing: {
    backgroundColor: '#fed7aa',
  },
  statusReady: {
    backgroundColor: '#bbf7d0',
  },
  statusOutForDelivery: {
    backgroundColor: '#fed7d0',
  },
  statusDelivered: {
    backgroundColor: '#e0e7ff',
  },
  statusRejected: {
    backgroundColor: '#fee2e2',
  },
  statusTextNew: {
    color: '#1d4ed8',
    fontSize: 12,
  },
  statusTextPreparing: {
    color: '#ea580c',
    fontSize: 12,
  },
  statusTextReady: {
    color: '#166534',
    fontSize: 12,
  },
  statusTextOutForDelivery: {
    color: '#dc2626',
    fontSize: 12,
  },
  statusTextDelivered: {
    color: '#7c3aed',
    fontSize: 12,
  },
  statusTextRejected: {
    color: '#dc2626',
    fontSize: 12,
  },
  orderActions: {
    flexDirection: 'row',
    gap: 8,
  },
  actionButton: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 8,
    alignItems: 'center',
  },
  actionButtonPrimary: {
    backgroundColor: '#16a34a',
  },
  actionButtonDanger: {
    backgroundColor: '#dc2626',
  },
  actionButtonSecondary: {
    borderWidth: 1,
    borderColor: '#d1d5db',
  },
  actionButtonText: {
    color: 'white',
    fontWeight: '600',
  },
  actionButtonTextSecondary: {
    color: '#374151',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: 'white',
    borderRadius: 16,
    padding: 24,
    width: '85%',
    maxHeight: '70%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.25,
    shadowRadius: 20,
    elevation: 10,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  detailLabel: {
    fontSize: 16,
    color: '#6b7280',
  },
  detailValue: {
    fontSize: 16,
    color: '#111827',
    fontWeight: '500',
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
  },
  emptyStateText: {
    fontSize: 18,
    color: '#6b7280',
    marginTop: 12,
  },
  emptyStateSubtext: {
    fontSize: 14,
    color: '#9ca3af',
    marginTop: 4,
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    marginBottom: 16,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: '#374151',
    marginBottom: 8,
  },
  button: {
    backgroundColor: '#16a34a',
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonDanger: {
    backgroundColor: '#dc2626',
  },
  buttonText: {
    color: 'white',
    fontWeight: '600',
    fontSize: 16,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 60,
  },
  errorContainer: {
    backgroundColor: '#fee2e2',
    padding: 16,
    margin: 24,
    borderRadius: 8,
  },
  errorText: {
    color: '#dc2626',
    fontSize: 14,
  },
  itemsList: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
  },
  itemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 4,
  },
  itemName: {
    fontSize: 14,
    color: '#374151',
  },
  itemQty: {
    fontSize: 14,
    color: '#6b7280',
  },
});

// Map API order states to UI display states
const mapOrderState = (state: string): string => {
  const stateMap: Record<string, string> = {
    'PENDING_ACCEPTANCE': 'new',
    'ACCEPTED': 'preparing',
    'PREPARING': 'preparing',
    'READY_FOR_PICKUP': 'ready',
    'ASSIGNED_TO_RIDER': 'ready',
    'PICKED_UP': 'out for delivery',
    'DELIVERED': 'delivered',
    'CLOSED': 'delivered',
    'CANCELLED': 'cancelled',
    'REJECTED': 'rejected',
  };
  return stateMap[state] || 'new';
};

// Format time ago
const formatTimeAgo = (dateString: string | undefined): string => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins} min ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours} hr ago`;
  return `${Math.floor(diffHours / 24)} days ago`;
};

export default function OrdersScreen() {
  const [detailsModalVisible, setDetailsModalVisible] = useState(false);
  const [acceptModalVisible, setAcceptModalVisible] = useState(false);
  const [rejectModalVisible, setRejectModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null);
  const [prepTime, setPrepTime] = useState('15');
  const [rejectReason, setRejectReason] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const { isEnabled } = useFeatureFlags();
  const dispatch = useAppDispatch();

  // Redux state
  const orders = useSelector((state: RootState) => state.orders.orders);
  const isLoading = useSelector((state: RootState) => state.orders.isLoading);
  const error = useSelector((state: RootState) => state.orders.error);
  const filter = useSelector((state: RootState) => state.orders.filter);

  const filters = [
    { key: 'all', label: 'All' },
    { key: 'PENDING_ACCEPTANCE', label: 'New' },
    { key: 'PREPARING', label: 'Preparing' },
    { key: 'READY_FOR_PICKUP', label: 'Ready' },
    { key: 'PICKED_UP', label: 'Out for Delivery' },
    { key: 'DELIVERED', label: 'Delivered' },
  ];

  // Fetch orders on mount
  useEffect(() => {
    dispatch(fetchOrders());
  }, [dispatch]);

  // Pull to refresh
  const onRefresh = async () => {
    setRefreshing(true);
    await dispatch(fetchOrders());
    setRefreshing(false);
  };



  // Filter orders based on selected filter
  const filteredOrders = filter === 'all'
    ? orders
    : orders.filter(order => order.state === filter);

  const getStatusStyle = (status: string) => {
    switch (status) {
      case 'new':
        return { badge: styles.statusNew, text: styles.statusTextNew };
      case 'preparing':
        return { badge: styles.statusPreparing, text: styles.statusTextPreparing };
      case 'ready':
        return { badge: styles.statusReady, text: styles.statusTextReady };
      case 'out for delivery':
        return { badge: styles.statusOutForDelivery, text: styles.statusTextOutForDelivery };
      case 'delivered':
        return { badge: styles.statusDelivered, text: styles.statusTextDelivered };
      case 'rejected':
      case 'cancelled':
        return { badge: styles.statusRejected, text: styles.statusTextRejected };
      default:
        return { badge: styles.statusNew, text: styles.statusTextNew };
    }
  };

  const handleAcceptOrder = async () => {
    if (!selectedOrder) return;

    const prepTimeNum = parseInt(prepTime, 10);
    if (isNaN(prepTimeNum) || prepTimeNum < 5 || prepTimeNum > 120) {
      Alert.alert('Invalid Time', 'Prep time must be between 5 and 120 minutes');
      return;
    }

    try {
      await dispatch(acceptOrder({
        orderId: selectedOrder.orderId!,
        estimatedPrepTime: prepTimeNum
      })).unwrap();
      setAcceptModalVisible(false);
      setSelectedOrder(null);
      setPrepTime('15');
      Alert.alert('Success', 'Order accepted! Start preparing.');
      dispatch(fetchOrders());
    } catch (err: any) {
      Alert.alert('Error', err.message || 'Failed to accept order');
    }
  };

  const handleRejectOrder = async () => {
    if (!selectedOrder) return;

    if (!rejectReason.trim()) {
      Alert.alert('Required', 'Please provide a rejection reason');
      return;
    }

    try {
      await dispatch(rejectOrder({
        orderId: selectedOrder.orderId!,
        reason: rejectReason
      })).unwrap();
      setRejectModalVisible(false);
      setSelectedOrder(null);
      setRejectReason('');
      Alert.alert('Order Rejected', 'The order has been rejected.');
      dispatch(fetchOrders());
    } catch (err: any) {
      Alert.alert('Error', err.message || 'Failed to reject order');
    }
  };

  const handleMarkReady = async (order: OrderResponse) => {
    try {
      await dispatch(markOrderReady(order.orderId!)).unwrap();
      Alert.alert('Success', 'Order marked as ready for pickup!');
      dispatch(fetchOrders());
    } catch (err: any) {
      Alert.alert('Error', err.message || 'Failed to mark order ready');
    }
  };

  const handleViewDetails = (order: OrderResponse) => {
    setSelectedOrder(order);
    setDetailsModalVisible(true);
  };

  const openAcceptModal = (order: OrderResponse) => {
    setSelectedOrder(order);
    setAcceptModalVisible(true);
  };

  const openRejectModal = (order: OrderResponse) => {
    setSelectedOrder(order);
    setRejectModalVisible(true);
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Orders</Text>
          <Text style={styles.headerSubtitle}>Manage your customer orders</Text>
        </View>

        {/* Filters */}
        <View style={styles.filtersContainer}>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={styles.filtersScroll}
            contentContainerStyle={styles.filtersContent}
          >
            {filters.map((f) => (
              <TouchableOpacity
                key={f.key}
                style={[
                  styles.filterButton,
                  filter === f.key && styles.filterButtonActive,
                ]}
                onPress={() => dispatch(setFilter(f.key as any))}
              >
                <Text style={[
                  styles.filterText,
                  filter === f.key && styles.filterTextActive,
                ]}>
                  {f.label}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* Error State */}
        {error && (
          <View style={styles.errorContainer}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        )}

        {/* Loading State */}
        {isLoading && !refreshing && (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#16a34a" />
            <Text style={styles.emptyStateSubtext}>Loading orders...</Text>
          </View>
        )}

        {/* Empty State */}
        {!isLoading && filteredOrders.length === 0 && (
          <View style={styles.emptyState}>
            <Ionicons name="receipt-outline" size={48} color="#9ca3af" />
            <Text style={styles.emptyStateText}>No orders found</Text>
            <Text style={styles.emptyStateSubtext}>
              {filter === 'all' ? 'Orders will appear here' : `No ${filter.toLowerCase().replace('_', ' ')} orders`}
            </Text>
          </View>
        )}

        {/* Orders List */}
        {!isLoading && filteredOrders.length > 0 && (
          <View style={styles.content}>
            <View style={styles.ordersContainer}>
              {filteredOrders.map((order) => {
                const displayStatus = mapOrderState(order.state || '');
                const statusStyle = getStatusStyle(displayStatus);
                const itemCount = order.items?.length || 0;
                const totalAmount = order.totalAmount || 0;

                return (
                  <View key={order.orderId} style={styles.orderCard}>
                    <View style={styles.orderHeader}>
                      <View style={styles.orderInfo}>
                        <Text style={styles.orderId}>
                          #{order.orderId?.slice(-6)}
                        </Text>
                        <Text style={styles.orderCustomer}>
                          Customer Order
                        </Text>
                        <Text style={styles.orderDetails}>
                          {itemCount} items • {formatTimeAgo(order.createdAt)}
                        </Text>
                      </View>
                      <View style={styles.orderMeta}>
                        <Text style={styles.orderTotal}>
                          ₹{totalAmount.toFixed(0)}
                        </Text>
                        <View style={[styles.statusBadge, statusStyle.badge]}>
                          <Text style={statusStyle.text}>
                            {displayStatus}
                          </Text>
                        </View>
                      </View>
                    </View>

                    <View style={styles.orderActions}>
                      {/* New Order Actions */}
                      {displayStatus === 'new' && (
                        <>
                          <TouchableOpacity
                            style={[styles.actionButton, styles.actionButtonPrimary]}
                            onPress={() => openAcceptModal(order)}
                          >
                            <Text style={styles.actionButtonText}>Accept</Text>
                          </TouchableOpacity>
                          <TouchableOpacity
                            style={[styles.actionButton, styles.actionButtonDanger]}
                            onPress={() => openRejectModal(order)}
                          >
                            <Text style={styles.actionButtonText}>Reject</Text>
                          </TouchableOpacity>
                        </>
                      )}

                      {/* Preparing Order Actions */}
                      {displayStatus === 'preparing' && (
                        <TouchableOpacity
                          style={[styles.actionButton, styles.actionButtonPrimary]}
                          onPress={() => handleMarkReady(order)}
                        >
                          <Text style={styles.actionButtonText}>Mark Ready</Text>
                        </TouchableOpacity>
                      )}

                      {/* Ready Order - waiting for pickup */}
                      {displayStatus === 'ready' && (
                        <View style={[styles.actionButton, styles.actionButtonSecondary]}>
                          <Text style={styles.actionButtonTextSecondary}>Awaiting Pickup</Text>
                        </View>
                      )}

                      {/* Voice Orders - Beta Feature */}
                      <FeatureGate feature="voiceOrders">
                        <TouchableOpacity style={[styles.actionButton, styles.actionButtonSecondary]}>
                          <Ionicons name="mic" size={16} color="#374151" />
                        </TouchableOpacity>
                      </FeatureGate>

                      <TouchableOpacity
                        style={[styles.actionButton, styles.actionButtonSecondary]}
                        onPress={() => handleViewDetails(order)}
                        accessibilityLabel="View Order Details"
                      >
                        <Text style={styles.actionButtonTextSecondary}>Details</Text>
                      </TouchableOpacity>
                    </View>
                  </View>
                );
              })}
            </View>
          </View>
        )}
      </ScrollView>

      {/* Accept Order Modal */}
      <Modal visible={acceptModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity
                onPress={() => { setAcceptModalVisible(false); setSelectedOrder(null); }}
                accessibilityLabel="Close Accept Modal"
              >
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Accept Order</Text>
              <View />
            </View>
            <Text style={styles.inputLabel}>Estimated Preparation Time (minutes)</Text>
            <TextInput
              style={styles.input}
              value={prepTime}
              onChangeText={setPrepTime}
              keyboardType="numeric"
              placeholder="15"
              placeholderTextColor="#9ca3af"
            />
            <Text style={{ color: '#6b7280', marginBottom: 16, fontSize: 12 }}>
              Enter time between 5-120 minutes
            </Text>
            <TouchableOpacity style={styles.button} onPress={handleAcceptOrder}>
              <Text style={styles.buttonText}>Accept Order</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Reject Order Modal */}
      <Modal visible={rejectModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity
                onPress={() => { setRejectModalVisible(false); setSelectedOrder(null); setRejectReason(''); }}
                accessibilityLabel="Close Reject Modal"
              >
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Reject Order</Text>
              <View />
            </View>
            <Text style={styles.inputLabel}>Rejection Reason</Text>
            <TextInput
              style={[styles.input, { height: 100, textAlignVertical: 'top' }]}
              value={rejectReason}
              onChangeText={setRejectReason}
              placeholder="Enter reason for rejection..."
              placeholderTextColor="#9ca3af"
              multiline
            />
            <TouchableOpacity style={[styles.button, styles.buttonDanger]} onPress={handleRejectOrder}>
              <Text style={styles.buttonText}>Reject Order</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Order Details Modal */}
      <Modal visible={detailsModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity
                onPress={() => { setDetailsModalVisible(false); setSelectedOrder(null); }}
                accessibilityLabel="Close Order Details Modal"
              >
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Order Details</Text>
              <View />
            </View>
            {selectedOrder && (
              <ScrollView showsVerticalScrollIndicator={false}>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Order ID</Text>
                  <Text style={styles.detailValue}>#{selectedOrder.orderId?.slice(-6)}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Status</Text>
                  <Text style={styles.detailValue}>{mapOrderState(selectedOrder.state || '')}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Items</Text>
                  <Text style={styles.detailValue}>{selectedOrder.items?.length || 0}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Total</Text>
                  <Text style={styles.detailValue}>₹{selectedOrder.totalAmount?.toFixed(0) || 0}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Placed At</Text>
                  <Text style={styles.detailValue}>{formatTimeAgo(selectedOrder.createdAt)}</Text>
                </View>

                {/* Order Items */}
                {selectedOrder.items && selectedOrder.items.length > 0 && (
                  <View style={styles.itemsList}>
                    <Text style={[styles.inputLabel, { marginBottom: 8 }]}>Order Items</Text>
                    {selectedOrder.items.map((item, index) => (
                      <View key={index} style={styles.itemRow}>
                        <Text style={styles.itemName}>{item.itemName}</Text>
                        <Text style={styles.itemQty}>x{item.quantity} - ₹{item.priceAtOrder}</Text>
                      </View>
                    ))}
                  </View>
                )}

                {/* Delivery Info */}
                {selectedOrder.deliveryAddress && (
                  <View style={styles.itemsList}>
                    <Text style={[styles.inputLabel, { marginBottom: 8 }]}>Delivery Address</Text>
                    <Text style={styles.itemName}>
                      {selectedOrder.deliveryAddress.addressLine1}
                      {selectedOrder.deliveryAddress.addressLine2 && `, ${selectedOrder.deliveryAddress.addressLine2}`}
                    </Text>
                    <Text style={styles.itemQty}>
                      {selectedOrder.deliveryAddress.city}, {selectedOrder.deliveryAddress.pincode}
                    </Text>
                  </View>
                )}
              </ScrollView>
            )}
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}