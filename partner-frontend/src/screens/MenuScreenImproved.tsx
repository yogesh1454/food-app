import { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Modal,
  Alert,
  FlatList,
} from 'react-native';
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
} from '../store/slices/menuSlice';
import { RootState } from '../store';
import { MenuItem, MenuItemCreateRequest } from '../core/types/api';
import ScreenLayout, { Section, EmptyState } from '../core/components/ScreenLayout';
import { Card, MenuItemCard } from '../core/components/Card';
import { Button } from '../core/components/Button';
import { TextInputField } from '../core/components/TextInputField';
import { LoadingSpinner } from '../core/components/LoadingSpinner';
import { ErrorHandler, useErrorHandler } from '../core/components/ErrorHandler';
import { colors, spacing } from '../core/constants';
import { apiService } from '../core/api/unifiedApiService';

// Mock current branch ID - in real app this would come from auth or context
const CURRENT_BRANCH_ID = 1;

const MenuScreenImproved = () => {
  const dispatch = useDispatch();
  const { items, categories, isLoading, error, selectedCategory } = useSelector((state: RootState) => state.menu);
  const { handleError } = useErrorHandler();

  // Local state
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);
  const [formData, setFormData] = useState<MenuItemCreateRequest>({
    name: '',
    description: '',
    price: 0,
    category: '',
    isAvailable: true,
    preparationTimeMinutes: 15,
    tags: [],
  });

  // Load data on component mount
  useEffect(() => {
    loadMenuData();
  }, []);

  // Load menu items and categories
  const loadMenuData = async () => {
    try {
      await Promise.all([
        dispatch(fetchMenuItems({ branchId: CURRENT_BRANCH_ID })).unwrap(),
        dispatch(fetchMenuItemCategories(CURRENT_BRANCH_ID)).unwrap(),
      ]);
    } catch (error) {
      console.error('Failed to load menu data:', error);
    }
  };

  // Handle category selection
  const handleCategorySelect = (category: string) => {
    dispatch(setSelectedCategory(category === 'All' ? null : category));
    dispatch(fetchMenuItems({ 
      branchId: CURRENT_BRANCH_ID, 
      category: category === 'All' ? undefined : category 
    }));
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchQuery(query);
    if (query.trim()) {
      // Implement search functionality
      dispatch(fetchMenuItems({ branchId: CURRENT_BRANCH_ID }));
    }
  };

  // Handle add/edit menu item
  const handleSaveMenuItem = async () => {
    try {
      if (editingItem) {
        await dispatch(updateMenuItem({
          menuItemId: editingItem.menuItemId,
          menuItemData: formData,
        })).unwrap();
        Alert.alert('Success', 'Menu item updated successfully');
      } else {
        await dispatch(createMenuItem({
          branchId: CURRENT_BRANCH_ID,
          menuItemData: formData,
        })).unwrap();
        Alert.alert('Success', 'Menu item added successfully');
      }
      
      resetForm();
      setShowAddModal(false);
    } catch (error) {
      Alert.alert('Error', handleError(error));
    }
  };

  // Handle delete menu item
  const handleDeleteMenuItem = async (item: MenuItem) => {
    Alert.alert(
      'Delete Item',
      `Are you sure you want to delete "${item.name}"?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await dispatch(deleteMenuItem(item.menuItemId)).unwrap();
              Alert.alert('Success', 'Menu item deleted successfully');
            } catch (error) {
              Alert.alert('Error', handleError(error));
            }
          },
        },
      ]
    );
  };

  // Reset form
  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      price: 0,
      category: '',
      isAvailable: true,
      preparationTimeMinutes: 15,
      tags: [],
    });
    setEditingItem(null);
  };

  // Start editing item
  const handleEditItem = (item: MenuItem) => {
    setEditingItem(item);
    setFormData({
      name: item.name,
      description: item.description,
      price: item.price,
      category: item.category,
      isAvailable: item.isAvailable,
      preparationTimeMinutes: item.preparationTimeMinutes,
      tags: item.tags,
    });
    setShowAddModal(true);
  };

  // Filter items based on search
  const filteredItems = items.filter(item =>
    item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    item.description.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Render menu item
  const renderMenuItem = ({ item }: { item: MenuItem }) => (
    <MenuItemCard
      item={item}
      onPress={() => handleEditItem(item)}
      onEdit={() => handleEditItem(item)}
      onDelete={() => handleDeleteMenuItem(item)}
      showActions={true}
    />
  );

  // Render add/edit modal
  const renderAddEditModal = () => (
    <Modal
      visible={showAddModal}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={() => {
        setShowAddModal(false);
        resetForm();
      }}
    >
      <ScreenLayout
        title={editingItem ? 'Edit Menu Item' : 'Add Menu Item'}
        showBackButton
        onBackPress={() => {
          setShowAddModal(false);
          resetForm();
        }}
        scrollable
      >
        <Section>
          <TextInputField
            label="Item Name"
            value={formData.name}
            onChangeText={(text) => setFormData({ ...formData, name: text })}
            placeholder="Enter item name"
          />
          
          <TextInputField
            label="Description"
            value={formData.description}
            onChangeText={(text) => setFormData({ ...formData, description: text })}
            placeholder="Enter item description"
            multiline
            numberOfLines={3}
          />
          
          <TextInputField
            label="Price (₹)"
            value={formData.price.toString()}
            onChangeText={(text) => setFormData({ ...formData, price: parseFloat(text) || 0 })}
            placeholder="0.00"
            keyboardType="numeric"
          />
          
          <TextInputField
            label="Category"
            value={formData.category}
            onChangeText={(text) => setFormData({ ...formData, category: text })}
            placeholder="Enter category"
          />
          
          <TextInputField
            label="Preparation Time (minutes)"
            value={formData.preparationTimeMinutes.toString()}
            onChangeText={(text) => setFormData({ ...formData, preparationTimeMinutes: parseInt(text) || 15 })}
            placeholder="15"
            keyboardType="numeric"
          />
        </Section>

        <View style={styles.modalActions}>
          <Button
            title="Cancel"
            variant="outline"
            onPress={() => {
              setShowAddModal(false);
              resetForm();
            }}
            style={styles.cancelButton}
          />
          <Button
            title={editingItem ? 'Update' : 'Add'}
            onPress={handleSaveMenuItem}
            disabled={!formData.name || !formData.category}
          />
        </View>
      </ScreenLayout>
    </Modal>
  );

  return (
    <ScreenLayout
      title="Menu Management"
      loading={isLoading}
      error={error}
      onRetry={loadMenuData}
    >
      {/* Search Bar */}
      <View style={styles.searchContainer}>
        <Ionicons name="search" size={20} color={colors.textMuted} style={styles.searchIcon} />
        <TextInputField
          value={searchQuery}
          onChangeText={handleSearch}
          placeholder="Search menu items..."
          style={styles.searchInput}
          containerStyle={styles.searchInputContainer}
        />
      </View>

      {/* Categories */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        style={styles.categoriesContainer}
        contentContainerStyle={styles.categoriesContent}
      >
        {categories.map((category) => (
          <TouchableOpacity
            key={category}
            style={[
              styles.categoryButton,
              (selectedCategory === category || (!selectedCategory && category === 'All')) &&
                styles.categoryButtonActive,
            ]}
            onPress={() => handleCategorySelect(category)}
          >
            <Text
              style={[
                styles.categoryText,
                (selectedCategory === category || (!selectedCategory && category === 'All')) &&
                  styles.categoryTextActive,
              ]}
            >
              {category}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {/* Menu Items */}
      {filteredItems.length === 0 ? (
        <EmptyState
          title="No menu items found"
          subtitle={searchQuery ? 'Try adjusting your search' : 'Add your first menu item to get started'}
          icon="restaurant-outline"
          actionText="Add Menu Item"
          onAction={() => setShowAddModal(true)}
        />
      ) : (
        <FlatList
          data={filteredItems}
          renderItem={({ item }) => renderMenuItem({ item })}
          keyExtractor={(item) => item.menuItemId}
          contentContainerStyle={styles.listContainer}
          showsVerticalScrollIndicator={false}
        />
      )}

      {/* Floating Action Button */}
      <TouchableOpacity
        style={styles.fab}
        onPress={() => setShowAddModal(true)}
      >
        <Ionicons name="add" size={24} color={colors.textWhite} />
      </TouchableOpacity>

      {/* Add/Edit Modal */}
      {renderAddEditModal()}

      {/* Error Handler */}
      <ErrorHandler
        error={error}
        onRetry={loadMenuData}
        onDismiss={() => dispatch(clearError())}
      />
    </ScreenLayout>
  );
};

const styles = StyleSheet.create({
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surfaceSecondary,
    borderRadius: spacing.md,
    paddingHorizontal: spacing.md,
    marginBottom: spacing.md,
  },
  searchIcon: {
    marginRight: spacing.sm,
  },
  searchInputContainer: {
    flex: 1,
    backgroundColor: 'transparent',
    paddingHorizontal: 0,
    paddingVertical: 0,
  },
  searchInput: {
    fontSize: 16,
    color: colors.text,
  },
  categoriesContainer: {
    marginBottom: spacing.md,
  },
  categoriesContent: {
    paddingHorizontal: spacing.md,
  },
  categoryButton: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: spacing.lg,
    backgroundColor: colors.surfaceSecondary,
    marginRight: spacing.sm,
  },
  categoryButtonActive: {
    backgroundColor: colors.primary,
  },
  categoryText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.textSecondary,
  },
  categoryTextActive: {
    color: colors.textWhite,
  },
  listContainer: {
    paddingHorizontal: spacing.md,
    paddingBottom: spacing.xxxl,
  },
  fab: {
    position: 'absolute',
    bottom: spacing.lg,
    right: spacing.lg,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
  },
  modalActions: {
    flexDirection: 'row',
    gap: spacing.md,
    marginTop: spacing.lg,
  },
  cancelButton: {
    flex: 1,
  },
});

export default MenuScreenImproved;