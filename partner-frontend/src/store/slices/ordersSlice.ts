import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import { Order, OrderStatusUpdateRequest } from '../../core/types/api';
import { apiService } from '../../core/api/unifiedApiService';

interface OrdersState {
  orders: Order[];
  isLoading: boolean;
  error: string | null;
  filter: Order['status'] | 'all';
  currentBranchId: number | null;
  dashboardStats: any | null;
}

const initialState: OrdersState = {
  orders: [],
  isLoading: false,
  error: null,
  filter: 'all',
  currentBranchId: null,
  dashboardStats: null,
};

// Async thunks for API operations - NOT IMPLEMENTED YET
export const fetchOrders = createAsyncThunk(
  'orders/fetchOrders',
  async (params: {
    branchId: number;
    status?: Order['status'];
    page?: number;
    size?: number;
  }) => {
    const response = await apiService.getOrders(params.branchId, {
      status: params.status,
      page: params.page,
      size: params.size,
    });
    return response.data; // Will throw error since not implemented
  }
);

export const updateOrderStatus = createAsyncThunk(
  'orders/updateOrderStatus',
  async (params: {
    orderId: string;
    statusData: OrderStatusUpdateRequest;
  }) => {
    const response = await apiService.updateOrderStatus(params.orderId, params.statusData);
    return response.data; // Will throw error since not implemented
  }
);

export const fetchDashboardStats = createAsyncThunk(
  'orders/fetchDashboardStats',
  async (params: {
    branchId: number;
    dateRange?: string;
  }) => {
    const response = await apiService.getDashboardStats(params.branchId, params.dateRange);
    return response.data; // Will throw error since not implemented
  }
);

export const fetchTopItems = createAsyncThunk(
  'orders/fetchTopItems',
  async (params: {
    branchId: number;
    period?: string;
    limit?: number;
  }) => {
    const response = await apiService.getTopItems(params.branchId, {
      period: params.period,
      limit: params.limit,
    });
    return response.data; // Will throw error since not implemented
  }
);

const ordersSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    setFilter: (state, action: PayloadAction<OrdersState['filter']>) => {
      state.filter = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
    setCurrentBranchId: (state, action: PayloadAction<number>) => {
      state.currentBranchId = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch orders
    builder
      .addCase(fetchOrders.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.isLoading = false;
        state.orders = action.payload.orders;
        state.currentBranchId = action.payload.branchId;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch orders';
      });

    // Update order status
    builder
      .addCase(updateOrderStatus.pending, (state) => {
        state.error = null;
      })
      .addCase(updateOrderStatus.fulfilled, (state, action) => {
        const index = state.orders.findIndex(order => order.orderId === action.payload.orderId);
        if (index !== -1) {
          state.orders[index] = action.payload;
        }
      })
      .addCase(updateOrderStatus.rejected, (state, action) => {
        state.error = action.error.message || 'Failed to update order status';
      });

    // Fetch dashboard stats
    builder
      .addCase(fetchDashboardStats.pending, (state) => {
        state.error = null;
      })
      .addCase(fetchDashboardStats.fulfilled, (state, action) => {
        state.dashboardStats = action.payload;
      })
      .addCase(fetchDashboardStats.rejected, (state, action) => {
        state.error = action.error.message || 'Failed to fetch dashboard stats';
      });

    // Fetch top items
    builder
      .addCase(fetchTopItems.pending, (state) => {
        state.error = null;
      })
      .addCase(fetchTopItems.fulfilled, (state, action) => {
        // Store top items in dashboard stats for now
        if (!state.dashboardStats) {
          state.dashboardStats = {};
        }
        state.dashboardStats.topItems = action.payload;
      })
      .addCase(fetchTopItems.rejected, (state, action) => {
        state.error = action.error.message || 'Failed to fetch top items';
      });
  },
});

export const { setFilter, clearError, setCurrentBranchId } = ordersSlice.actions;
export default ordersSlice.reducer;