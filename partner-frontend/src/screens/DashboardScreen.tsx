import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Modal,
  Alert,
  RefreshControl,
  Switch,
  TextInput,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import FeatureGate from '../core/components/FeatureGate';
import useFeatureFlags from '../core/hooks/useFeatureFlags';
import { useAppDispatch, useAppSelector } from '../store';
import { fetchOrders } from '../store/slices/ordersSlice';
import { createBranch, toggleBranchStatus } from '../store/slices/restaurantSlice';
import { OrderState, Branch, BranchCreateRequest } from '../core/types/api';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  scrollView: {
    flex: 1,
  },
  headerGradient: {
    paddingHorizontal: 24,
    paddingVertical: 32,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  headerTextContainer: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 4,
  },
  headerSubtitle: {
    color: 'rgba(255, 255, 255, 0.9)',
    fontSize: 16,
  },
  notificationButton: {
    width: 48,
    height: 48,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  statsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    backgroundColor: 'white',
    paddingHorizontal: 24,
    paddingVertical: 16,
  },
  statCard: {
    width: '48%',
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  statTextContainer: {
    flex: 1,
  },
  statTitle: {
    color: '#6b7280',
    fontSize: 14,
    marginBottom: 4,
  },
  statValue: {
    color: '#111827',
    fontSize: 20,
    fontWeight: 'bold',
  },
  statChange: {
    fontSize: 14,
    marginTop: 4,
  },
  statChangePositive: {
    color: '#10b981',
  },
  statChangeNegative: {
    color: '#ef4444',
  },
  statIcon: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    paddingHorizontal: 24,
    paddingVertical: 24,
  },
  card: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 24,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  cardIcon: {
    width: 40,
    height: 40,
    backgroundColor: '#dcfce7',
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  cardTitleContainer: {
    flex: 1,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  cardSubtitle: {
    color: '#6b7280',
    fontSize: 14,
  },
  insightItem: {
    backgroundColor: '#f9fafb',
    borderRadius: 8,
    padding: 16,
  },
  insightTitle: {
    fontWeight: '500',
    color: '#111827',
    marginBottom: 4,
  },
  insightText: {
    color: '#6b7280',
    fontSize: 14,
  },
  actionButton: {
    backgroundColor: '#16a34a',
    borderRadius: 8,
    paddingVertical: 16,
    paddingHorizontal: 16,
    marginBottom: 12,
    alignItems: 'center',
  },
  actionButtonSecondary: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingVertical: 16,
    paddingHorizontal: 16,
    marginBottom: 12,
    alignItems: 'center',
  },
  actionButtonText: {
    color: 'white',
    fontWeight: '600',
  },
  actionButtonTextSecondary: {
    color: '#374151',
  },
  ordersHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  ordersTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  viewAllText: {
    color: '#16a34a',
    fontSize: 16,
  },
  orderItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  orderInfo: {
    flex: 1,
  },
  orderId: {
    fontWeight: '500',
    color: '#111827',
  },
  orderDetails: {
    color: '#6b7280',
    fontSize: 14,
  },
  orderTotal: {
    fontWeight: '500',
    color: '#111827',
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusPreparing: {
    backgroundColor: '#fed7aa',
  },
  statusReady: {
    backgroundColor: '#bbf7d0',
  },
  statusTextPreparing: {
    color: '#ea580c',
    fontSize: 12,
  },
  statusTextReady: {
    color: '#166534',
    fontSize: 12,
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
  // Branch Management Styles
  branchesHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  branchItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  branchInfo: {
    flex: 1,
  },
  branchName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  branchLocation: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 2,
  },
  branchStatusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
  statusDotOnline: {
    backgroundColor: '#10b981',
  },
  statusDotOffline: {
    backgroundColor: '#9ca3af',
  },
  addBranchButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f0fdf4',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
  },
  addBranchButtonText: {
    color: '#16a34a',
    fontWeight: '600',
    marginLeft: 4,
  },
  noBranchesText: {
    color: '#6b7280',
    textAlign: 'center',
    padding: 20,
  },
  // Modal form styles
  modalScrollContent: {
    maxHeight: 400,
  },
  formGroup: {
    marginBottom: 16,
  },
  formLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: '#374151',
    marginBottom: 6,
  },
  formInput: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 16,
    color: '#111827',
    backgroundColor: '#f9fafb',
  },
  formRow: {
    flexDirection: 'row',
    gap: 12,
  },
  formHalf: {
    flex: 1,
  },
  submitButton: {
    backgroundColor: '#16a34a',
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 8,
  },
  submitButtonDisabled: {
    backgroundColor: '#9ca3af',
  },
  submitButtonText: {
    color: 'white',
    fontWeight: '600',
    fontSize: 16,
  },
});

export default function DashboardScreen() {
  const [addItemModalVisible, setAddItemModalVisible] = useState(false);
  const [updateHoursModalVisible, setUpdateHoursModalVisible] = useState(false);
  const [viewReportsModalVisible, setViewReportsModalVisible] = useState(false);
  const [createBranchModalVisible, setCreateBranchModalVisible] = useState(false);
  const [branchFormData, setBranchFormData] = useState({
    branchName: '',
    branchPhone: '',
    branchEmail: '',
    city: '',
    area: '',
    state: '',
    pincode: '',
  });
  const [isCreatingBranch, setIsCreatingBranch] = useState(false);

  const navigation = useNavigation();
  const dispatch = useAppDispatch();

  // Feature flag hooks
  const { flags, isEnabled } = useFeatureFlags();

  // Redux state
  const { restaurant, branchStatusLoading } = useAppSelector(state => state.restaurant);
  const { orders, isLoading } = useAppSelector(state => state.orders);

  useEffect(() => {
    // Fetch orders on mount
    dispatch(fetchOrders());
  }, [dispatch]);

  const onRefresh = React.useCallback(() => {
    dispatch(fetchOrders());
  }, [dispatch]);

  // Calculate stats
  const todayOrders = orders.filter(order => {
    const orderDate = new Date(order.createdAt);
    const today = new Date();
    return orderDate.getDate() === today.getDate() &&
      orderDate.getMonth() === today.getMonth() &&
      orderDate.getFullYear() === today.getFullYear();
  });

  const revenue = todayOrders.reduce((sum, order) => {
    if (order.state !== OrderState.CANCELLED && order.state !== OrderState.REJECTED) {
      return sum + order.totalAmount;
    }
    return sum;
  }, 0);

  const activeOrders = orders.filter(order =>
    order.state !== OrderState.DELIVERED &&
    order.state !== OrderState.CANCELLED &&
    order.state !== OrderState.REJECTED &&
    order.state !== OrderState.CLOSED
  ).length;

  const quickStats = [
    {
      title: "Today's Revenue",
      value: `₹${revenue.toFixed(0)}`,
      change: 'Today',
      icon: 'cash' as const,
      color: '#10b981',
    },
    {
      title: 'Orders Today',
      value: todayOrders.length.toString(),
      change: 'Today',
      icon: 'receipt' as const,
      color: '#3b82f6',
    },
    {
      title: 'Avg. Order Value',
      value: `₹${todayOrders.length > 0 ? (revenue / todayOrders.length).toFixed(0) : 0}`,
      change: 'Today',
      icon: 'trending-up' as const,
      color: '#f59e0b',
    },
    {
      title: 'Active Orders',
      value: activeOrders.toString(),
      change: 'Now',
      icon: 'restaurant' as const,
      color: '#8b5cf6',
    },
  ];

  const handleAddItem = () => {
    // Navigate to Menu screen
    navigation.navigate('Menu' as never);
  };

  const handleUpdateHours = () => {
    setUpdateHoursModalVisible(true);
  };

  const handleViewReports = () => {
    setViewReportsModalVisible(true);
  };

  const handleTestUploads = () => {
    navigation.navigate('UploadTest' as never);
  };

  const handleToggleBranchStatus = async (branchId: number, currentStatus: boolean) => {
    try {
      await dispatch(toggleBranchStatus({ branchId, isOpen: !currentStatus })).unwrap();
    } catch (error: any) {
      Alert.alert('Error', error.message || 'Failed to update branch status');
    }
  };

  const handleCreateBranch = async () => {
    if (!branchFormData.branchName || !branchFormData.branchPhone || !branchFormData.branchEmail || !branchFormData.city) {
      Alert.alert('Error', 'Please fill in all required fields');
      return;
    }

    if (!restaurant?.vendorId) {
      Alert.alert('Error', 'Vendor ID not found. Please complete onboarding first.');
      return;
    }

    setIsCreatingBranch(true);
    try {
      // Default Bangalore center coordinates - branches without location won't show in customer search
      const DEFAULT_BANGALORE_LAT = 12.9716;
      const DEFAULT_BANGALORE_LNG = 77.5946;

      const branchData: BranchCreateRequest = {
        branchName: branchFormData.branchName,
        branchPhone: branchFormData.branchPhone,
        branchEmail: branchFormData.branchEmail,
        city: branchFormData.city,
        latitude: DEFAULT_BANGALORE_LAT,
        longitude: DEFAULT_BANGALORE_LNG,
        address: {
          area: branchFormData.area,
          city: branchFormData.city,
          state: branchFormData.state,
          pincode: branchFormData.pincode,
        },
      };

      const result = await dispatch(createBranch({ vendorId: restaurant.vendorId, branchData })).unwrap();

      // Activate the branch immediately after creation
      if (result?.branchId) {
        try {
          const { vendorApiService } = await import('../core/api/vendorApiService');
          await vendorApiService.activateBranch(result.branchId);
          console.log('[Dashboard] Branch', result.branchId, 'activated successfully');
        } catch (activateError) {
          console.warn('[Dashboard] Failed to activate branch:', activateError);
          // Continue anyway - branch was created
        }
      }

      Alert.alert('Success', 'Branch created and activated!');
      setCreateBranchModalVisible(false);
      setBranchFormData({
        branchName: '',
        branchPhone: '',
        branchEmail: '',
        city: '',
        area: '',
        state: '',
        pincode: '',
      });

      // Refresh the restaurant data to get updated branches
      const { hydrateRestaurant } = await import('../store/slices/restaurantSlice');
      dispatch(hydrateRestaurant());
    } catch (error: any) {
      Alert.alert('Error', error.message || 'Failed to create branch');
    } finally {
      setIsCreatingBranch(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={isLoading} onRefresh={onRefresh} />
        }
      >
        {/* Header */}
        <LinearGradient
          colors={['#16a34a', '#15803d']}
          style={styles.headerGradient}
        >
          <View style={styles.headerContent}>
            <View style={styles.headerTextContainer}>
              <Text style={styles.headerTitle}>
                Good Morning, {restaurant?.name || 'Chef'}! 👋
              </Text>
              <Text style={styles.headerSubtitle}>
                Here's what's happening at your restaurant today
              </Text>
            </View>
            <TouchableOpacity style={styles.notificationButton}>
              <Ionicons name="notifications" size={24} color="white" />
            </TouchableOpacity>
          </View>
        </LinearGradient>

        {/* Quick Stats */}
        <View style={styles.statsContainer}>
          {quickStats.map((stat, index) => (
            <TouchableOpacity
              key={index}
              style={styles.statCard}
            >
              <View style={styles.statContent}>
                <View style={styles.statTextContainer}>
                  <Text style={styles.statTitle}>{stat.title}</Text>
                  <Text style={styles.statValue}>{stat.value}</Text>
                  <Text style={[
                    styles.statChange,
                    styles.statChangePositive
                  ]}>
                    {stat.change}
                  </Text>
                </View>
                <View
                  style={[
                    styles.statIcon,
                    { backgroundColor: `${stat.color}20` }
                  ]}
                >
                  <Ionicons name={stat.icon} size={20} color={stat.color} />
                </View>
              </View>
            </TouchableOpacity>
          ))}
        </View>

        {/* Content */}
        <View style={styles.content}>
          {/* AI Insights */}
          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <View style={styles.cardIcon}>
                <Ionicons name="bulb" size={20} color="#16a34a" />
              </View>
              <View style={styles.cardTitleContainer}>
                <Text style={styles.cardTitle}>AI Insights</Text>
                <Text style={styles.cardSubtitle}>Personalized recommendations</Text>
              </View>
            </View>

            <View>
              <View style={styles.insightItem}>
                <Text style={styles.insightTitle}>Peak Hour Analysis</Text>
                <Text style={styles.insightText}>
                  Your busiest time is 7-9 PM. Consider offering express combos.
                </Text>
              </View>

              <View style={[styles.insightItem, { marginTop: 12 }]}>
                <Text style={styles.insightTitle}>Top Performer</Text>
                <Text style={styles.insightText}>
                  Paneer Tikka Roll is your star dish this week!
                </Text>
              </View>
            </View>
          </View>

          {/* Quick Actions */}
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Quick Actions</Text>
            <TouchableOpacity style={styles.actionButton} onPress={handleAddItem} accessibilityLabel="Add New Item">
              <Text style={styles.actionButtonText}>Add New Item</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionButtonSecondary} onPress={handleUpdateHours} accessibilityLabel="Update Hours">
              <Text style={styles.actionButtonTextSecondary}>Update Hours</Text>
            </TouchableOpacity>
            <FeatureGate feature="analytics">
              <TouchableOpacity style={styles.actionButtonSecondary} onPress={handleViewReports} accessibilityLabel="View Reports">
                <Text style={styles.actionButtonTextSecondary}>View Reports</Text>
              </TouchableOpacity>
            </FeatureGate>
            <FeatureGate feature="imageUpload">
              <TouchableOpacity style={styles.actionButtonSecondary} onPress={handleTestUploads} accessibilityLabel="Test Upload Functions">
                <Text style={styles.actionButtonTextSecondary}>Test Upload Functions</Text>
              </TouchableOpacity>
            </FeatureGate>
          </View>

          {/* Your Branches */}
          <View style={styles.card}>
            <View style={styles.branchesHeader}>
              <Text style={styles.cardTitle}>Your Branches</Text>
              <TouchableOpacity
                style={styles.addBranchButton}
                onPress={() => setCreateBranchModalVisible(true)}
                accessibilityLabel="Add Branch"
              >
                <Ionicons name="add" size={18} color="#16a34a" />
                <Text style={styles.addBranchButtonText}>Add Branch</Text>
              </TouchableOpacity>
            </View>

            {(!restaurant?.branches || restaurant.branches.length === 0) ? (
              <Text style={styles.noBranchesText}>
                No branches yet. Add your first branch to get started.
              </Text>
            ) : (
              restaurant.branches.map((branch) => (
                <View key={branch.branchId} style={styles.branchItem}>
                  <View style={styles.branchInfo}>
                    <Text style={styles.branchName}>{branch.branchName}</Text>
                    <Text style={styles.branchLocation}>
                      {branch.address?.area
                        ? `${branch.address.area}, ${branch.city}`
                        : branch.city || 'No location set'}
                    </Text>
                  </View>
                  <View style={styles.branchStatusContainer}>
                    <View
                      style={[
                        styles.statusDot,
                        branch.isOpen ? styles.statusDotOnline : styles.statusDotOffline,
                      ]}
                    />
                    <Switch
                      value={branch.isOpen}
                      onValueChange={() => handleToggleBranchStatus(branch.branchId, branch.isOpen)}
                      trackColor={{ false: '#d1d5db', true: '#86efac' }}
                      thumbColor={branch.isOpen ? '#16a34a' : '#9ca3af'}
                      disabled={branchStatusLoading}
                    />
                  </View>
                </View>
              ))
            )}
          </View>

          {/* Recent Orders */}
          <View style={styles.card}>
            <View style={styles.ordersHeader}>
              <Text style={styles.ordersTitle}>Recent Orders</Text>
              <TouchableOpacity onPress={() => navigation.navigate('Orders' as never)}>
                <Text style={styles.viewAllText}>View All</Text>
              </TouchableOpacity>
            </View>

            <View>
              {orders.length === 0 ? (
                <Text style={{ color: '#6b7280', textAlign: 'center', padding: 20 }}>
                  No orders yet.
                </Text>
              ) : (
                orders.slice(0, 5).map((order, index) => (
                  <View key={index} style={styles.orderItem}>
                    <View style={styles.orderInfo}>
                      <Text style={styles.orderId}>#{order.orderId.substring(0, 8)}</Text>
                      <Text style={styles.orderDetails}>{order.items.length} items</Text>
                    </View>
                    <View style={{ alignItems: 'flex-end' }}>
                      <Text style={styles.orderTotal}>₹{order.totalAmount}</Text>
                      <View style={[
                        styles.statusBadge,
                        order.state === OrderState.PREPARING ? styles.statusPreparing : styles.statusReady
                      ]}>
                        <Text style={[
                          order.state === OrderState.PREPARING ? styles.statusTextPreparing : styles.statusTextReady
                        ]}>
                          {order.state}
                        </Text>
                      </View>
                    </View>
                  </View>
                ))
              )}
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Update Hours Modal */}
      <Modal visible={updateHoursModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setUpdateHoursModalVisible(false)} accessibilityLabel="Close Update Hours Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Update Hours</Text>
              <View />
            </View>
            <Text style={styles.cardTitle}>This would open the hours update form.</Text>
            <TouchableOpacity style={styles.actionButton} onPress={() => Alert.alert('Mock', 'Update hours functionality')} accessibilityLabel="Update Hours">
              <Text style={styles.actionButtonText}>Update Hours</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* View Reports Modal */}
      <Modal visible={viewReportsModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setViewReportsModalVisible(false)} accessibilityLabel="Close View Reports Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>View Reports</Text>
              <View />
            </View>
            <Text style={styles.cardTitle}>Mock reports data would be displayed here.</Text>
            <TouchableOpacity style={styles.actionButton} onPress={() => Alert.alert('Mock', 'View reports functionality')} accessibilityLabel="View Reports">
              <Text style={styles.actionButtonText}>View Reports</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Create Branch Modal */}
      <Modal visible={createBranchModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setCreateBranchModalVisible(false)} accessibilityLabel="Close Create Branch Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Add New Branch</Text>
              <View />
            </View>

            <ScrollView style={styles.modalScrollContent} showsVerticalScrollIndicator={false}>
              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Branch Name *</Text>
                <TextInput
                  style={styles.formInput}
                  placeholder="e.g., Downtown Branch"
                  value={branchFormData.branchName}
                  onChangeText={(text) => setBranchFormData({ ...branchFormData, branchName: text })}
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Phone Number *</Text>
                <TextInput
                  style={styles.formInput}
                  placeholder="e.g., 9876543210"
                  keyboardType="phone-pad"
                  value={branchFormData.branchPhone}
                  onChangeText={(text) => setBranchFormData({ ...branchFormData, branchPhone: text })}
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Email *</Text>
                <TextInput
                  style={styles.formInput}
                  placeholder="e.g., branch@example.com"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  value={branchFormData.branchEmail}
                  onChangeText={(text) => setBranchFormData({ ...branchFormData, branchEmail: text })}
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>City *</Text>
                <TextInput
                  style={styles.formInput}
                  placeholder="e.g., Mumbai"
                  value={branchFormData.city}
                  onChangeText={(text) => setBranchFormData({ ...branchFormData, city: text })}
                />
              </View>

              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>Area</Text>
                <TextInput
                  style={styles.formInput}
                  placeholder="e.g., Andheri West"
                  value={branchFormData.area}
                  onChangeText={(text) => setBranchFormData({ ...branchFormData, area: text })}
                />
              </View>

              <View style={[styles.formRow, { marginBottom: 16 }]}>
                <View style={styles.formHalf}>
                  <Text style={styles.formLabel}>State</Text>
                  <TextInput
                    style={styles.formInput}
                    placeholder="e.g., Maharashtra"
                    value={branchFormData.state}
                    onChangeText={(text) => setBranchFormData({ ...branchFormData, state: text })}
                  />
                </View>
                <View style={styles.formHalf}>
                  <Text style={styles.formLabel}>Pincode</Text>
                  <TextInput
                    style={styles.formInput}
                    placeholder="e.g., 400053"
                    keyboardType="numeric"
                    value={branchFormData.pincode}
                    onChangeText={(text) => setBranchFormData({ ...branchFormData, pincode: text })}
                  />
                </View>
              </View>
            </ScrollView>

            <TouchableOpacity
              style={[styles.submitButton, isCreatingBranch && styles.submitButtonDisabled]}
              onPress={handleCreateBranch}
              disabled={isCreatingBranch}
              accessibilityLabel="Create Branch"
            >
              <Text style={styles.submitButtonText}>
                {isCreatingBranch ? 'Creating...' : 'Create Branch'}
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>

  );
}