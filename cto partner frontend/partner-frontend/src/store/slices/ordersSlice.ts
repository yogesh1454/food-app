import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Order {
  id: string;
  customerName: string;
  items: OrderItem[];
  total: number;
  status: 'new' | 'preparing' | 'ready' | 'out for delivery' | 'delivered' | 'cancelled';
  createdAt: string;
  estimatedTime?: number;
}

export interface OrderItem {
  id: string;
  name: string;
  quantity: number;
  price: number;
  specialInstructions?: string;
}

interface OrdersState {
  orders: Order[];
  isLoading: boolean;
  error: string | null;
  filter: 'all' | 'new' | 'preparing' | 'ready' | 'delivered';
}

const initialState: OrdersState = {
  orders: [],
  isLoading: false,
  error: null,
  filter: 'all',
};

const ordersSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    setOrders: (state, action: PayloadAction<Order[]>) => {
      state.orders = action.payload;
    },
    addOrder: (state, action: PayloadAction<Order>) => {
      state.orders.unshift(action.payload);
    },
    updateOrderStatus: (state, action: PayloadAction<{ orderId: string; status: Order['status'] }>) => {
      const order = state.orders.find(o => o.id === action.payload.orderId);
      if (order) {
        order.status = action.payload.status;
      }
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
    setFilter: (state, action: PayloadAction<OrdersState['filter']>) => {
      state.filter = action.payload;
    },
  },
});

export const { setOrders, addOrder, updateOrderStatus, setLoading, setError, setFilter } = ordersSlice.actions;
export default ordersSlice.reducer;