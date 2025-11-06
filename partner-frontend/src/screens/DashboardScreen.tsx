import React, { useState } from 'react';
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
import { useNavigation } from '@react-navigation/native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
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
});

export default function DashboardScreen() {
  const [addItemModalVisible, setAddItemModalVisible] = useState(false);
  const [updateHoursModalVisible, setUpdateHoursModalVisible] = useState(false);
  const [viewReportsModalVisible, setViewReportsModalVisible] = useState(false);
  
  const navigation = useNavigation();
  
  // Feature flag hooks
  const { flags, isEnabled } = useFeatureFlags();

  const quickStats = [
    {
      title: "Today's Revenue",
      value: '₹12,450',
      change: '+12.5%',
      icon: 'cash' as const,
      color: '#10b981',
    },
    {
      title: 'Orders Today',
      value: '47',
      change: '+8.2%',
      icon: 'receipt' as const,
      color: '#3b82f6',
    },
    {
      title: 'Avg. Order Value',
      value: '₹264',
      change: '-2.1%',
      icon: 'trending-up' as const,
      color: '#f59e0b',
    },
    {
      title: 'Active Items',
      value: '23',
      change: '2 new',
      icon: 'restaurant' as const,
      color: '#8b5cf6',
    },
  ];

  const handleAddItem = () => {
    setAddItemModalVisible(true);
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

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <LinearGradient
          colors={['#16a34a', '#15803d']}
          style={styles.headerGradient}
        >
          <View style={styles.headerContent}>
            <View style={styles.headerTextContainer}>
              <Text style={styles.headerTitle}>
                Good Morning, Chef! 👋
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
                    stat.change.startsWith('+') ? styles.statChangePositive : styles.statChangeNegative
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

          {/* AI Insights - Gated by analytics feature */}
          <FeatureGate feature="analytics">
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
          </FeatureGate>

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

          {/* Recent Orders */}
          <View style={styles.card}>
            <View style={styles.ordersHeader}>
              <Text style={styles.ordersTitle}>Recent Orders</Text>
              <TouchableOpacity>
                <Text style={styles.viewAllText}>View All</Text>
              </TouchableOpacity>
            </View>

            <View>
              {[
                { id: '#1234', customer: 'Rahul S.', items: 3, total: '₹420', status: 'preparing' },
                { id: '#1235', customer: 'Priya M.', items: 2, total: '₹290', status: 'ready' },
              ].map((order, index) => (
                <View key={index} style={styles.orderItem}>
                  <View style={styles.orderInfo}>
                    <Text style={styles.orderId}>{order.id} - {order.customer}</Text>
                    <Text style={styles.orderDetails}>{order.items} items</Text>
                  </View>
                  <View style={{ alignItems: 'flex-end' }}>
                    <Text style={styles.orderTotal}>{order.total}</Text>
                    <View style={[
                      styles.statusBadge,
                      order.status === 'preparing' ? styles.statusPreparing : styles.statusReady
                    ]}>
                      <Text style={[
                        order.status === 'preparing' ? styles.statusTextPreparing : styles.statusTextReady
                      ]}>
                        {order.status}
                      </Text>
                    </View>
                  </View>
                </View>
              ))}
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Add Item Modal */}
      <Modal visible={addItemModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setAddItemModalVisible(false)} accessibilityLabel="Close Add Item Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Add New Item</Text>
              <View />
            </View>
            <Text style={styles.cardTitle}>This would open the add item form.</Text>
            <TouchableOpacity style={styles.actionButton} onPress={() => Alert.alert('Mock', 'Navigate to MenuScreen Add Item')} accessibilityLabel="Go to Add Item">
              <Text style={styles.actionButtonText}>Go to Menu</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

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
    </SafeAreaView>
  );
}