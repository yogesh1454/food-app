<<<<<<< HEAD
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface MenuItem {
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
  preparationTime: number; // in minutes
  allergens?: string[];
  tags?: string[];
  quantity?: number; // available quantity
  addons?: { id: string; name: string; price: number }[];
  complimentaryItems?: { id: string; name: string }[];
  nutritionInfo?: { calories: number; protein: number; carbs: number; fat: number }; // AI-analyzed
}
=======
import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import { MenuItem, MenuItemCreateRequest, MenuItemUpdateRequest } from '../../core/types/api';
import { apiService } from '../../core/api/unifiedApiService';
>>>>>>> origin/partner-frontend

interface MenuState {
  items: MenuItem[];
  categories: string[];
  isLoading: boolean;
  error: string | null;
  selectedCategory: string | null;
<<<<<<< HEAD
}

const initialState: MenuState = {
  items: [
    {
      id: 'snack1',
      name: 'Samosa',
      description: 'Crispy fried pastry with spiced potatoes',
      price: 50,
      category: 'Snacks',
      isAvailable: true,
      isVegetarian: true,
      isVegan: false,
      spiceLevel: 'medium',
      preparationTime: 10,
      quantity: 20,
    },
    {
      id: 'snack2',
      name: 'Pakora',
      description: 'Vegetable fritters',
      price: 60,
      category: 'Snacks',
      isAvailable: true,
      isVegetarian: true,
      isVegan: true,
      spiceLevel: 'mild',
      preparationTime: 8,
      quantity: 15,
    },
    {
      id: 'tea1',
      name: 'Masala Chai',
      description: 'Spiced tea with milk',
      price: 30,
      category: 'Tea',
      isAvailable: true,
      isVegetarian: true,
      isVegan: false,
      spiceLevel: 'mild',
      preparationTime: 5,
      quantity: 50,
    },
    {
      id: 'coffee1',
      name: 'Filter Coffee',
      description: 'South Indian filter coffee',
      price: 40,
      category: 'Coffee',
      isAvailable: true,
      isVegetarian: true,
      isVegan: false,
      spiceLevel: 'mild',
      preparationTime: 3,
      quantity: 30,
    },
  ],
  categories: ['All', 'Appetizers', 'Main Course', 'Desserts', 'Beverages', 'Snacks', 'Tea', 'Coffee'],
  isLoading: false,
  error: null,
  selectedCategory: 'All',
};

=======
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

>>>>>>> origin/partner-frontend
const menuSlice = createSlice({
  name: 'menu',
  initialState,
  reducers: {
<<<<<<< HEAD
    setMenuItems: (state, action: PayloadAction<MenuItem[]>) => {
      state.items = action.payload;
    },
    addMenuItem: (state, action: PayloadAction<MenuItem>) => {
      state.items.push(action.payload);
    },
    updateMenuItem: (state, action: PayloadAction<{ id: string; updates: Partial<MenuItem> }>) => {
      const index = state.items.findIndex(item => item.id === action.payload.id);
      if (index !== -1) {
        state.items[index] = { ...state.items[index], ...action.payload.updates };
      }
    },
    deleteMenuItem: (state, action: PayloadAction<string>) => {
      state.items = state.items.filter(item => item.id !== action.payload);
    },
    setCategories: (state, action: PayloadAction<string[]>) => {
      state.categories = action.payload;
    },
    setSelectedCategory: (state, action: PayloadAction<string | null>) => {
      state.selectedCategory = action.payload;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
  },
});

export const {
  setMenuItems,
  addMenuItem,
  updateMenuItem,
  deleteMenuItem,
  setCategories,
  setSelectedCategory,
  setLoading,
  setError,
=======
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
>>>>>>> origin/partner-frontend
} = menuSlice.actions;
export default menuSlice.reducer;