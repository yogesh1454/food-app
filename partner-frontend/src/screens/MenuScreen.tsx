import { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  TextInput,
  StyleSheet,
  Modal,
  Alert,
  Image,
  FlatList,
  SafeAreaView,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Ionicons } from '@expo/vector-icons';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchMenuItems,
  fetchMenuItemCategories,
  createMenuItem,
  updateMenuItem,
  deleteMenuItem,
  setSelectedCategory,
  clearError,
  setCurrentBranchId,
} from '../store/slices/menuSlice';
import { RootState, AppDispatch, useAppDispatch } from '../store';
import { MenuItem, MenuItemCreateRequest } from '../core/types/api';
import ScreenLayout, { Section, EmptyState } from '../core/components/ScreenLayout';
import { Card, MenuItemCard } from '../core/components/Card';
import { Button } from '../core/components/Button';
import { TextInputField } from '../core/components/TextInputField';
import { LoadingSpinner } from '../core/components/LoadingSpinner';
import { ErrorHandler, useErrorHandler } from '../core/components/ErrorHandler';
import { colors, spacing } from '../core/constants';
import { apiService } from '../core/api/unifiedApiService';
import { menuApiService } from '../core/api/menuApiService';
import * as ImagePicker from 'expo-image-picker';
import useFeatureFlags from '../core/hooks/useFeatureFlags';
import FeatureGate from '../core/components/FeatureGate';
import ImageUploadButton from '../core/components/ImageUploadButton';

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
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#111827',
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  searchIcon: {
    marginRight: 12,
  },
  searchInput: {
    flex: 1,
    color: '#111827',
    fontSize: 16,
  },
  categoriesContainer: {
    backgroundColor: 'white',
    paddingHorizontal: 24,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  categoriesScroll: {
    marginHorizontal: -24,
    paddingHorizontal: 24,
  },
  categoriesContent: {
    flexDirection: 'row',
  },
  categoryButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    marginRight: 12,
    backgroundColor: '#f3f4f6',
  },
  categoryButtonActive: {
    backgroundColor: '#16a34a',
  },
  categoryText: {
    fontWeight: '500',
    color: '#374151',
  },
  categoryTextActive: {
    color: 'white',
  },
  content: {
    paddingHorizontal: 24,
    paddingVertical: 24,
  },
  menuItemsContainer: {
    gap: 16,
  },
  menuItem: {
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 2,
  },
  menuItemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  menuItemInfo: {
    flex: 1,
  },
  menuItemName: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  menuItemPrice: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#16a34a',
  },
  menuItemActions: {
    flexDirection: 'row',
    gap: 8,
  },
  actionButton: {
    padding: 8,
  },
  menuItemFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  menuItemDetails: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  vegIndicator: {
    width: 16,
    height: 16,
    backgroundColor: '#22c55e',
    borderRadius: 8,
  },
  itemCategoryText: {
    fontSize: 14,
    color: '#6b7280',
  },
  availabilityBadge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  availableBadge: {
    backgroundColor: '#dcfce7',
  },
  unavailableBadge: {
    backgroundColor: '#fee2e2',
  },
  availableText: {
    color: '#166534',
    fontSize: 14,
  },
  unavailableText: {
    color: '#dc2626',
    fontSize: 14,
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
    maxHeight: '80%',
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
  input: {
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
  optionButton: {
    paddingVertical: 16,
    paddingHorizontal: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  optionText: {
    fontSize: 16,
    color: '#111827',
    fontWeight: '500',
  },
  fab: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    backgroundColor: '#16a34a',
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
  branchSelectorContainer: {
    marginBottom: 16,
  },
  branchSelectorLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: '#374151',
    marginBottom: 8,
  },
  branchOption: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    marginBottom: 8,
    backgroundColor: '#fff',
  },
  branchOptionSelected: {
    borderColor: '#16a34a',
    backgroundColor: '#f0fdf4',
  },
  branchOptionIcon: {
    marginRight: 12,
  },
  branchOptionContent: {
    flex: 1,
  },
  branchOptionName: {
    fontSize: 14,
    fontWeight: '500',
    color: '#111827',
  },
  branchOptionCity: {
    fontSize: 12,
    color: '#6b7280',
  },
  branchOptionCheck: {
    marginLeft: 8,
  },
  headerBranchSelector: {
    marginTop: 12,
  },
  headerBranchPicker: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f0fdf4',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: '#16a34a',
  },
  headerBranchPickerText: {
    flex: 1,
    fontSize: 14,
    fontWeight: '500',
    color: '#16a34a',
    marginLeft: 8,
  },
  branchModalContent: {
    backgroundColor: 'white',
    borderRadius: 16,
    padding: 16,
    width: '85%',
    maxHeight: '60%',
  },
});

export default function MenuScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [optionsModalVisible, setOptionsModalVisible] = useState(false);
  const [branchPickerVisible, setBranchPickerVisible] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [newItem, setNewItem] = useState({
    name: '',
    price: '',
    category: 'Main Course',
    isAvailable: true,
    isVegetarian: true,
    addons: '',
    complimentaryItems: '',
    imageUrl: '',
    nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 },
    selectedBranchId: null as number | null,
  });

  // Feature flag hooks
  const { flags, isEnabled, loading } = useFeatureFlags();
  const dispatch = useAppDispatch();
  const menuItems = useSelector((state: RootState) => state.menu.items);
  const categories = useSelector((state: RootState) => state.menu.categories);
  const currentBranchId = useSelector((state: RootState) => state.menu.currentBranchId);
  const restaurant = useSelector((state: RootState) => state.restaurant.restaurant);
  const branches = useSelector((state: RootState) => state.restaurant.restaurant?.branches || []);

  const filteredItems = selectedCategory === 'All' ? menuItems : menuItems.filter(item => item.category === selectedCategory);

  useEffect(() => {
    const initializeMenu = async () => {
      // If we already have a branchId set in Redux, use it (don't reset user's selection)
      if (currentBranchId) {
        dispatch(fetchMenuItems({ branchId: currentBranchId }));
        return;
      }

      // Get stored branchId from AsyncStorage (only for initial load)
      const storedBranchId = await AsyncStorage.getItem('branchId');
      let branchIdToUse = storedBranchId ? parseInt(storedBranchId, 10) : null;

      // Fallback to restaurant state branchId
      if (!branchIdToUse && restaurant?.branchId) {
        branchIdToUse = restaurant.branchId;
      }

      // If still no branchId, show error - don't use hardcoded fallback
      if (!branchIdToUse) {
        console.warn('[Menu] No branchId found - please complete onboarding first');
        Alert.alert(
          'Setup Required',
          'Please complete the onboarding process to create your restaurant branch first.',
          [{ text: 'OK' }]
        );
        return;
      }

      dispatch(setCurrentBranchId(branchIdToUse));
      dispatch(fetchMenuItems({ branchId: branchIdToUse }));
    };

    initializeMenu();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, restaurant?.branchId]); // Removed currentBranchId to prevent resetting user's selection

  const handleImageUpload = async (uri: string) => {
    setNewItem({ ...newItem, imageUrl: uri });
  };

  const handleNutritionAnalysis = async () => {
    // Mock AI nutrition analysis
    Alert.alert('Nutrition Analysis', 'Analyzing image with AI...');
    // Simulate analysis
    setTimeout(() => {
      const mockNutrition = {
        calories: Math.floor(Math.random() * 500) + 100,
        protein: Math.floor(Math.random() * 50) + 5,
        carbs: Math.floor(Math.random() * 100) + 10,
        fat: Math.floor(Math.random() * 50) + 5,
      };
      setNewItem({ ...newItem, nutritionInfo: mockNutrition });
      Alert.alert('Analysis Complete', `Calories: ${mockNutrition.calories}, Protein: ${mockNutrition.protein}g, Carbs: ${mockNutrition.carbs}g, Fat: ${mockNutrition.fat}g`);
    }, 2000);
  };

  // Pick image from gallery
  const handlePickImage = async () => {
    const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();

    if (!permissionResult.granted) {
      Alert.alert('Permission Required', 'Please allow access to your photo library to upload images.');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });

    if (!result.canceled && result.assets[0]) {
      setNewItem({ ...newItem, imageUrl: result.assets[0].uri });
    }
  };

  const handleAddItem = async () => {
    if (!currentBranchId && !newItem.selectedBranchId) {
      Alert.alert('Error', 'Please select a branch first.');
      return;
    }

    // Use selected branch or fall back to current branch
    const branchIdToUse = newItem.selectedBranchId || currentBranchId;

    if (newItem.name && newItem.price) {
      try {
        const addons = newItem.addons.split(',').filter(a => a.trim()).map(a => ({ name: a.trim(), price: 0 }));
        const complimentaryItems = newItem.complimentaryItems.split(',').filter(c => c.trim()).map(c => ({ name: c.trim() }));

        const item: MenuItemCreateRequest = {
          name: newItem.name,
          description: 'Mock description',
          price: parseFloat(newItem.price),
          category: newItem.category,
          preparationTimeMinutes: 15,
          metadata: {
            isVegetarian: newItem.isVegetarian,
            isVegan: false,
            spiceLevel: 'medium',
            addons: addons.length > 0 ? addons : undefined,
            complimentaryItems: complimentaryItems.length > 0 ? complimentaryItems : undefined,
            nutritionInfo: newItem.nutritionInfo,
            isAvailable: newItem.isAvailable
          }
        };

        // Create the menu item first
        const result = await dispatch(createMenuItem({
          branchId: branchIdToUse!,
          menuItemData: item
        })).unwrap();

        // If there's a local image, upload it to the server
        if (newItem.imageUrl && newItem.imageUrl.startsWith('file://')) {
          try {
            console.log('[Menu] Uploading image for newly created item:', result.menuItemId);
            await menuApiService.uploadMenuItemImage(result.menuItemId, newItem.imageUrl, 'primary');
            console.log('[Menu] Image uploaded successfully');
          } catch (uploadError: any) {
            console.error('[Menu] Failed to upload image:', uploadError);
            Alert.alert('Warning', 'Item created but image upload failed. You can add the image later.');
          }
        }

        setNewItem({
          name: '',
          price: '',
          category: 'Main Course',
          isAvailable: true,
          isVegetarian: true,
          addons: '',
          complimentaryItems: '',
          imageUrl: '',
          nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 },
          selectedBranchId: null,
        });
        setAddModalVisible(false);
        Alert.alert('Success', 'Item added successfully!');

        // Refresh menu items for the selected branch
        dispatch(fetchMenuItems({ branchId: branchIdToUse! }));
      } catch (error: any) {
        console.error('Failed to add item:', error);
        Alert.alert('Error', error.message || 'Failed to add item. Please try again.');
      }
    } else {
      Alert.alert('Error', 'Please fill name and price');
    }
  };

  const handleEditItem = (item: MenuItem) => {
    setSelectedItem(item);
    setNewItem({
      name: item.name,
      price: item.price.toString(),
      category: item.category,
      isAvailable: item.isAvailable,
      isVegetarian: item.metadata?.isVegetarian ?? true,
      addons: item.metadata?.addons?.map((a: any) => a.name).join(', ') || '',
      complimentaryItems: item.metadata?.complimentaryItems?.map((c: any) => c.name).join(', ') || '',
      imageUrl: item.metadata?.imageUrl || '',
      nutritionInfo: item.metadata?.nutritionInfo || { calories: 0, protein: 0, carbs: 0, fat: 0 }
    });
    setEditModalVisible(true);
  };

  const handleUpdateItem = async () => {
    if (selectedItem && newItem.name && newItem.price) {
      try {
        const addons = newItem.addons.split(',').filter(a => a.trim()).map(a => ({ name: a.trim(), price: 0 }));
        const complimentaryItems = newItem.complimentaryItems.split(',').filter(c => c.trim()).map(c => ({ name: c.trim() }));

        await dispatch(updateMenuItem({
          menuItemId: selectedItem.menuItemId,
          menuItemData: {
            name: newItem.name,
            price: parseFloat(newItem.price),
            category: newItem.category,
            isAvailable: newItem.isAvailable,
            preparationTimeMinutes: 15,
            metadata: {
              isVegetarian: newItem.isVegetarian,
              isVegan: false,
              spiceLevel: 'medium',
              addons: addons.length > 0 ? addons : undefined,
              complimentaryItems: complimentaryItems.length > 0 ? complimentaryItems : undefined,
              imageUrl: newItem.imageUrl || undefined,
              nutritionInfo: newItem.nutritionInfo,
            }
          }
        })).unwrap();

        setNewItem({
          name: '',
          price: '',
          category: 'Main Course',
          isAvailable: true,
          isVegetarian: true,
          addons: '',
          complimentaryItems: '',
          imageUrl: '',
          nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 }
        });
        setSelectedItem(null);
        setEditModalVisible(false);
        Alert.alert('Success', 'Item updated successfully!');
      } catch (error: any) {
        console.error('Failed to update item:', error);
        Alert.alert('Error', error.message || 'Failed to update item.');
      }
    } else {
      Alert.alert('Error', 'Please fill name and price');
    }
  };

  const handleOptionsItem = (item: any) => {
    setSelectedItem(item);
    setOptionsModalVisible(true);
  };

  const handleDeleteItem = () => {
    if (selectedItem) {
      dispatch(deleteMenuItem(selectedItem.menuItemId));
      setSelectedItem(null);
      setOptionsModalVisible(false);
      Alert.alert('Success', 'Item deleted successfully!');
    }
  };


  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.headerContent}>
            <Text style={styles.headerTitle}>Menu Management</Text>
          </View>

          {/* Search */}
          <View style={styles.searchContainer}>
            <Ionicons name="search" size={20} color="#6b7280" style={styles.searchIcon} />
            <TextInput
              style={styles.searchInput}
              placeholder="Search menu items..."
              placeholderTextColor="#6b7280"
              value={searchQuery}
              onChangeText={setSearchQuery}
            />
          </View>

          {/* Branch Selector for Viewing */}
          {branches.length > 1 && (
            <View style={styles.headerBranchSelector}>
              <TouchableOpacity
                style={styles.headerBranchPicker}
                onPress={() => setBranchPickerVisible(true)}
                accessibilityLabel="Select branch to view menu"
              >
                <Ionicons name="storefront-outline" size={18} color="#16a34a" />
                <Text style={styles.headerBranchPickerText}>
                  {branches.find(b => b.branchId === currentBranchId)?.branchName || 'Select Branch'}
                  {branches.find(b => b.branchId === currentBranchId)?.address?.area &&
                    ` (${branches.find(b => b.branchId === currentBranchId)?.address?.area})`
                  }
                </Text>
                <Ionicons name="chevron-down" size={18} color="#16a34a" />
              </TouchableOpacity>
            </View>
          )}
        </View>

        {/* Categories */}
        <View style={styles.categoriesContainer}>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={styles.categoriesScroll}
            contentContainerStyle={styles.categoriesContent}
          >
            {categories.map((category) => (
              <TouchableOpacity
                key={category}
                style={[
                  styles.categoryButton,
                  selectedCategory === category && styles.categoryButtonActive,
                ]}
                onPress={() => setSelectedCategory(category)}
              >
                <Text style={[
                  styles.categoryText,
                  selectedCategory === category && styles.categoryTextActive,
                ]}>
                  {category}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* Menu Items */}
        <View style={styles.content}>
          <View style={styles.menuItemsContainer}>
            {filteredItems.map((item) => (
              <View key={item.menuItemId} style={styles.menuItem}>
                <View style={styles.menuItemHeader}>
                  <View style={styles.menuItemInfo}>
                    <Text style={styles.menuItemName}>
                      {item.name}
                    </Text>
                    <Text style={styles.menuItemPrice}>
                      ₹{item.price}
                    </Text>
                  </View>
                  <View style={styles.menuItemActions}>
                    <TouchableOpacity style={styles.actionButton} onPress={() => handleEditItem(item)} accessibilityLabel="Edit Item">
                      <Ionicons name="pencil" size={20} color="#6b7280" />
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.actionButton} onPress={() => handleOptionsItem(item)} accessibilityLabel="Item Options">
                      <Ionicons name="ellipsis-vertical" size={20} color="#6b7280" />
                    </TouchableOpacity>
                  </View>
                </View>

                <View style={styles.menuItemFooter}>
                  <View style={styles.menuItemDetails}>
                    {item.metadata?.isVegetarian && (
                      <View style={styles.vegIndicator} />
                    )}
                    <Text style={styles.itemCategoryText}>{item.category}</Text>
                  </View>

                  <TouchableOpacity style={[
                    styles.availabilityBadge,
                    item.isAvailable ? styles.availableBadge : styles.unavailableBadge,
                  ]}>
                    <Text style={[
                      item.isAvailable ? styles.availableText : styles.unavailableText,
                    ]}>
                      {item.isAvailable ? 'Available' : 'Out of Stock'}
                    </Text>
                  </TouchableOpacity>
                </View>

                {item.metadata?.nutritionInfo && (
                  <View style={styles.menuItemFooter}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-around', marginTop: 8 }}>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Calories</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.metadata.nutritionInfo.calories}</Text>
                      </View>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Carbs</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.metadata.nutritionInfo.carbs}g</Text>
                      </View>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Protein</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.metadata.nutritionInfo.protein}g</Text>
                      </View>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Fat</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.metadata.nutritionInfo.fat}g</Text>
                      </View>
                    </View>
                  </View>
                )}
              </View>
            ))}
          </View>
        </View>
      </ScrollView>

      {/* Add Item Modal */}
      <Modal visible={addModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setAddModalVisible(false)} accessibilityLabel="Close Add Item Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Add New Item</Text>
              <View />
            </View>
            <ScrollView showsVerticalScrollIndicator={false}>
              {/* Branch Selector - only show if multiple branches exist */}
              {branches.length > 0 && (
                <View style={styles.branchSelectorContainer}>
                  <Text style={styles.branchSelectorLabel}>
                    <Ionicons name="location-outline" size={14} color="#374151" /> Select Branch
                  </Text>
                  {branches.map((branch) => (
                    <TouchableOpacity
                      key={branch.branchId}
                      style={[
                        styles.branchOption,
                        newItem.selectedBranchId === branch.branchId && styles.branchOptionSelected,
                      ]}
                      onPress={() => setNewItem({ ...newItem, selectedBranchId: branch.branchId })}
                      accessibilityLabel={`Select ${branch.branchName} branch`}
                    >
                      <Ionicons
                        name="storefront-outline"
                        size={20}
                        color={newItem.selectedBranchId === branch.branchId ? '#16a34a' : '#6b7280'}
                        style={styles.branchOptionIcon}
                      />
                      <View style={styles.branchOptionContent}>
                        <Text style={styles.branchOptionName}>{branch.branchName}</Text>
                        <Text style={styles.branchOptionCity}>
                          {branch.address?.area ? `${branch.address.area}, ${branch.city}` : branch.city}
                        </Text>
                      </View>
                      {newItem.selectedBranchId === branch.branchId && (
                        <Ionicons
                          name="checkmark-circle"
                          size={20}
                          color="#16a34a"
                          style={styles.branchOptionCheck}
                        />
                      )}
                    </TouchableOpacity>
                  ))}
                </View>
              )}
              <TextInput
                style={styles.input}
                placeholder="Enter dish name (e.g., Butter Chicken, Paneer Tikka)"
                placeholderTextColor="#6b7280"
                value={newItem.name}
                onChangeText={(text) => setNewItem({ ...newItem, name: text })}
                accessibilityLabel="Item Name Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Enter item price in INR (e.g., 299)"
                placeholderTextColor="#6b7280"
                value={newItem.price}
                onChangeText={(text) => setNewItem({ ...newItem, price: text })}
                keyboardType="numeric"
                accessibilityLabel="Item Price Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Category"
                placeholderTextColor="#6b7280"
                value={newItem.category}
                onChangeText={(text) => setNewItem({ ...newItem, category: text })}
                accessibilityLabel="Item Category Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Addons (comma separated)"
                placeholderTextColor="#6b7280"
                value={newItem.addons}
                onChangeText={(text) => setNewItem({ ...newItem, addons: text })}
                accessibilityLabel="Item Addons Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Complimentary Items (comma separated)"
                placeholderTextColor="#6b7280"
                value={newItem.complimentaryItems}
                onChangeText={(text) => setNewItem({ ...newItem, complimentaryItems: text })}
                accessibilityLabel="Item Complimentary Items Input"
              />
              {/* Image Upload Section */}
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 8 }}>Item Image</Text>
              {!newItem.imageUrl ? (
                <TouchableOpacity
                  style={[styles.button, { backgroundColor: '#6b7280' }]}
                  onPress={handlePickImage}
                  accessibilityLabel="Upload Item Image"
                >
                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                    <Ionicons name="camera" size={20} color="white" />
                    <Text style={styles.buttonText}>Upload Image</Text>
                  </View>
                </TouchableOpacity>
              ) : (
                <View style={{ marginTop: 12, alignItems: 'center' }}>
                  <View style={{
                    width: 150,
                    height: 150,
                    borderRadius: 12,
                    borderWidth: 1,
                    borderColor: '#e5e7eb',
                    overflow: 'hidden',
                    backgroundColor: '#f9fafb'
                  }}>
                    <Image
                      source={{ uri: newItem.imageUrl }}
                      style={{ width: '100%', height: '100%' }}
                      resizeMode="cover"
                    />
                  </View>
                  <View style={{ flexDirection: 'row', gap: 16, marginTop: 12 }}>
                    <TouchableOpacity
                      onPress={handlePickImage}
                      style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}
                      accessibilityLabel="Change Image"
                    >
                      <Ionicons name="refresh" size={16} color="#6b7280" />
                      <Text style={{ color: '#6b7280', fontSize: 14, fontWeight: '500' }}>
                        Change
                      </Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                      onPress={() => setNewItem({ ...newItem, imageUrl: '' })}
                      style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}
                      accessibilityLabel="Remove Image"
                    >
                      <Ionicons name="trash" size={16} color="#ef4444" />
                      <Text style={{ color: '#ef4444', fontSize: 14, fontWeight: '500' }}>
                        Remove
                      </Text>
                    </TouchableOpacity>
                  </View>
                </View>
              )}
              <Text style={{ fontSize: 16, fontWeight: 'semibold', color: '#111827', marginBottom: 8 }}>Nutrition Info (per serving)</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Calories</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.calories.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, calories: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Calories Input"
              />
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Carbs (g)</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.carbs.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, carbs: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Carbs Input"
              />
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Protein (g)</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.protein.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, protein: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Protein Input"
              />
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Fat (g)</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.fat.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, fat: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Fat Input"
              />
              <TouchableOpacity style={styles.button} onPress={handleAddItem} accessibilityLabel="Add Item">
                <Text style={styles.buttonText}>Add Item</Text>
              </TouchableOpacity>
            </ScrollView>
          </View>
        </View>
      </Modal>

      {/* Edit Item Modal */}
      <Modal visible={editModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => {
                setEditModalVisible(false); setSelectedItem(null); setNewItem({
                  name: '',
                  price: '',
                  category: 'Main Course',
                  isAvailable: true,
                  isVegetarian: true,
                  addons: '',
                  complimentaryItems: '',
                  imageUrl: '',
                  nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 }
                });
              }} accessibilityLabel="Close Edit Item Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Edit Item</Text>
              <View />
            </View>
            <ScrollView showsVerticalScrollIndicator={false}>
              <TextInput
                style={styles.input}
                placeholder="Item Name"
                placeholderTextColor="#6b7280"
                value={newItem.name}
                onChangeText={(text) => setNewItem({ ...newItem, name: text })}
                accessibilityLabel="Edit Item Name Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Price"
                placeholderTextColor="#6b7280"
                value={newItem.price}
                onChangeText={(text) => setNewItem({ ...newItem, price: text })}
                keyboardType="numeric"
                accessibilityLabel="Edit Item Price Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Category"
                placeholderTextColor="#6b7280"
                value={newItem.category}
                onChangeText={(text) => setNewItem({ ...newItem, category: text })}
                accessibilityLabel="Edit Item Category Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Addons (comma separated)"
                placeholderTextColor="#6b7280"
                value={newItem.addons}
                onChangeText={(text) => setNewItem({ ...newItem, addons: text })}
                accessibilityLabel="Edit Item Addons Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Complimentary Items (comma separated)"
                placeholderTextColor="#6b7280"
                value={newItem.complimentaryItems}
                onChangeText={(text) => setNewItem({ ...newItem, complimentaryItems: text })}
                accessibilityLabel="Edit Item Complimentary Items Input"
              />
              <TextInput
                style={styles.input}
                placeholder="Image URL (or upload)"
                placeholderTextColor="#6b7280"
                value={newItem.imageUrl}
                onChangeText={(text) => setNewItem({ ...newItem, imageUrl: text })}
                accessibilityLabel="Edit Item Image URL Input"
              />
              {/* TEMPORARILY COMMENTED OUT - DEBUG TEXT NODE ERROR
              <FeatureGate feature="imageUpload">
                <ImageUploadButton
                  onImageUploaded={(uri) => setNewItem({ ...newItem, imageUrl: uri })}
                  buttonText="Upload Image"
                  style={{ marginTop: 8 }}
                />
              </FeatureGate>
              */}
              {newItem.imageUrl && (
                <View style={{ marginTop: 12, alignItems: 'center' }}>
                  <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 8 }}>
                    Preview:
                  </Text>
                  <View style={{
                    width: 120,
                    height: 120,
                    borderRadius: 8,
                    borderWidth: 1,
                    borderColor: '#e5e7eb',
                    overflow: 'hidden',
                    backgroundColor: '#f9fafb'
                  }}>
                    <Image
                      source={{ uri: newItem.imageUrl.startsWith('local_file_') ? 'https://via.placeholder.com/120x120.png?text=Image+Uploaded' : newItem.imageUrl }}
                      style={{ width: '100%', height: '100%' }}
                      resizeMode="cover"
                    />
                  </View>
                  <TouchableOpacity
                    onPress={() => setNewItem({ ...newItem, imageUrl: '' })}
                    style={{ marginTop: 8 }}
                    accessibilityLabel="Remove Image"
                  >
                    <Text style={{ color: '#ef4444', fontSize: 12, fontWeight: '500' }}>
                      Remove Image
                    </Text>
                  </TouchableOpacity>
                </View>
              )}
              <Text style={{ fontSize: 16, fontWeight: 'semibold', color: '#111827', marginBottom: 8 }}>Nutrition Info (per serving)</Text>
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Calories</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.calories.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, calories: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Edit Calories Input"
              />
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Carbs (g)</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.carbs.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, carbs: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Edit Carbs Input"
              />
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Protein (g)</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.protein.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, protein: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Edit Protein Input"
              />
              <Text style={{ fontSize: 14, fontWeight: '500', color: '#374151', marginBottom: 4 }}>Fat (g)</Text>
              <TextInput
                style={styles.input}
                placeholder="0"
                placeholderTextColor="#6b7280"
                value={newItem.nutritionInfo.fat.toString()}
                onChangeText={(text) => setNewItem({ ...newItem, nutritionInfo: { ...newItem.nutritionInfo, fat: parseInt(text) || 0 } })}
                keyboardType="numeric"
                accessibilityLabel="Edit Fat Input"
              />
              <TouchableOpacity style={styles.button} onPress={handleUpdateItem} accessibilityLabel="Update Item">
                <Text style={styles.buttonText}>Update Item</Text>
              </TouchableOpacity>
            </ScrollView>
          </View>
        </View>
      </Modal>

      {/* Options Modal */}
      <Modal visible={optionsModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => { setOptionsModalVisible(false); setSelectedItem(null); }} accessibilityLabel="Close Options Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Item Options</Text>
              <View />
            </View>
            <TouchableOpacity style={styles.optionButton} onPress={() => { handleEditItem(selectedItem); setOptionsModalVisible(false); }} accessibilityLabel="Edit Item Option">
              <Text style={styles.optionText}>Edit Item</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.optionButton} onPress={handleDeleteItem} accessibilityLabel="Delete Item Option">
              <Text style={styles.optionText}>Delete Item</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Branch Picker Modal */}
      <Modal visible={branchPickerVisible} animationType="fade" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.branchModalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setBranchPickerVisible(false)} accessibilityLabel="Close Branch Picker">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Select Branch</Text>
              <View />
            </View>
            <ScrollView showsVerticalScrollIndicator={false}>
              {branches.map((branch) => (
                <TouchableOpacity
                  key={branch.branchId}
                  style={[
                    styles.branchOption,
                    currentBranchId === branch.branchId && styles.branchOptionSelected,
                  ]}
                  onPress={() => {
                    dispatch(setCurrentBranchId(branch.branchId));
                    dispatch(fetchMenuItems({ branchId: branch.branchId }));
                    setBranchPickerVisible(false);
                  }}
                  accessibilityLabel={`View ${branch.branchName} menu`}
                >
                  <Ionicons
                    name="storefront-outline"
                    size={20}
                    color={currentBranchId === branch.branchId ? '#16a34a' : '#6b7280'}
                    style={styles.branchOptionIcon}
                  />
                  <View style={styles.branchOptionContent}>
                    <Text style={styles.branchOptionName}>{branch.branchName}</Text>
                    <Text style={styles.branchOptionCity}>
                      {branch.address?.area ? `${branch.address.area}, ${branch.city}` : branch.city}
                    </Text>
                  </View>
                  {currentBranchId === branch.branchId && (
                    <Ionicons
                      name="checkmark-circle"
                      size={20}
                      color="#16a34a"
                      style={styles.branchOptionCheck}
                    />
                  )}
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>
        </View>
      </Modal>

      {/* Floating Action Button */}
      <TouchableOpacity style={styles.fab} onPress={() => setAddModalVisible(true)} accessibilityLabel="Add New Menu Item">
        <Ionicons name="add" size={30} color="white" />
      </TouchableOpacity>
    </SafeAreaView>
  );
}