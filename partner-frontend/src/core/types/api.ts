// API Types matching backend DTOs

// Base response types
export interface ApiResponse<T = any> {
  data: T;
  success: boolean;
  message?: string;
  status?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Vendor and Branch types (matching backend DTOs)
export interface Vendor {
  vendorId: number;
  companyName: string;
  brandName: string;
  legalEntityName: string;
  companyEmail: string;
  companyPhone: string;
  panNumber?: string;
  gstNumber?: string;
  images?: Record<string, any>; // Map<String, Object>
  metadata?: Record<string, any>; // Map<String, Object>
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface VendorRegistrationRequest {
  companyName: string;
  brandName: string;
  companyEmail: string;
  companyPhone: string;
  gstNumber?: string;
  panNumber?: string;
  legalEntityName?: string;
}

export interface Address {
  street?: string;
  area?: string;
  city?: string;
  state?: string;
  pincode?: string;
  latitude?: number;
  longitude?: number;
}

export interface OperatingHours {
  day: string;
  timeSlots: TimeSlot[];
}

export interface TimeSlot {
  open: string;
  close: string;
}

export interface Branch {
  branchId: number;
  vendorId: number;
  branchName: string;
  branchCode?: string;
  address?: Record<string, any>; // Map<String, Object>
  latitude?: number;
  longitude?: number;
  city?: string;
  branchPhone: string;
  branchEmail: string;
  branchManagerName?: string;
  onboardingStatus?: string;
  isActive: boolean;
  isOpen: boolean;
  preferences?: Record<string, any>; // Map<String, Object>
  operatingHours?: Record<string, any>; // Map<String, Object>
  images?: Record<string, any>; // Map<String, Object>
  metadata?: Record<string, any>; // Map<String, Object>
  createdAt: string;
  updatedAt: string;
}

export interface BranchCreateRequest {
  branchName: string;
  address?: Record<string, any>; // Map<String, Object>
  latitude?: number;
  longitude?: number;
  city?: string;
  branchPhone: string;
  branchEmail: string;
}

export interface BranchStatusRequest {
  isOpen: boolean;
}

export interface OperatingHoursRequest {
  hours: Record<string, TimeSlot[]>;
}

export interface BranchAvailabilityResponse {
  branchId: number;
  isOpen: boolean;
  isActive: boolean;
  isWithinOperatingHours: boolean;
  currentStatus: string;
  nextOpenTime?: string;
  nextCloseTime?: string;
}

export interface OperatingHoursResponse {
  branchId: number;
  operatingHours: Record<string, TimeSlot[]>;
  isOpen: boolean;
}

// Image Upload types (matching backend ImageUploadResponse)
export interface ImageUploadResponse {
  status: string;
  message: string;
  entityId: number;
  entityType: string;
  imageType: string;
  fileKey: string;
  urls: Record<string, string>;
  processing?: {
    status: string;
    message: string;
    startedAt?: string;
    completedAt?: string;
  };
  uploadedAt: string;
}


// Document types
export interface Document {
  documentId: number;
  branchId: number;
  documentType: string;
  documentNumber: string;
  issueDate: string;
  expiryDate?: string;
  fileUrl: string;
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
}

export interface DocumentUploadRequest {
  documentType: string;
  documentNumber: string;
  issueDate: string;
  expiryDate?: string;
  fileUrl: string;
}

export interface DocumentResponse {
  documentId: number;
  branchId: number;
  documentType: string;
  documentNumber: string;
  issueDate: string;
  expiryDate?: string;
  fileUrl: string;
  verificationStatus: string;
  createdAt: string;
  updatedAt: string;
}

// Menu Item types (matching backend DTOs)
export interface MenuItem {
  menuItemId: number; // Changed from string to number (Long)
  branchId: number;
  name: string;
  description: string;
  price: number;
  category: string;
  isAvailable: boolean;
  preparationTimeMinutes: number;
  images?: Record<string, any>; // Map<String, Object>
  metadata?: Record<string, any>; // Map<String, Object>
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface MenuItemCreateRequest {
  name: string;
  description?: string;
  price: number;
  category: string;
  preparationTimeMinutes: number;
  metadata?: Record<string, any>; // Map<String, Object>
  tags?: string[];
}

export interface MenuItemUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  category?: string;
  isAvailable?: boolean;
  preparationTimeMinutes?: number;
  metadata?: Record<string, any>; // Map<String, Object>
  tags?: string[];
}

export interface MenuItemResponse {
  menuItemId: number; // Changed from string to number (Long)
  branchId: number;
  name: string;
  description: string;
  price: number;
  category: string;
  isAvailable: boolean;
  preparationTimeMinutes: number;
  images?: Record<string, any>; // Map<String, Object>
  metadata?: Record<string, any>; // Map<String, Object>
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

// ============================================================================
// ORDER TYPES - Matching OpenAPI Specification
// ============================================================================

// Order State Enum (13 states)
export enum OrderState {
  CREATED = 'CREATED',
  VALIDATED = 'VALIDATED',
  PAYMENT_CONFIRMED = 'PAYMENT_CONFIRMED',
  PENDING_ACCEPTANCE = 'PENDING_ACCEPTANCE',
  ACCEPTED = 'ACCEPTED',
  PREPARING = 'PREPARING',
  READY_FOR_PICKUP = 'READY_FOR_PICKUP',
  ASSIGNED_TO_RIDER = 'ASSIGNED_TO_RIDER',
  PICKED_UP = 'PICKED_UP',
  DELIVERED = 'DELIVERED',
  CLOSED = 'CLOSED',
  CANCELLED = 'CANCELLED',
  REJECTED = 'REJECTED',
}

// Payment Status Enum
export enum PaymentStatus {
  PENDING = 'PENDING',
  AUTHORIZED = 'AUTHORIZED',
  CAPTURED = 'CAPTURED',
  PAID = 'PAID',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
  PARTIALLY_REFUNDED = 'PARTIALLY_REFUNDED',
}

// Order Type Enum
export enum OrderType {
  SINGLE = 'SINGLE',
  MULTI_RESTAURANT = 'MULTI_RESTAURANT',
}

// Delivery Address
export interface DeliveryAddress {
  addressLine1?: string;
  addressLine2?: string;
  landmark?: string;
  city?: string;
  state?: string;
  pincode?: string;
  addressType?: string;
  label?: string;
}

// Order Item Response
export interface OrderItemResponse {
  orderItemId: string; // UUID
  menuItemId: number;
  itemName: string;
  quantity: number;
  priceAtOrder: number;
  notes?: string;
  customizations?: Record<string, any>;
}

// Order Response (matching backend DTO)
export interface OrderResponse {
  orderId: string; // UUID
  customerId: string; // UUID
  orderType: OrderType;
  state: OrderState;
  paymentStatus: PaymentStatus;
  items: OrderItemResponse[];
  itemTotal: number;
  deliveryCharges: number;
  platformFee: number;
  gst: number;
  discount: number;
  totalAmount: number;
  deliveryAddress?: DeliveryAddress;
  specialInstructions?: string;
  createdAt: string;
  updatedAt: string;
  acceptedAt?: string;
  deliveredAt?: string;
}

// Order entity (for internal use)
export interface Order {
  orderId: string; // UUID
  orderType: OrderType;
  parentOrderId?: string; // UUID
  customerId: string; // UUID
  vendorId: number;
  vendorBranchId: number;
  checkoutSessionId?: string;
  state: OrderState;
  itemTotal: number;
  deliveryCharges: number;
  platformFee: number;
  gst: number;
  discount: number;
  totalAmount: number;
  paymentStatus: PaymentStatus;
  paymentMethod?: string;
  paymentTransactionId?: string;
  deliveryAddress?: DeliveryAddress;
  deliveryLatitude?: number;
  deliveryLongitude?: number;
  specialInstructions?: string;
  createdAt: string;
  updatedAt: string;
  validatedAt?: string;
  paymentConfirmedAt?: string;
  acceptedAt?: string;
  preparingStartedAt?: string;
  readyAt?: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  cancelledAt?: string;
  estimatedPrepTimeMinutes?: number;
  estimatedDeliveryTime?: string;
  cancellationReason?: string;
  cancelledBy?: string;
  metadata?: Record<string, any>;
  orderItems?: OrderItem[];
  terminal?: boolean;
  cancellable?: boolean;
}

export interface OrderItem {
  orderItemId: string; // UUID
  order?: Order;
  menuItemId: number;
  itemName: string;
  quantity: number;
  priceAtOrder: number;
  notes?: string;
  customizations?: Record<string, any>;
  createdAt: string;
}

// Order Request/Action types
export interface AcceptOrderRequest {
  estimatedPrepTime: number; // 5-120 minutes
}

export interface RejectOrderRequest {
  reason: string;
}

export interface CancelOrderRequest {
  reason: string;
  cancelledBy?: string;
}

export interface CreateOrderFromCheckoutRequest {
  checkoutSessionId: string;
  paymentToken?: string;
}

// ============================================================================
// DELIVERY TYPES - Matching OpenAPI Specification
// ============================================================================

// Delivery State Enum (9 states)
export enum DeliveryState {
  PENDING = 'PENDING',
  SEARCHING_RIDER = 'SEARCHING_RIDER',
  RIDER_ASSIGNED = 'RIDER_ASSIGNED',
  RIDER_ACCEPTED = 'RIDER_ACCEPTED',
  AT_RESTAURANT = 'AT_RESTAURANT',
  PICKED_UP = 'PICKED_UP',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  FAILED = 'FAILED',
}

// Location DTO
export interface LocationDTO {
  latitude: number; // -90 to 90
  longitude: number; // -180 to 180
  address?: string;
  landmark?: string;
}

// Delivery Response DTO
export interface DeliveryResponseDTO {
  deliveryId: string; // UUID
  orderId: string; // UUID
  riderId?: string; // UUID
  state: DeliveryState;
  deliveryFee: number;
  pickupLocation?: LocationDTO;
  deliveryLocation?: LocationDTO;
  riderLocation?: LocationDTO;
  riderAssignedAt?: string;
  riderAcceptedAt?: string;
  reachedRestaurantAt?: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  failedAt?: string;
  failureReason?: string;
  restaurantWaitTimeMinutes?: number;
  totalDeliveryTimeMinutes?: number;
  createdAt: string;
  updatedAt: string;
}

// Delivery Action types
export interface RejectDeliveryRequestDTO {
  reason: string; // 10-500 characters
}

export interface UpdateDeliveryStatusRequestDTO {
  status: 'REACHED_RESTAURANT' | 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED';
  notes?: string;
  deliveryProof?: string;
  customerSignature?: string;
  currentLocation?: LocationDTO;
}

// ============================================================================
// RIDER TYPES - Matching OpenAPI Specification
// ============================================================================

// Rider Response DTO
export interface RiderResponseDTO {
  riderId: string; // UUID
  name: string;
  phone: string;
  email?: string;
  isOnline: boolean;
  isOnBreak: boolean;
  currentDeliveries: number;
  currentLocation?: LocationDTO;
  lastLocationUpdate?: string;
  rating?: number;
  totalDeliveries: number;
  completedDeliveriesToday: number;
  acceptanceRate?: number;
  earnings?: EarningsDTO;
  createdAt: string;
}

export interface EarningsDTO {
  today: number;
  thisWeek: number;
  thisMonth: number;
  deliveriesToday: number;
  deliveriesThisWeek: number;
  deliveriesThisMonth: number;
}

export interface UpdateRiderRequestDTO {
  status?: 'ONLINE' | 'OFFLINE' | 'ON_BREAK';
  location?: LocationDTO;
}

export interface RiderInfoDTO {
  riderId: string; // UUID
  name: string;
  phone: string;
  rating?: number;
  currentLocation?: LocationDTO;
}

// ============================================================================
// CHECKOUT TYPES - Matching OpenAPI Specification
// ============================================================================

export enum CheckoutStatus {
  READY_FOR_COMMIT = 'READY_FOR_COMMIT',
  IN_PROGRESS = 'IN_PROGRESS',
  VALIDATION_FAILED = 'VALIDATION_FAILED',
  COMMITTED = 'COMMITTED',
  EXPIRED = 'EXPIRED',
}

export interface GeoLocation {
  latitude: number;
  longitude: number;
}

export interface CartItemRequest {
  menuItemId: number;
  quantity: number;
  customizations?: Record<string, any>;
  specialInstructions?: string;
}

export interface CheckoutRequest {
  userId: string; // UUID
  vendorBranchId: number;
  deliveryAddress: DeliveryAddress;
  deliveryLocation?: GeoLocation;
  items: CartItemRequest[];
  paymentMethod: string;
  couponCode?: string;
  scheduledDeliveryTime?: string;
  contactlessDelivery?: boolean;
  leaveAtDoor?: boolean;
  deliveryInstructions?: string;
}

export interface CheckoutItem {
  menuItemId: number;
  name: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  customizations?: Record<string, any>;
  isAvailable: boolean;
  stockQuantity?: number;
}

export interface DiscountDetails {
  couponCode?: string;
  discountType?: string;
  discountValue?: number;
  maxDiscount?: number;
  appliedDiscount: number;
}

export interface GstDetails {
  cgst: number;
  sgst: number;
  gstRate: number;
}

export interface DeliveryDetails {
  distance: number;
  distanceUnit: string;
  deliveryZone?: string;
  baseFee: number;
  distanceFee: number;
}

export interface PricingDetails {
  itemTotal: number;
  discount: number;
  discountDetails?: DiscountDetails;
  subtotalAfterDiscount: number;
  deliveryCharges: number;
  deliveryDetails?: DeliveryDetails;
  platformFee: number;
  gst: number;
  gstDetails?: GstDetails;
  totalAmount: number;
  currency: string;
}

export interface DeliveryEstimate {
  estimatedDeliveryTime?: string;
  estimatedPrepTime: number;
  estimatedDeliveryDuration: number;
  totalEstimatedTime: number;
}

export interface ValidationResults {
  allItemsAvailable: boolean;
  deliveryAddressValid: boolean;
  deliveryZoneServiceable: boolean;
  vendorAcceptingOrders: boolean;
  paymentMethodSupported: boolean;
}

export interface VendorInfo {
  vendorId: string;
  vendorName: string;
  vendorBranchId: number;
  branchName: string;
  estimatedPrepTime: number;
  isAcceptingOrders: boolean;
}

export interface CheckoutError {
  code: string;
  message: string;
  field?: string;
  severity?: string;
  metadata?: Record<string, any>;
}

export interface CheckoutResponse {
  checkoutSessionId: string;
  status: CheckoutStatus;
  expiresAt: string;
  vendor?: VendorInfo;
  items: CheckoutItem[];
  pricing: PricingDetails;
  deliveryEstimate?: DeliveryEstimate;
  validations?: ValidationResults;
  errors?: CheckoutError[];
}

export interface CommitCheckoutRequest {
  checkoutSessionId: string;
  paymentTransactionId?: string;
  paymentMethod?: string;
}

// ============================================================================
// CUSTOMER ORDER TRACKING TYPES
// ============================================================================

export enum CustomerOrderStatus {
  ORDER_PLACED = 'ORDER_PLACED',
  ORDER_CONFIRMED = 'ORDER_CONFIRMED',
  PREPARING = 'PREPARING',
  RIDER_ASSIGNED = 'RIDER_ASSIGNED',
  READY_FOR_PICKUP = 'READY_FOR_PICKUP',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export interface CustomerStatusResponseDTO {
  orderId: string; // UUID
  status: CustomerOrderStatus;
  primaryMessage: string;
  secondaryMessage: string;
  progressPercentage: number;
  canCancel: boolean;
  estimatedArrival?: string;
  estimatedMinutesRemaining?: number;
  riderInfo?: RiderInfoDTO;
  orderPlacedAt: string;
  lastUpdatedAt: string;
}

// ============================================================================
// PAGINATED RESPONSE TYPES
// ============================================================================

export interface PageableObject {
  offset: number;
  sort?: SortObject;
  pageNumber: number;
  pageSize: number;
  paged: boolean;
  unpaged: boolean;
}

export interface SortObject {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}

export interface PageDeliveryResponseDTO {
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
  size: number;
  content: DeliveryResponseDTO[];
  number: number;
  sort?: SortObject;
  numberOfElements: number;
  pageable?: PageableObject;
  empty: boolean;
}

// Dashboard/Analytics types - PLACEHOLDERS FOR FUTURE IMPLEMENTATION
export interface DashboardStats {
  revenue: {
    today: number;
    yesterday: number;
    growth: number;
  };
  orders: {
    today: number;
    yesterday: number;
    growth: number;
  };
  avgOrderValue: {
    today: number;
    yesterday: number;
    growth: number;
  };
  activeItems: number;
}

export interface TopItem {
  name: string;
  orders: number;
  revenue: number;
}

// Error types
export interface ApiError {
  message: string;
  code?: string;
  status?: number;
  data?: any;
  errors?: string[];
}

export interface ValidationErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>; // Map of field names to error messages
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}