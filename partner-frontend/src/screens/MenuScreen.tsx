import { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  TextInput,
  StyleSheet,
  Modal,
  Alert,
  Image,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useDispatch, useSelector } from 'react-redux';
import { addMenuItem, updateMenuItem, deleteMenuItem } from '../store/slices/menuSlice';
import { RootState } from '../store';
import ImageUploadButton from '../core/components/ImageUploadButton';
import DocumentUploadButton from '../core/components/DocumentUploadButton';
import FeatureGate from '../core/components/FeatureGate';
import featureFlags from '../core/api/featureFlags';
import { useFeatureFlags } from '../core/hooks/useFeatureFlags';

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
});

export default function MenuScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [optionsModalVisible, setOptionsModalVisible] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [newItem, setNewItem] = useState({
    name: '',
    price: '',
    category: 'Main Course',
    isAvailable: true,
    isVegetarian: true,
    quantity: '',
    addons: '',
    complimentaryItems: '',
    imageUrl: '',
    nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 }
  });
  
  // Feature flag hooks
  const { flags, isEnabled, loading } = useFeatureFlags();
  const dispatch = useDispatch();
  const menuItems = useSelector((state: RootState) => state.menu.items);
  const categories = useSelector((state: RootState) => state.menu.categories);

  const filteredItems = selectedCategory === 'All' ? menuItems : menuItems.filter(item => item.category === selectedCategory);

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

  const handleAddItem = () => {
    if (newItem.name && newItem.price) {
      const addons = newItem.addons.split(',').map(a => ({ id: Date.now().toString() + Math.random(), name: a.trim(), price: 0 }));
      const complimentaryItems = newItem.complimentaryItems.split(',').map(c => ({ id: Date.now().toString() + Math.random(), name: c.trim() }));
      const item = {
        id: Date.now().toString(),
        name: newItem.name,
        description: 'Mock description',
        price: parseFloat(newItem.price),
        category: newItem.category,
        isAvailable: newItem.isAvailable,
        isVegetarian: newItem.isVegetarian,
        isVegan: false,
        spiceLevel: 'medium' as const,
        preparationTime: 15,
        quantity: parseInt(newItem.quantity) || undefined,
        addons: addons.length > 0 ? addons : undefined,
        complimentaryItems: complimentaryItems.length > 0 ? complimentaryItems : undefined,
        imageUrl: newItem.imageUrl || undefined,
        nutritionInfo: newItem.nutritionInfo,
      };
      dispatch(addMenuItem(item));
      setNewItem({
        name: '',
        price: '',
        category: 'Main Course',
        isAvailable: true,
        isVegetarian: true,
        quantity: '',
        addons: '',
        complimentaryItems: '',
        imageUrl: '',
        nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 }
      });
      setAddModalVisible(false);
      Alert.alert('Success', 'Item added successfully!');
    } else {
      Alert.alert('Error', 'Please fill name and price');
    }
  };

  const handleEditItem = (item: any) => {
    setSelectedItem(item);
    setNewItem({
      name: item.name,
      price: item.price.toString(),
      category: item.category,
      isAvailable: item.isAvailable,
      isVegetarian: item.isVegetarian,
      quantity: item.quantity?.toString() || '',
      addons: item.addons?.map((a: any) => a.name).join(', ') || '',
      complimentaryItems: item.complimentaryItems?.map((c: any) => c.name).join(', ') || '',
      imageUrl: item.imageUrl || '',
      nutritionInfo: item.nutritionInfo || { calories: 0, protein: 0, carbs: 0, fat: 0 }
    });
    setEditModalVisible(true);
  };

  const handleUpdateItem = () => {
    if (selectedItem && newItem.name && newItem.price) {
      const addons = newItem.addons.split(',').map(a => ({ id: Date.now().toString() + Math.random(), name: a.trim(), price: 0 }));
      const complimentaryItems = newItem.complimentaryItems.split(',').map(c => ({ id: Date.now().toString() + Math.random(), name: c.trim() }));
      dispatch(updateMenuItem({
        id: selectedItem.id,
        updates: {
          name: newItem.name,
          price: parseFloat(newItem.price),
          category: newItem.category,
          isAvailable: newItem.isAvailable,
          isVegetarian: newItem.isVegetarian,
          quantity: parseInt(newItem.quantity) || undefined,
          addons: addons.length > 0 ? addons : undefined,
          complimentaryItems: complimentaryItems.length > 0 ? complimentaryItems : undefined,
          imageUrl: newItem.imageUrl || undefined,
          nutritionInfo: newItem.nutritionInfo,
        }
      }));
      setNewItem({
        name: '',
        price: '',
        category: 'Main Course',
        isAvailable: true,
        isVegetarian: true,
        quantity: '',
        addons: '',
        complimentaryItems: '',
        imageUrl: '',
        nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 }
      });
      setSelectedItem(null);
      setEditModalVisible(false);
      Alert.alert('Success', 'Item updated successfully!');
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
      dispatch(deleteMenuItem(selectedItem.id));
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
              <View key={item.id} style={styles.menuItem}>
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
                    {item.isVegetarian && (
                      <View style={styles.vegIndicator} />
                    )}
                    <Text style={styles.itemCategoryText}>{item.category}</Text>
                    {item.quantity !== undefined && (
                      <Text style={styles.itemCategoryText}>Qty: {item.quantity}</Text>
                    )}
                  </View>

                  <TouchableOpacity style={[
                    styles.availabilityBadge,
                    (item.isAvailable && (item.quantity === undefined || item.quantity > 0)) ? styles.availableBadge : styles.unavailableBadge,
                  ]}>
                    <Text style={[
                      (item.isAvailable && (item.quantity === undefined || item.quantity > 0)) ? styles.availableText : styles.unavailableText,
                    ]}>
                      {(item.isAvailable && (item.quantity === undefined || item.quantity > 0)) ? 'Available' : 'Out of Stock'}
                    </Text>
                  </TouchableOpacity>
                </View>

                {item.nutritionInfo && (
                  <View style={styles.menuItemFooter}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-around', marginTop: 8 }}>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Calories</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.nutritionInfo.calories}</Text>
                      </View>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Carbs</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.nutritionInfo.carbs}g</Text>
                      </View>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Protein</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.nutritionInfo.protein}g</Text>
                      </View>
                      <View style={{ alignItems: 'center', backgroundColor: '#f3f4f6', padding: 8, borderRadius: 8, minWidth: 60 }}>
                        <Text style={{ fontSize: 12, color: '#6b7280', fontWeight: '500' }}>Fat</Text>
                        <Text style={{ fontSize: 16, fontWeight: 'bold', color: '#111827' }}>{item.nutritionInfo.fat}g</Text>
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
                placeholder="Quantity"
                placeholderTextColor="#6b7280"
                value={newItem.quantity}
                onChangeText={(text) => setNewItem({ ...newItem, quantity: text })}
                keyboardType="numeric"
                accessibilityLabel="Item Quantity Input"
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
              <TextInput
                style={styles.input}
                placeholder="Image URL (or upload)"
                placeholderTextColor="#6b7280"
                value={newItem.imageUrl}
                onChangeText={(text) => setNewItem({ ...newItem, imageUrl: text })}
                accessibilityLabel="Item Image URL Input"
              />
<FeatureGate feature="imageUpload">
                <ImageUploadButton
                  onImageUploaded={handleImageUpload}
                  buttonText="Upload Image"
                  style={{ marginTop: 8 }}
                />
              </FeatureGate>
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
              <TouchableOpacity style={styles.button} onPress={handleNutritionAnalysis} accessibilityLabel="Analyze Nutrition">
                <Text style={styles.buttonText}>Analyze Nutrition (AI)</Text>
              </TouchableOpacity>
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
              <TouchableOpacity onPress={() => { setEditModalVisible(false); setSelectedItem(null); setNewItem({
                name: '',
                price: '',
                category: 'Main Course',
                isAvailable: true,
                isVegetarian: true,
                quantity: '',
                addons: '',
                complimentaryItems: '',
                imageUrl: '',
                nutritionInfo: { calories: 0, protein: 0, carbs: 0, fat: 0 }
              }); }} accessibilityLabel="Close Edit Item Modal">
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
                placeholder="Quantity"
                placeholderTextColor="#6b7280"
                value={newItem.quantity}
                onChangeText={(text) => setNewItem({ ...newItem, quantity: text })}
                keyboardType="numeric"
                accessibilityLabel="Edit Item Quantity Input"
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
              <FeatureGate feature="imageUpload">
                <ImageUploadButton
                  onImageUploaded={(uri) => setNewItem({ ...newItem, imageUrl: uri })}
                  buttonText="Upload Image"
                  style={{ marginTop: 8 }}
                />
              </FeatureGate>
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

      {/* Floating Action Button */}
      <TouchableOpacity style={styles.fab} onPress={() => setAddModalVisible(true)} accessibilityLabel="Add New Menu Item">
        <Ionicons name="add" size={30} color="white" />
      </TouchableOpacity>
    </SafeAreaView>
  );
}