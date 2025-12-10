import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface Staff {
  id: string;
  name: string;
  role: string;
  phone: string;
  email: string;
}

interface Restaurant {
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
  menuItems?: string[];
  staff: Staff[];
}

interface RestaurantState {
  restaurant: Restaurant | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: RestaurantState = {
  restaurant: null,
  isLoading: false,
  error: null,
};

const restaurantSlice = createSlice({
  name: 'restaurant',
  initialState,
  reducers: {
    setRestaurant: (state, action: PayloadAction<Restaurant>) => {
      state.restaurant = action.payload;
    },
    updateRestaurant: (state, action: PayloadAction<Partial<Restaurant>>) => {
      if (state.restaurant) {
        state.restaurant = { ...state.restaurant, ...action.payload };
      }
    },
    addStaff: (state, action: PayloadAction<Staff>) => {
      if (state.restaurant) {
        state.restaurant.staff.push(action.payload);
      }
    },
    updateStaff: (state, action: PayloadAction<{ id: string; updates: Partial<Staff> }>) => {
      if (state.restaurant) {
        const index = state.restaurant.staff.findIndex(s => s.id === action.payload.id);
        if (index !== -1) {
          state.restaurant.staff[index] = { ...state.restaurant.staff[index], ...action.payload.updates };
        }
      }
    },
    removeStaff: (state, action: PayloadAction<string>) => {
      if (state.restaurant) {
        state.restaurant.staff = state.restaurant.staff.filter(s => s.id !== action.payload);
      }
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
  },
});

export const { setRestaurant, updateRestaurant, addStaff, updateStaff, removeStaff, setLoading, setError } = restaurantSlice.actions;
export default restaurantSlice.reducer;