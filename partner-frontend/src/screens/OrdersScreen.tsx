import { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Modal,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useDispatch } from 'react-redux';
import { updateOrderStatus } from '../store/slices/ordersSlice';
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
});

export default function OrdersScreen() {
  const [selectedFilter, setSelectedFilter] = useState('all');
  const [detailsModalVisible, setDetailsModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<any>(null);
  
  // Feature flag hooks
  const { isEnabled } = useFeatureFlags();
  
  const dispatch = useDispatch();

  const filters = [
    { key: 'all', label: 'All' },
    { key: 'new', label: 'New' },
    { key: 'preparing', label: 'Preparing' },
    { key: 'ready', label: 'Ready' },
    { key: 'out for delivery', label: 'Out for Delivery' },
    { key: 'delivered', label: 'Delivered' },
  ];

  const orders = [
    {
      id: '#1234',
      customer: 'Rahul S.',
      items: 3,
      total: '₹420',
      status: 'preparing',
      time: '2 min ago',
    },
    {
      id: '#1235',
      customer: 'Priya M.',
      items: 2,
      total: '₹290',
      status: 'ready',
      time: '5 min ago',
    },
  ];

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
      default:
        return { badge: styles.statusNew, text: styles.statusTextNew };
    }
  };

  const handleUpdateStatus = (orderId: string, newStatus: string) => {
    dispatch(updateOrderStatus({ orderId, status: newStatus as any }));
  };

  const handleViewDetails = (order: any) => {
    setSelectedOrder(order);
    setDetailsModalVisible(true);
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
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
            {filters.map((filter) => (
              <TouchableOpacity
                key={filter.key}
                style={[
                  styles.filterButton,
                  selectedFilter === filter.key && styles.filterButtonActive,
                ]}
                onPress={() => setSelectedFilter(filter.key)}
              >
                <Text style={[
                  styles.filterText,
                  selectedFilter === filter.key && styles.filterTextActive,
                ]}>
                  {filter.label}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* Orders List */}
        <View style={styles.content}>
          <View style={styles.ordersContainer}>
            {orders.map((order) => {
              const statusStyle = getStatusStyle(order.status);
              return (
                <View key={order.id} style={styles.orderCard}>
                  <View style={styles.orderHeader}>
                    <View style={styles.orderInfo}>
                      <Text style={styles.orderId}>
                        {order.id}
                      </Text>
                      <Text style={styles.orderCustomer}>{order.customer}</Text>
                      <Text style={styles.orderDetails}>{order.items} items • {order.time}</Text>
                    </View>
                    <View style={styles.orderMeta}>
                      <Text style={styles.orderTotal}>
                        {order.total}
                      </Text>
                      <View style={[styles.statusBadge, statusStyle.badge]}>
                        <Text style={statusStyle.text}>
                          {order.status}
                        </Text>
                      </View>
                    </View>
                  </View>

                  <View style={styles.orderActions}>
                    {order.status === 'new' && (
                      <TouchableOpacity
                        style={[styles.actionButton, styles.actionButtonPrimary]}
                        onPress={() => handleUpdateStatus(order.id, 'preparing')}
                      >
                        <Text style={styles.actionButtonText}>Accept</Text>
                      </TouchableOpacity>
                    )}
                    {order.status === 'preparing' && (
                      <TouchableOpacity
                        style={[styles.actionButton, styles.actionButtonPrimary]}
                        onPress={() => handleUpdateStatus(order.id, 'ready')}
                      >
                        <Text style={styles.actionButtonText}>Mark Ready</Text>
                      </TouchableOpacity>
                    )}
                    {order.status === 'ready' && (
                      <TouchableOpacity
                        style={[styles.actionButton, styles.actionButtonPrimary]}
                        onPress={() => handleUpdateStatus(order.id, 'out for delivery')}
                      >
                        <Text style={styles.actionButtonText}>Out for Delivery</Text>
                      </TouchableOpacity>
                    )}
                    {order.status === 'out for delivery' && (
                      <TouchableOpacity
                        style={[styles.actionButton, styles.actionButtonPrimary]}
                        onPress={() => handleUpdateStatus(order.id, 'delivered')}
                      >
                        <Text style={styles.actionButtonText}>Mark Delivered</Text>
                      </TouchableOpacity>
                    )}
                    
                    {/* Voice Orders - Beta Feature */}
                    <FeatureGate feature="voiceOrders">
                      <TouchableOpacity style={[styles.actionButton, styles.actionButtonSecondary]}>
                        <Ionicons name="mic" size={16} color="#374151" />
                      </TouchableOpacity>
                    </FeatureGate>
                    
                    <TouchableOpacity style={[styles.actionButton, styles.actionButtonSecondary]} onPress={() => handleViewDetails(order)} accessibilityLabel="View Order Details">
                      <Text style={styles.actionButtonTextSecondary}>Details</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              );
            })}
          </View>
        </View>
      </ScrollView>

      {/* Order Details Modal */}
      <Modal visible={detailsModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => { setDetailsModalVisible(false); setSelectedOrder(null); }} accessibilityLabel="Close Order Details Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Order Details</Text>
              <View />
            </View>
            {selectedOrder && (
              <View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Order ID</Text>
                  <Text style={styles.detailValue}>{selectedOrder.id}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Customer</Text>
                  <Text style={styles.detailValue}>{selectedOrder.customer}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Items</Text>
                  <Text style={styles.detailValue}>{selectedOrder.items}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Total</Text>
                  <Text style={styles.detailValue}>{selectedOrder.total}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Status</Text>
                  <Text style={styles.detailValue}>{selectedOrder.status}</Text>
                </View>
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Time</Text>
                  <Text style={styles.detailValue}>{selectedOrder.time}</Text>
                </View>
              </View>
            )}
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}