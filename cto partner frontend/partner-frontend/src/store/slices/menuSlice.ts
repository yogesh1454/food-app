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

interface MenuState {
  items: MenuItem[];
  categories: string[];
  isLoading: boolean;
  error: string | null;
  selectedCategory: string | null;
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

const menuSlice = createSlice({
  name: 'menu',
  initialState,
  reducers: {
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
} = menuSlice.actions;
export default menuSlice.reducer;