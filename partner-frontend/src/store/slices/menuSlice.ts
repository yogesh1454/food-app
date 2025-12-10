import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import { MenuItem, MenuItemCreateRequest, MenuItemUpdateRequest } from '../../core/types/api';
import { apiService } from '../../core/api/unifiedApiService';

interface MenuState {
  items: MenuItem[];
  categories: string[];
  isLoading: boolean;
  error: string | null;
  selectedCategory: string | null;
  currentBranchId: number | null;
}

const initialState: MenuState = {
  items: [],
  categories: [],
  isLoading: false,
  error: null,
  selectedCategory: null,
  currentBranchId: null,
};

// Async thunks for API operations (ONLY IMPLEMENTED ENDPOINTS)
export const fetchMenuItems = createAsyncThunk(
  'menu/fetchMenuItems',
  async (params: {
    branchId: number;
    category?: string;
  }) => {
    const response = await apiService.getBranchMenuItems(
      params.branchId,
      { category: params.category }
    );
    return response.data; // Backend returns List<MenuItemResponse>, not paginated
  }
);

export const createMenuItem = createAsyncThunk(
  'menu/createMenuItem',
  async (params: {
    branchId: number;
    menuItemData: MenuItemCreateRequest;
  }) => {
    const response = await apiService.createMenuItem(params.branchId, params.menuItemData);
    return response.data;
  }
);

export const updateMenuItem = createAsyncThunk(
  'menu/updateMenuItem',
  async (params: {
    menuItemId: number; // Changed from string to number
    menuItemData: MenuItemUpdateRequest;
  }) => {
    const response = await apiService.updateMenuItem(params.menuItemId, params.menuItemData);
    return response.data;
  }
);

export const deleteMenuItem = createAsyncThunk(
  'menu/deleteMenuItem',
  async (menuItemId: number) => { // Changed from string to number
    await apiService.deleteMenuItem(menuItemId);
    return menuItemId;
  }
);

// NOTE: The following thunks are NOT implemented yet
export const fetchMenuItemCategories = createAsyncThunk(
  'menu/fetchCategories',
  async (branchId: number) => {
    const response = await apiService.getMenuItemCategories(branchId);
    return response.data;
  }
);

export const searchMenuItems = createAsyncThunk(
  'menu/searchMenuItems',
  async (params: {
    branchId: number;
    query: string;
  }) => {
    const response = await apiService.searchMenuItems(params.branchId, params.query);
    return response.data;
  }
);

const menuSlice = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    setSelectedCategory: (state, action: PayloadAction<string | null>) => {
      state.selectedCategory = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
    setCurrentBranchId: (state, action: PayloadAction<number>) => {
      state.currentBranchId = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch menu items
    builder
      .addCase(fetchMenuItems.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchMenuItems.fulfilled, (state, action) => {
        state.isLoading = false;
        state.items = action.payload.items;
        state.currentBranchId = action.payload.branchId;
      })
      .addCase(fetchMenuItems.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch menu items';
      });

    // Fetch categories
    builder
      .addCase(fetchMenuItemCategories.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchMenuItemCategories.fulfilled, (state, action) => {
        state.isLoading = false;
        state.categories = ['All', ...action.payload];
      })
      .addCase(fetchMenuItemCategories.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch categories';
      });

    // Create menu item
    builder
      .addCase(createMenuItem.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createMenuItem.fulfilled, (state, action) => {
        state.isLoading = false;
        state.items.push(action.payload);
      })
      .addCase(createMenuItem.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to create menu item';
      });

    // Update menu item
    builder
      .addCase(updateMenuItem.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateMenuItem.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.items.findIndex(item => item.menuItemId === action.payload.menuItemId);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
      })
      .addCase(updateMenuItem.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to update menu item';
      });

    // Delete menu item
    builder
      .addCase(deleteMenuItem.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteMenuItem.fulfilled, (state, action) => {
        state.isLoading = false;
        state.items = state.items.filter(item => item.menuItemId !== action.payload);
      })
      .addCase(deleteMenuItem.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to delete menu item';
      });

    // Search menu items
    builder
      .addCase(searchMenuItems.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(searchMenuItems.fulfilled, (state, action) => {
        state.isLoading = false;
        state.items = action.payload;
      })
      .addCase(searchMenuItems.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to search menu items';
      });
  },
});

export const {
  setSelectedCategory,
  clearError,
  setCurrentBranchId,
} = menuSlice.actions;
export default menuSlice.reducer;