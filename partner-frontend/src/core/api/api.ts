// Mock API service - replace with actual API calls later
import { Order } from '../../store/slices/ordersSlice';
import { MenuItem as MenuItemType } from '../../store/slices/menuSlice';
import { FileUploadService } from '../services/fileUploadService';

class ApiService {
  private baseURL = 'https://api.nashtto.com'; // Replace with actual API URL

  // Simulate API delay
  private delay(ms: number = 1000): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // Auth endpoints
  async login(email: string, password: string) {
    await this.delay(1500);

    // Mock response
    return {
      user: {
        id: '1',
        name: 'Restaurant Owner',
        email: email,
        restaurantId: 'rest_123',
      },
      token: 'mock_jwt_token',
    };
  }

  async getRestaurantProfile(restaurantId: string) {
    await this.delay(800);

    return {
      id: restaurantId,
      name: 'Nashtto Restaurant',
      description: 'Fine dining restaurant serving authentic cuisine',
      address: '123 Main Street, Downtown',
      phone: '+91 9876543210',
      email: 'contact@nashtto.com',
      imageUrl: 'https://via.placeholder.com/200',
      isOpen: true,
      operatingHours: {
        monday: { open: '09:00', close: '22:00', isOpen: true },
        tuesday: { open: '09:00', close: '22:00', isOpen: true },
        wednesday: { open: '09:00', close: '22:00', isOpen: true },
        thursday: { open: '09:00', close: '22:00', isOpen: true },
        friday: { open: '09:00', close: '23:00', isOpen: true },
        saturday: { open: '10:00', close: '23:00', isOpen: true },
        sunday: { open: '10:00', close: '22:00', isOpen: true },
      },
    };
  }

  // Orders endpoints
  async getOrders(restaurantId: string, status?: string): Promise<Order[]> {
    await this.delay(1000);

    const mockOrders: Order[] = [
      {
        id: '#1234',
        customerName: 'Rahul S.',
        items: [
          { id: '1', name: 'Paneer Tikka Roll', quantity: 2, price: 180 },
          { id: '2', name: 'Masala Dosa', quantity: 1, price: 120 },
        ],
        total: 480,
        status: 'preparing',
        createdAt: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
        estimatedTime: 15,
      },
      {
        id: '#1235',
        customerName: 'Priya M.',
        items: [
          { id: '3', name: 'Chicken Biryani', quantity: 1, price: 200 },
          { id: '4', name: 'Raita', quantity: 1, price: 40 },
        ],
        total: 240,
        status: 'ready',
        createdAt: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
      },
      {
        id: '#1236',
        customerName: 'Amit K.',
        items: [
          { id: '5', name: 'Veg Thali', quantity: 1, price: 250 },
        ],
        total: 250,
        status: 'delivered',
        createdAt: new Date(Date.now() - 12 * 60 * 1000).toISOString(),
      },
    ];

    if (status && status !== 'all') {
      return mockOrders.filter(order => order.status === status);
    }

    return mockOrders;
  }

  async updateOrderStatus(orderId: string, status: Order['status']) {
    await this.delay(500);

    return {
      success: true,
      orderId,
      status,
      updatedAt: new Date().toISOString(),
    };
  }

  // Menu endpoints
  async getMenuItems(restaurantId: string): Promise<MenuItemType[]> {
    await this.delay(800);

    return [
      {
        id: '1',
        name: 'Paneer Tikka Roll',
        description: 'Delicious paneer tikka wrapped in fresh roti',
        price: 180,
        category: 'Main Course',
        imageUrl: 'https://via.placeholder.com/150',
        isAvailable: true,
        isVegetarian: true,
        isVegan: false,
        spiceLevel: 'medium',
        preparationTime: 15,
        allergens: ['gluten', 'dairy'],
        tags: ['popular', 'recommended'],
      },
      {
        id: '2',
        name: 'Chicken Biryani',
        description: 'Aromatic basmati rice with tender chicken',
        price: 200,
        category: 'Main Course',
        imageUrl: 'https://via.placeholder.com/150',
        isAvailable: true,
        isVegetarian: false,
        isVegan: false,
        spiceLevel: 'hot',
        preparationTime: 25,
        allergens: ['gluten'],
        tags: ['bestseller'],
      },
      {
        id: '3',
        name: 'Masala Dosa',
        description: 'Crispy dosa filled with spiced potatoes',
        price: 120,
        category: 'Main Course',
        imageUrl: 'https://via.placeholder.com/150',
        isAvailable: true,
        isVegetarian: true,
        isVegan: true,
        spiceLevel: 'mild',
        preparationTime: 10,
        allergens: ['gluten'],
        tags: ['vegetarian'],
      },
    ];
  }

  async createMenuItem(restaurantId: string, item: Omit<MenuItemType, 'id'>) {
    await this.delay(1000);

    return {
      id: `item_${Date.now()}`,
      ...item,
    };
  }

  async updateMenuItem(itemId: string, updates: Partial<MenuItemType>) {
    await this.delay(800);

    return {
      success: true,
      itemId,
      updates,
    };
  }

  async deleteMenuItem(itemId: string) {
    await this.delay(500);

    return {
      success: true,
      itemId,
    };
  }

  // Analytics endpoints
  async getDashboardStats(restaurantId: string, dateRange: string) {
    await this.delay(1200);

    return {
      revenue: {
        today: 12450,
        yesterday: 11200,
        growth: 12.5,
      },
      orders: {
        today: 47,
        yesterday: 43,
        growth: 8.2,
      },
      avgOrderValue: {
        today: 264,
        yesterday: 270,
        growth: -2.1,
      },
      activeItems: 23,
    };
  }

  async getTopItems(restaurantId: string, period: string) {
    await this.delay(800);

    return [
      { name: 'Paneer Tikka Roll', orders: 12, revenue: 1800 },
      { name: 'Chicken Biryani', orders: 8, revenue: 1600 },
      { name: 'Masala Dosa', orders: 6, revenue: 900 },
      { name: 'Veg Thali', orders: 5, revenue: 1250 },
    ];
  }

  // File upload endpoints
  async uploadImage(restaurantId: string, imageUri: string, category: 'menu' | 'logo' | 'cover' = 'menu') {
    await this.delay(2000); // Simulate upload time

    // In real implementation, this would:
    // 1. Upload the image to cloud storage (AWS S3, Cloudinary, etc.)
    // 2. Return the public URL
    // 3. Update the restaurant/menu item record

    // For mock implementation, return a placeholder URL
    const mockUrl = `https://via.placeholder.com/400x400.png?text=${encodeURIComponent(category.toUpperCase())}`;
    
    return {
      success: true,
      url: mockUrl,
      category,
      message: 'Image uploaded successfully (mock)',
    };
  }

  async uploadDocument(restaurantId: string, documentUri: string, documentName: string, documentType: string) {
    await this.delay(1500); // Simulate upload time

    // In real implementation, this would:
    // 1. Upload the document to secure cloud storage
    // 2. Return the document URL
    // 3. Update the restaurant record with document metadata

    // For mock implementation, return success
    return {
      success: true,
      documentId: `doc_${Date.now()}`,
      documentName,
      documentType,
      message: `${documentName} uploaded successfully (mock)`,
    };
  }

  async deleteUploadedFile(fileUri: string) {
    await this.delay(500);

    return {
      success: true,
      message: 'File deleted successfully (mock)',
    };
  }

  // Menu item with image operations
  async addMenuItemWithImage(restaurantId: string, menuItem: MenuItemType, imageUri?: string) {
    await this.delay(1500);

    let imageUrl = menuItem.imageUrl;
    
    if (imageUri) {
      const uploadResult = await this.uploadImage(restaurantId, imageUri, 'menu');
      if (uploadResult.success) {
        imageUrl = uploadResult.url;
      }
    }

    const itemWithImage = {
      ...menuItem,
      imageUrl,
    };

    return {
      success: true,
      item: itemWithImage,
      message: 'Menu item added successfully',
    };
  }
}

export const apiService = new ApiService();
export default apiService;