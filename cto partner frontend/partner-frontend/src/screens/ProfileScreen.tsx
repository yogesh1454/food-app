import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Alert,
  TextInput,
  Modal,
  Image,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { addStaff, updateStaff, removeStaff, updateRestaurant } from '../store/slices/restaurantSlice';
import ImageUploadButton from '../core/components/ImageUploadButton';
import DocumentUploadButton from '../core/components/DocumentUploadButton';
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
  profileSection: {
    backgroundColor: 'white',
    marginHorizontal: 24,
    marginTop: 24,
    borderRadius: 12,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  profileContent: {
    alignItems: 'center',
  },
  profileImageContainer: {
    alignItems: 'center',
    marginBottom: 16,
  },
  profileIcon: {
    width: 80,
    height: 80,
    backgroundColor: '#16a34a',
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  profileName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  profileStatus: {
    color: '#6b7280',
    fontSize: 16,
    marginTop: 4,
  },
  editButton: {
    marginTop: 16,
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 8,
  },
  editButtonText: {
    color: '#374151',
  },
  menuContainer: {
    marginHorizontal: 24,
    marginTop: 24,
  },
  menuCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingVertical: 16,
  },
  menuItemWithBorder: {
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  menuIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 16,
    backgroundColor: '#f3f4f6',
  },
  menuIconDanger: {
    backgroundColor: '#fee2e2',
  },
  menuContent: {
    flex: 1,
  },
  menuTitle: {
    fontWeight: '500',
    color: '#111827',
  },
  menuTitleDanger: {
    color: '#dc2626',
  },
  menuSubtitle: {
    color: '#6b7280',
    fontSize: 14,
    marginTop: 2,
  },
  menuArrow: {
    marginLeft: 8,
  },
  appInfo: {
    marginHorizontal: 24,
    marginTop: 24,
    marginBottom: 32,
  },
  appInfoCard: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
    alignItems: 'center',
  },
  appInfoText: {
    color: '#6b7280',
    fontSize: 14,
  },
  content: {
    paddingHorizontal: 24,
    paddingVertical: 24,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: 16,
  },
  staffCard: {
    backgroundColor: 'white',
    borderRadius: 8,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  staffInfo: {
    flex: 1,
  },
  staffName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  staffRole: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 4,
  },
  staffContact: {
    fontSize: 12,
    color: '#9ca3af',
    marginTop: 2,
  },
  staffActions: {
    flexDirection: 'row',
    gap: 16,
  },
  input: {
    backgroundColor: 'white',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 16,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    color: '#111827',
  },
  button: {
    backgroundColor: '#16a34a',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 16,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
  profileModalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  profileModalContent: {
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
  profileModalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  profileModalTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
  },
  profileInput: {
    backgroundColor: '#f9fafb',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    marginBottom: 16,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    color: '#111827',
  },
  profileButton: {
    backgroundColor: '#16a34a',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 16,
  },
  profileButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default function ProfileScreen() {
  const [staffModalVisible, setStaffModalVisible] = useState(false);
  const [editingStaff, setEditingStaff] = useState<any>(null);
  const [staffForm, setStaffForm] = useState({ name: '', role: '', phone: '', email: '' });
  const [editProfileModalVisible, setEditProfileModalVisible] = useState(false);
  const [profileForm, setProfileForm] = useState({ name: '', description: '', address: '', phone: '', email: '' });
  const [documentsModalVisible, setDocumentsModalVisible] = useState(false);
  const [uploadedDocuments, setUploadedDocuments] = useState<Array<{ name: string; uri: string }>>([]);
  
  // Feature flag hooks
  const { flags, isEnabled } = useFeatureFlags();
  
  const restaurant = useSelector((state: RootState) => state.restaurant.restaurant);
  const dispatch = useDispatch();

  const handleStaffManagement = () => {
    setStaffModalVisible(true);
  };

  const handleAddStaff = () => {
    if (staffForm.name && staffForm.role && staffForm.phone && staffForm.email) {
      const newStaff = {
        id: Date.now().toString(),
        name: staffForm.name,
        role: staffForm.role,
        phone: staffForm.phone,
        email: staffForm.email,
      };
      dispatch(addStaff(newStaff));
      setStaffForm({ name: '', role: '', phone: '', email: '' });
      setStaffModalVisible(false);
    } else {
      Alert.alert('Error', 'Please fill all fields');
    }
  };

  const handleEditStaff = (staff: any) => {
    setEditingStaff(staff);
    setStaffForm({ name: staff.name, role: staff.role, phone: staff.phone, email: staff.email });
    setStaffModalVisible(true);
  };

  const handleUpdateStaff = () => {
    if (editingStaff && staffForm.name && staffForm.role && staffForm.phone && staffForm.email) {
      dispatch(updateStaff({ id: editingStaff.id, updates: staffForm }));
      setStaffForm({ name: '', role: '', phone: '', email: '' });
      setEditingStaff(null);
      setStaffModalVisible(false);
    } else {
      Alert.alert('Error', 'Please fill all fields');
    }
  };

  const handleRemoveStaff = (id: string) => {
    Alert.alert(
      'Confirm',
      'Are you sure you want to remove this staff member?',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Remove', onPress: () => dispatch(removeStaff(id)) },
      ]
    );
  };

  const handleCloseModal = () => {
    setStaffModalVisible(false);
    setEditingStaff(null);
    setStaffForm({ name: '', role: '', phone: '', email: '' });
  };

  const handleEditProfile = () => {
    if (restaurant) {
      setProfileForm({
        name: restaurant.name,
        description: restaurant.description,
        address: restaurant.address,
        phone: restaurant.phone,
        email: restaurant.email,
      });
      setEditProfileModalVisible(true);
    }
  };

  const handleUpdateProfile = () => {
    if (restaurant) {
      dispatch(updateRestaurant({
        name: profileForm.name,
        description: profileForm.description,
        address: profileForm.address,
        phone: profileForm.phone,
        email: profileForm.email,
      }));
      setEditProfileModalVisible(false);
      Alert.alert('Success', 'Profile updated successfully!');
    }
  };

  const menuItems = [
    {
      title: 'Restaurant Info',
      subtitle: 'Update your restaurant details',
      icon: 'restaurant' as const,
      action: () => {},
    },
    {
      title: 'Operating Hours',
      subtitle: 'Set your business hours',
      icon: 'time' as const,
      action: () => {},
    },
    {
      title: 'Staff Management',
      subtitle: 'View, add, edit, and remove staff members',
      icon: 'people' as const,
      action: () => handleStaffManagement(),
    },
    {
      title: 'Documents',
      subtitle: 'Upload licenses, certificates, and other documents',
      icon: 'document-text' as const,
      action: () => setDocumentsModalVisible(true),
    },
    {
      title: 'Payment Settings',
      subtitle: 'Configure payment methods',
      icon: 'card' as const,
      action: () => {},
    },
    {
      title: 'Notifications',
      subtitle: 'Manage notification preferences',
      icon: 'notifications' as const,
      action: () => {},
    },
    {
      title: 'Support',
      subtitle: 'Get help and contact support',
      icon: 'help-circle' as const,
      action: () => {},
    },
    {
      title: 'Logout',
      subtitle: 'Sign out of your account',
      icon: 'log-out' as const,
      action: () => {},
      danger: true,
    },
  ];

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Settings</Text>
          <Text style={styles.headerSubtitle}>Manage your restaurant profile</Text>
        </View>

        {/* Profile Section */}
        <View style={styles.profileSection}>
          <View style={styles.profileContent}>
            <View style={styles.profileImageContainer}>
              <View style={styles.profileIcon}>
                <Ionicons name="restaurant" size={32} color="white" />
              </View>
              <ImageUploadButton
                onImageUploaded={(uri) => {
                  // Handle logo upload
                  if (restaurant) {
                    dispatch(updateRestaurant({ logoUrl: uri }));
                  }
                }}
                buttonText="Change Logo"
                iconName="image-outline"
                style={{ marginTop: 12 }}
              />
            </View>
            <Text style={styles.profileName}>Nashtto Restaurant</Text>
            <Text style={styles.profileStatus}>Premium Partner</Text>
            <TouchableOpacity style={styles.editButton} onPress={handleEditProfile} accessibilityLabel="Edit Profile">
              <Text style={styles.editButtonText}>Edit Profile</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Menu Items */}
        <View style={styles.menuContainer}>
          <View style={styles.menuCard}>
            {menuItems.map((item, index) => (
              <TouchableOpacity
                key={index}
                style={[
                  styles.menuItem,
                  index !== menuItems.length - 1 && styles.menuItemWithBorder,
                ]}
                onPress={item.action}
              >
                <View style={[
                  styles.menuIcon,
                  item.danger && styles.menuIconDanger,
                ]}>
                  <Ionicons
                    name={item.icon}
                    size={20}
                    color={item.danger ? '#ef4444' : '#6b7280'}
                  />
                </View>
                <View style={styles.menuContent}>
                  <Text style={[
                    styles.menuTitle,
                    item.danger && styles.menuTitleDanger,
                  ]}>
                    {item.title}
                  </Text>
                  <Text style={styles.menuSubtitle}>
                    {item.subtitle}
                  </Text>
                </View>
                <Ionicons
                  name="chevron-forward"
                  size={20}
                  color="#9ca3af"
                  style={styles.menuArrow}
                />
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* App Info */}
        <View style={styles.appInfo}>
          <View style={styles.appInfoCard}>
            <Text style={styles.appInfoText}>
              Nashtto Partner App v1.0.0
            </Text>
            <Text style={[styles.appInfoText, { marginTop: 4 }]}>
              © 2024 Nashtto. All rights reserved.
            </Text>
          </View>
        </View>
      </ScrollView>

      {/* Staff Management Modal */}
      <Modal visible={staffModalVisible} animationType="slide">
        <SafeAreaView style={styles.container}>
          <View style={styles.header}>
            <TouchableOpacity onPress={handleCloseModal}>
              <Ionicons name="close" size={24} color="#111827" />
            </TouchableOpacity>
            <Text style={styles.headerTitle}>
              {editingStaff ? 'Edit Staff' : 'Staff Management'}
            </Text>
            <View />
          </View>
          <ScrollView style={styles.scrollView}>
            <View style={styles.content}>
              <Text style={styles.title}>Staff Members</Text>
              {restaurant?.staff.map((staff) => (
                <View key={staff.id} style={styles.staffCard}>
                  <View style={styles.staffInfo}>
                    <Text style={styles.staffName}>{staff.name}</Text>
                    <Text style={styles.staffRole}>{staff.role}</Text>
                    <Text style={styles.staffContact}>{staff.phone} • {staff.email}</Text>
                  </View>
                  <View style={styles.staffActions}>
                    <TouchableOpacity onPress={() => handleEditStaff(staff)}>
                      <Ionicons name="pencil" size={20} color="#16a34a" />
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => handleRemoveStaff(staff.id)}>
                      <Ionicons name="trash" size={20} color="#ef4444" />
                    </TouchableOpacity>
                  </View>
                </View>
              ))}
              <Text style={styles.title}>Add New Staff</Text>
              <TextInput
                style={styles.input}
                placeholder="Enter staff member's full name"
                placeholderTextColor="#6b7280"
                value={staffForm.name}
                onChangeText={(text) => setStaffForm({ ...staffForm, name: text })}
              />
              <TextInput
                style={styles.input}
                placeholder="Enter staff role (e.g., Manager, Chef, Waiter)"
                placeholderTextColor="#6b7280"
                value={staffForm.role}
                onChangeText={(text) => setStaffForm({ ...staffForm, role: text })}
              />
              <TextInput
                style={styles.input}
                placeholder="Phone"
                placeholderTextColor="#6b7280"
                value={staffForm.phone}
                onChangeText={(text) => setStaffForm({ ...staffForm, phone: text })}
                keyboardType="phone-pad"
              />
              <TextInput
                style={styles.input}
                placeholder="Email"
                placeholderTextColor="#6b7280"
                value={staffForm.email}
                onChangeText={(text) => setStaffForm({ ...staffForm, email: text })}
                keyboardType="email-address"
              />
              <TouchableOpacity
                style={styles.button}
                onPress={editingStaff ? handleUpdateStaff : handleAddStaff}
              >
                <Text style={styles.buttonText}>
                  {editingStaff ? 'Update' : 'Add'} Staff
                </Text>
              </TouchableOpacity>
            </View>
          </ScrollView>
        </SafeAreaView>
      </Modal>

      {/* Edit Profile Modal */}
      <Modal visible={editProfileModalVisible} animationType="slide" transparent={true}>
        <View style={styles.profileModalOverlay}>
          <View style={styles.profileModalContent}>
            <View style={styles.profileModalHeader}>
              <TouchableOpacity onPress={() => setEditProfileModalVisible(false)} accessibilityLabel="Close Edit Profile Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.profileModalTitle}>Edit Profile</Text>
              <View />
            </View>
            <TextInput
              style={styles.profileInput}
              placeholder="Restaurant Name"
              placeholderTextColor="#6b7280"
              value={profileForm.name}
              onChangeText={(text) => setProfileForm({ ...profileForm, name: text })}
              accessibilityLabel="Restaurant Name Input"
            />
            <TextInput
              style={styles.profileInput}
              placeholder="Description"
              placeholderTextColor="#6b7280"
              value={profileForm.description}
              onChangeText={(text) => setProfileForm({ ...profileForm, description: text })}
              accessibilityLabel="Description Input"
            />
            <TextInput
              style={styles.profileInput}
              placeholder="Address"
              placeholderTextColor="#6b7280"
              value={profileForm.address}
              onChangeText={(text) => setProfileForm({ ...profileForm, address: text })}
              accessibilityLabel="Address Input"
            />
            <TextInput
              style={styles.profileInput}
              placeholder="Phone"
              placeholderTextColor="#6b7280"
              value={profileForm.phone}
              onChangeText={(text) => setProfileForm({ ...profileForm, phone: text })}
              keyboardType="phone-pad"
              accessibilityLabel="Phone Input"
            />
            <TextInput
              style={styles.profileInput}
              placeholder="Email"
              placeholderTextColor="#6b7280"
              value={profileForm.email}
              onChangeText={(text) => setProfileForm({ ...profileForm, email: text })}
              keyboardType="email-address"
              accessibilityLabel="Email Input"
            />
            <TouchableOpacity style={styles.profileButton} onPress={handleUpdateProfile} accessibilityLabel="Update Profile">
              <Text style={styles.profileButtonText}>Update Profile</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Documents Modal */}
      <Modal visible={documentsModalVisible} animationType="slide" transparent={true}>
        <View style={styles.profileModalOverlay}>
          <View style={[styles.profileModalContent, { maxHeight: '80%' }]}>
            <View style={styles.profileModalHeader}>
              <TouchableOpacity onPress={() => setDocumentsModalVisible(false)} accessibilityLabel="Close Documents Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.profileModalTitle}>Upload Documents</Text>
              <View />
            </View>
            <ScrollView style={{ flex: 1 }}>
              <Text style={{ fontSize: 16, fontWeight: '600', color: '#111827', marginBottom: 16 }}>
                Upload License Documents
              </Text>
              <DocumentUploadButton
                onDocumentUploaded={(uri, fileName) => {
                  setUploadedDocuments([...uploadedDocuments, { name: fileName, uri }]);
                }}
                buttonText="Upload Document"
                style={{ marginBottom: 16 }}
              />
              
              {uploadedDocuments.length > 0 && (
                <View style={{ marginTop: 16 }}>
                  <Text style={{ fontSize: 16, fontWeight: '600', color: '#111827', marginBottom: 12 }}>
                    Uploaded Documents ({uploadedDocuments.length})
                  </Text>
                  {uploadedDocuments.map((doc, index) => (
                    <View key={index} style={{
                      backgroundColor: '#f9fafb',
                      padding: 12,
                      borderRadius: 8,
                      marginBottom: 8,
                      borderWidth: 1,
                      borderColor: '#e5e7eb',
                    }}>
                      <Text style={{ fontSize: 14, fontWeight: '500', color: '#111827' }}>
                        {doc.name}
                      </Text>
                      <Text style={{ fontSize: 12, color: '#6b7280', marginTop: 4 }}>
                        Stored locally • {doc.uri}
                      </Text>
                    </View>
                  ))}
                </View>
              )}
              
              <TouchableOpacity
                style={[styles.profileButton, { marginTop: 24 }]}
                onPress={() => setDocumentsModalVisible(false)}
              >
                <Text style={styles.profileButtonText}>Done</Text>
              </TouchableOpacity>
            </ScrollView>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}