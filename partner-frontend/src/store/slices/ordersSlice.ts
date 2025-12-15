import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import { Order, OrderResponse, OrderState } from '../../core/types/api';
import { vendorOrdersApiService } from '../../core/api/vendorOrdersApiService';

interface OrdersState {
  orders: OrderResponse[];
  isLoading: boolean;
  error: string | null;
  filter: OrderState | 'all';
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

// Async thunks for vendor order operations
export const fetchOrders = createAsyncThunk(
  'orders/fetchOrders',
  async () => {
    const response = await vendorOrdersApiService.listPendingOrders();
    return response.data;
  }
);

export const acceptOrder = createAsyncThunk(
  'orders/acceptOrder',
  async (params: {
    orderId: string;
    estimatedPrepTime: number;
  }) => {
    const response = await vendorOrdersApiService.acceptOrder(
      params.orderId,
      params.estimatedPrepTime
    );
    return response.data;
  }
);

export const rejectOrder = createAsyncThunk(
  'orders/rejectOrder',
  async (params: {
    orderId: string;
    reason: string;
  }) => {
    const response = await vendorOrdersApiService.rejectOrder(
      params.orderId,
      params.reason
    );
    return response.data;
  }
);

export const markOrderReady = createAsyncThunk(
  'orders/markReady',
  async (orderId: string) => {
    const response = await vendorOrdersApiService.markOrderReady(orderId);
    return response.data;
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
        state.orders = action.payload;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch orders';
      });

    // Accept order
    builder
      .addCase(acceptOrder.pending, (state) => {
        state.error = null;
      })
      .addCase(acceptOrder.fulfilled, (state, action) => {
        const index = state.orders.findIndex(order => order.orderId === action.payload.orderId);
        if (index !== -1) {
          state.orders[index] = action.payload;
        }
      })
      .addCase(acceptOrder.rejected, (state, action) => {
        state.error = action.error.message || 'Failed to accept order';
      });

    // Reject order
    builder
      .addCase(rejectOrder.pending, (state) => {
        state.error = null;
      })
      .addCase(rejectOrder.fulfilled, (state, action) => {
        const index = state.orders.findIndex(order => order.orderId === action.payload.orderId);
        if (index !== -1) {
          state.orders[index] = action.payload;
        }
      })
      .addCase(rejectOrder.rejected, (state, action) => {
        state.error = action.error.message || 'Failed to reject order';
      });

    // Mark order ready
    builder
      .addCase(markOrderReady.pending, (state) => {
        state.error = null;
      })
      .addCase(markOrderReady.fulfilled, (state, action) => {
        const index = state.orders.findIndex(order => order.orderId === action.payload.orderId);
        if (index !== -1) {
          state.orders[index] = action.payload;
        }
      })
      .addCase(markOrderReady.rejected, (state, action) => {
        state.error = action.error.message || 'Failed to mark order ready';
      });
  },
});

export const { setFilter, clearError, setCurrentBranchId } = ordersSlice.actions;
export default ordersSlice.reducer;