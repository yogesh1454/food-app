// =====================================================
// FRONTEND TO DATABASE MODEL MAPPING
// Generated from React Native Restaurant Partner App
// =====================================================

// =====================================================
// USER & AUTHENTICATION
// =====================================================

export interface FrontendUser {
  id: string;
  name: string;
  email: string;
  restaurantId: string;
}

export interface DatabaseUser {
  id: string; // UUID
  name: string;
  email: string;
  password_hash: string;
  restaurant_id: string; // Foreign key to restaurants
  is_active: boolean;
  is_first_time: boolean;
  created_at: string; // ISO timestamp
  updated_at: string; // ISO timestamp
}

// Mapping function: FrontendUser -> DatabaseUser (partial for inserts)
export function mapFrontendUserToDatabase(user: FrontendUser): Partial<DatabaseUser> {
  return {
    name: user.name,
    email: user.email,
    restaurant_id: user.restaurantId,
  };
}

// Mapping function: DatabaseUser -> FrontendUser
export function mapDatabaseUserToFrontend(user: DatabaseUser): FrontendUser {
  return {
    id: user.id,
    name: user.name,
    email: user.email,
    restaurantId: user.restaurant_id,
  };
}

// =====================================================
// RESTAURANT/VENDOR
// =====================================================

export interface FrontendRestaurant {
  id: string;
  name: string;
  cuisineType: string;
  description: string;
  address: string;
  phone: string;
  email: string;
  imageUrl?: string;
  logoUrl?: string;
  coverPhotoUrl?: string;
  isOpen: boolean;
  operatingHours: {
    [key: string]: { open: string; close: string; isOpen: boolean };
  };
  gstNumber?: string;
  fssaiNumber?: string;
  licenseDocuments?: string[];
  staff: FrontendStaff[];
}

export interface DatabaseRestaurant {
  id: string; // UUID
  name: string;
  cuisine_type: string;
  description: string;
  address: string;
  phone: string;
  email: string;
  image_url?: string;
  logo_url?: string;
  cover_photo_url?: string;
  is_open: boolean;
  operating_hours: {
    [key: string]: { open: string; close: string; isOpen: boolean };
  }; // JSONB
  gst_number?: string;
  fssai_number?: string;
  license_documents?: string[]; // JSONB
  owner_id: string; // Foreign key to users
  subscription_status: 'trial' | 'active' | 'expired';
  subscription_expires_at?: string; // ISO timestamp
  rating: number; // Decimal(2,1)
  total_orders: number;
  created_at: string; // ISO timestamp
  updated_at: string; // ISO timestamp
}

// Mapping function: FrontendRestaurant -> DatabaseRestaurant (partial for inserts)
export function mapFrontendRestaurantToDatabase(restaurant: FrontendRestaurant, ownerId: string): Partial<DatabaseRestaurant> {
  return {
    name: restaurant.name,
    cuisine_type: restaurant.cuisineType,
    description: restaurant.description,
    address: restaurant.address,
    phone: restaurant.phone,
    email: restaurant.email,
    image_url: restaurant.imageUrl,
    logo_url: restaurant.logoUrl,
    cover_photo_url: restaurant.coverPhotoUrl,
    is_open: restaurant.isOpen,
    operating_hours: restaurant.operatingHours,
    gst_number: restaurant.gstNumber,
    fssai_number: restaurant.fssaiNumber,
    license_documents: restaurant.licenseDocuments || [],
    owner_id: ownerId,
  };
}

// Mapping function: DatabaseRestaurant -> FrontendRestaurant
export function mapDatabaseRestaurantToFrontend(restaurant: DatabaseRestaurant, staff: FrontendStaff[] = []): FrontendRestaurant {
  return {
    id: restaurant.id,
    name: restaurant.name,
    cuisineType: restaurant.cuisine_type,
    description: restaurant.description,
    address: restaurant.address,
    phone: restaurant.phone,
    email: restaurant.email,
    imageUrl: restaurant.image_url,
    logoUrl: restaurant.logo_url,
    coverPhotoUrl: restaurant.cover_photo_url,
    isOpen: restaurant.is_open,
    operatingHours: restaurant.operating_hours,
    gstNumber: restaurant.gst_number,
    fssaiNumber: restaurant.fssai_number,
    licenseDocuments: restaurant.license_documents,
    staff: staff,
  };
}

// =====================================================
// STAFF MANAGEMENT
// =====================================================

export interface FrontendStaff {
  id: string;
  name: string;
  role: string;
  phone: string;
  email: string;
}

export interface DatabaseStaff {
  id: string; // UUID
  restaurant_id: string; // Foreign key to restaurants
  name: string;
  role: string;
  phone: string;
  email: string;
  is_active: boolean;
  created_at: string; // ISO timestamp
  updated_at: string; // ISO timestamp
}

// Mapping function: FrontendStaff -> DatabaseStaff (partial for inserts)
export function mapFrontendStaffToDatabase(staff: FrontendStaff, restaurantId: string): Partial<DatabaseStaff> {
  return {
    restaurant_id: restaurantId,
    name: staff.name,
    role: staff.role,
    phone: staff.phone,
    email: staff.email,
  };
}

// Mapping function: DatabaseStaff -> FrontendStaff
export function mapDatabaseStaffToFrontend(staff: DatabaseStaff): FrontendStaff {
  return {
    id: staff.id,
    name: staff.name,
    role: staff.role,
    phone: staff.phone,
    email: staff.email,
  };
}

// =====================================================
// MENU ITEMS
// =====================================================

export interface FrontendMenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl?: string;
  isAvailable: boolean;
  isVegetarian: boolean;
  isVegan: boolean;
  spiceLevel: 'mild' | 'medium' | 'hot' | 'extra-hot';
  preparationTime: number;
  allergens?: string[];
  tags?: string[];
  quantity?: number;
  addons?: { id: string; name: string; price: number }[];
  complimentaryItems?: { id: string; name: string }[];
  nutritionInfo?: { calories: number; protein: number; carbs: number; fat: number };
}

export interface DatabaseMenuItem {
  id: string; // UUID
  restaurant_id: string; // Foreign key to restaurants
  category_id?: string; // Foreign key to menu_categories
  name: string;
  description: string;
  price: number; // Decimal(10,2)
  image_url?: string;
  is_available: boolean;
  is_vegetarian: boolean;
  is_vegan: boolean;
  spice_level: 'mild' | 'medium' | 'hot' | 'extra-hot';
  preparation_time: number; // minutes
  allergens?: string[]; // JSONB
  tags?: string[]; // JSONB
  quantity?: number;
  addons?: { id: string; name: string; price: number }[]; // JSONB
  complimentary_items?: { id: string; name: string }[]; // JSONB
  nutrition_info?: { calories: number; protein: number; carbs: number; fat: number }; // JSONB
  position: number;
  is_featured: boolean;
  created_at: string; // ISO timestamp
  updated_at: string; // ISO timestamp
}

export interface DatabaseMenuCategory {
  id: string; // UUID
  restaurant_id: string; // Foreign key to restaurants
  name: string;
  description?: string;
  display_order: number;
  is_active: boolean;
  created_at: string; // ISO timestamp
  updated_at: string; // ISO timestamp
}

// Combined menu item with category info for frontend
export interface FrontendMenuItemWithCategory extends FrontendMenuItem {
  categoryId?: string;
  categoryName?: string;
  position: number;
  isFeatured: boolean;
}

// Mapping function: FrontendMenuItem -> DatabaseMenuItem (partial for inserts)
export function mapFrontendMenuItemToDatabase(
  item: FrontendMenuItem, 
  restaurantId: string, 
  categoryId?: string
): Partial<DatabaseMenuItem> {
  return {
    restaurant_id: restaurantId,
    category_id: categoryId,
    name: item.name,
    description: item.description,
    price: item.price,
    image_url: item.imageUrl,
    is_available: item.isAvailable,
    is_vegetarian: item.isVegetarian,
    is_vegan: item.isVegan,
    spice_level: item.spiceLevel,
    preparation_time: item.preparationTime,
    allergens: item.allergens || [],
    tags: item.tags || [],
    quantity: item.quantity || 0,
    addons: item.addons || [],
    complimentary_items: item.complimentaryItems || [],
    nutrition_info: item.nutritionInfo,
  };
}

// Mapping function: DatabaseMenuItem -> FrontendMenuItem
export function mapDatabaseMenuItemToFrontend(item: DatabaseMenuItem, categoryName?: string): FrontendMenuItemWithCategory {
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    price: item.price,
    category: categoryName || 'Uncategorized',
    imageUrl: item.image_url,
    isAvailable: item.is_available,
    isVegetarian: item.is_vegetarian,
    isVegan: item.is_vegan,
    spiceLevel: item.spice_level,
    preparationTime: item.preparation_time,
    allergens: item.allergens,
    tags: item.tags,
    quantity: item.quantity,
    addons: item.addons,
    complimentaryItems: item.complimentary_items,
    nutritionInfo: item.nutrition_info,
    categoryId: item.category_id,
    categoryName: categoryName,
    position: item.position,
    isFeatured: item.is_featured,
  };
}

// =====================================================
// ORDERS
// =====================================================

// Normalize order status between frontend and database
export type NormalizedOrderStatus = 'new' | 'preparing' | 'ready' | 'out_for_delivery' | 'delivered' | 'cancelled';

export interface FrontendOrder {
  id: string;
  customerName: string;
  items: FrontendOrderItem[];
  total: number;
  status: NormalizedOrderStatus;
  createdAt: string;
  estimatedTime?: number;
}

export interface FrontendOrderItem {
  id: string;
  name: string;
  quantity: number;
  price: number;
  specialInstructions?: string;
}

export interface DatabaseOrder {
  id: string; // UUID
  restaurant_id: string; // Foreign key to restaurants
  order_number: string;
  customer_name?: string;
  customer_phone?: string;
  customer_email?: string;
  order_type: 'delivery' | 'pickup' | 'dine-in';
  status: NormalizedOrderStatus;
  subtotal: number; // Decimal(10,2)
  tax_amount: number; // Decimal(10,2)
  delivery_charge: number; // Decimal(10,2)
  discount_amount: number; // Decimal(10,2)
  total_amount: number; // Decimal(10,2)
  payment_status: 'pending' | 'paid' | 'failed' | 'refunded';
  payment_method?: string;
  special_instructions?: string;
  delivery_address?: string;
  estimated_preparation_time?: number; // minutes
  actual_preparation_time?: number; // minutes
  delivered_at?: string; // ISO timestamp
  created_at: string; // ISO timestamp
  updated_at: string; // ISO timestamp
}

export interface DatabaseOrderItem {
  id: string; // UUID
  order_id: string; // Foreign key to orders
  menu_item_id?: string; // Foreign key to menu_items
  name: string; // Snapshot of menu item name
  quantity: number;
  unit_price: number; // Decimal(10,2)
  total_price: number; // Decimal(10,2)
  special_instructions?: string;
  addons?: { id: string; name: string; price: number }[]; // JSONB
  created_at: string; // ISO timestamp
}

// Status mapping utilities
export function normalizeOrderStatus(status: string): NormalizedOrderStatus {
  const statusMap: Record<string, NormalizedOrderStatus> = {
    'new': 'new',
    'preparing': 'preparing',
    'ready': 'ready',
    'out for delivery': 'out_for_delivery',
    'out_for_delivery': 'out_for_delivery',
    'delivered': 'delivered',
    'cancelled': 'cancelled',
  };
  
  return statusMap[status] || 'new';
}

export function denormalizeOrderStatus(status: NormalizedOrderStatus): string {
  const denormalizeMap: Record<NormalizedOrderStatus, string> = {
    'new': 'new',
    'preparing': 'preparing',
    'ready': 'ready',
    'out_for_delivery': 'out for delivery',
    'delivered': 'delivered',
    'cancelled': 'cancelled',
  };
  
  return denormalizeMap[status] || 'new';
}

// Mapping function: FrontendOrder -> DatabaseOrder (partial for inserts)
export function mapFrontendOrderToDatabase(
  order: FrontendOrder, 
  restaurantId: string, 
  orderNumber: string
): Partial<DatabaseOrder> {
  return {
    restaurant_id: restaurantId,
    order_number: orderNumber,
    customer_name: order.customerName,
    status: normalizeOrderStatus(order.status),
    subtotal: order.total, // Simplified calculation
    tax_amount: 0, // TODO: Calculate actual tax
    delivery_charge: 0, // TODO: Calculate based on order type
    discount_amount: 0, // TODO: Apply any discounts
    total_amount: order.total,
    payment_status: 'pending',
    order_type: 'delivery', // Default, TODO: make dynamic
    estimated_preparation_time: order.estimatedTime,
  };
}

// Mapping function: FrontendOrderItem -> DatabaseOrderItem (partial for inserts)
export function mapFrontendOrderItemToDatabase(
  item: FrontendOrderItem, 
  orderId: string, 
  menuItemId?: string
): Partial<DatabaseOrderItem> {
  return {
    order_id: orderId,
    menu_item_id: menuItemId,
    name: item.name,
    quantity: item.quantity,
    unit_price: item.price,
    total_price: item.price * item.quantity,
    special_instructions: item.specialInstructions,
  };
}

// Mapping function: DatabaseOrder + DatabaseOrderItem[] -> FrontendOrder
export function mapDatabaseOrderToFrontend(
  order: DatabaseOrder, 
  orderItems: DatabaseOrderItem[]
): FrontendOrder {
  const frontendItems: FrontendOrderItem[] = orderItems.map(item => ({
    id: item.id,
    name: item.name,
    quantity: item.quantity,
    price: item.unit_price,
    specialInstructions: item.special_instructions,
  }));

  return {
    id: order.id,
    customerName: order.customer_name || 'Unknown Customer',
    items: frontendItems,
    total: order.total_amount,
    status: normalizeOrderStatus(order.status),
    createdAt: order.created_at,
    estimatedTime: order.estimated_preparation_time,
  };
}

// =====================================================
// BULK MAPPING UTILITIES
// =====================================================

/**
 * Maps array of database restaurants to frontend format
 */
export function mapDatabaseRestaurantsToFrontend(
  restaurants: DatabaseRestaurant[],
  staffMap: Map<string, FrontendStaff[]>
): FrontendRestaurant[] {
  return restaurants.map(restaurant => {
    const staff = staffMap.get(restaurant.id) || [];
    return mapDatabaseRestaurantToFrontend(restaurant, staff);
  });
}

/**
 * Maps array of database menu items to frontend format with categories
 */
export function mapDatabaseMenuItemsToFrontend(
  items: (DatabaseMenuItem & { category_name?: string })[],
  categoriesMap: Map<string, string>
): FrontendMenuItemWithCategory[] {
  return items.map(item => {
    const categoryName = item.category_name || categoriesMap.get(item.category_id || '') || 'Uncategorized';
    return mapDatabaseMenuItemToFrontend(item, categoryName);
  });
}

/**
 * Maps array of database orders to frontend format
 */
export function mapDatabaseOrdersToFrontend(
  orders: DatabaseOrder[],
  orderItemsMap: Map<string, DatabaseOrderItem[]>
): FrontendOrder[] {
  return orders.map(order => {
    const orderItems = orderItemsMap.get(order.id) || [];
    return mapDatabaseOrderToFrontend(order, orderItems);
  });
}

// =====================================================
// VALIDATION FUNCTIONS
// =====================================================

export function validateFrontendRestaurant(restaurant: FrontendRestaurant): string[] {
  const errors: string[] = [];
  
  if (!restaurant.name?.trim()) errors.push('Restaurant name is required');
  if (!restaurant.address?.trim()) errors.push('Restaurant address is required');
  if (!restaurant.phone?.trim()) errors.push('Restaurant phone is required');
  if (!restaurant.email?.trim()) errors.push('Restaurant email is required');
  
  return errors;
}

export function validateFrontendMenuItem(item: FrontendMenuItem): string[] {
  const errors: string[] = [];
  
  if (!item.name?.trim()) errors.push('Menu item name is required');
  if (item.price <= 0) errors.push('Menu item price must be greater than 0');
  if (!item.category?.trim()) errors.push('Menu item category is required');
  
  return errors;
}

export function validateFrontendOrder(order: FrontendOrder): string[] {
  const errors: string[] = [];
  
  if (!order.customerName?.trim()) errors.push('Customer name is required');
  if (!order.items || order.items.length === 0) errors.push('Order must have at least one item');
  if (order.total <= 0) errors.push('Order total must be greater than 0');
  
  return errors;
}

// =====================================================
// SCHEMA VERSION INFO
// =====================================================

/**
 * Schema version for migration tracking
 */
export const SCHEMA_VERSION = '1.0.0';
export const SCHEMA_CREATED_AT = '2025-11-05T16:23:49.984Z';
export const LAST_UPDATED_AT = '2025-11-05T16:29:44.500Z';

/**
 * Migration notes for different versions
 */
export const MIGRATION_NOTES = {
  '1.0.0': 'Initial schema creation based on React Native frontend models',
};
// Export all types individually (they are already exported above as interfaces)

/**
 * Schema version for migration tracking
 */
export const SCHEMA_VERSION = '1.0.0';
export const SCHEMA_CREATED_AT = '2025-11-05T16:23:49.984Z';
export const LAST_UPDATED_AT = '2025-11-05T16:29:55.539Z';

/**
 * Migration notes for different versions
 */
export const MIGRATION_NOTES = {
  '1.0.0': 'Initial schema creation based on React Native frontend models',
};

// Export all types as a single object for easier importing
export const Types = {
  // User types
  FrontendUser,
  DatabaseUser,
  
  // Restaurant types
  FrontendRestaurant,
  DatabaseRestaurant,
  
  // Staff types
  FrontendStaff,
  DatabaseStaff,
  
  // Menu types
  FrontendMenuItem,
  DatabaseMenuItem,
  DatabaseMenuCategory,
  FrontendMenuItemWithCategory,
  
  // Order types
  FrontendOrder,
  FrontendOrderItem,
  DatabaseOrder,
  DatabaseOrderItem,
  
  // Utility types
  NormalizedOrderStatus,
};