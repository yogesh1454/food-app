import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { vendorApiService } from '../../core/api/vendorApiService';
import { Vendor, Branch, VendorRegistrationRequest, BranchCreateRequest } from '../../core/types/api';
// Note: Firestore removed - API uses header-based auth, not Firestore

// Combined interface for the frontend state
export interface RestaurantStateData {
  vendorId: number;
  branchId: number;
  name: string; // Vendor company name
  brandName: string;
  cuisineType: string; // Stored in tags or metadata
  description: string; // Stored in metadata
  address: string; // Branch address formatted
  phone: string; // Branch phone
  email: string; // Branch email
  logoUrl?: string; // Vendor logo
  coverPhotoUrl?: string; // Vendor/Branch cover
  isOpen: boolean; // Branch status
  operatingHours: {
    [key: string]: { open: string; close: string; isOpen: boolean };
  };
  gstNumber?: string; // Vendor GST
  fssaiNumber?: string; // Vendor/Branch FSSAI (stored in docs)
  licenseDocuments?: string[]; // URLs of uploaded docs
  staff: Staff[];
  // Raw API objects
  vendorData: Vendor | null;
  branchData: Branch | null;
  // Multiple branches support
  branches: Branch[];
}

interface Staff {
  id: string;
  name: string;
  role: string;
  phone: string;
  email: string;
}

interface RestaurantState {
  restaurant: RestaurantStateData | null;
  isLoading: boolean;
  error: string | null;
  registrationStatus: 'idle' | 'loading' | 'success' | 'failed';
  branchStatusLoading: boolean;
}

const initialState: RestaurantState = {
  restaurant: null,
  isLoading: false,
  error: null,
  registrationStatus: 'idle',
  branchStatusLoading: false,
};

// Async Thunks

export const registerVendor = createAsyncThunk(
  'restaurant/registerVendor',
  async (data: VendorRegistrationRequest) => {
    console.log('[registerVendor] Starting vendor registration...');
    const response = await vendorApiService.createVendor(data);
    console.log('[registerVendor] Vendor created, response:', JSON.stringify(response.data, null, 2));

    // Persist vendorId to AsyncStorage only (no Firestore needed)
    await AsyncStorage.setItem('vendorId', response.data.vendorId.toString());
    console.log('[registerVendor] Saved vendorId to AsyncStorage:', response.data.vendorId);

    return response.data;
  }
);

export const hydrateRestaurant = createAsyncThunk(
  'restaurant/hydrate',
  async (_, { rejectWithValue }) => {
    try {
      const storedVendorId = await AsyncStorage.getItem('vendorId');
      console.log('[hydrateRestaurant] Loaded vendorId from storage:', storedVendorId);

      // DEV FALLBACK: Use vendorId = 1 for development if no real vendorId exists
      const isDevFallback = !storedVendorId;
      const vendorIdStr = storedVendorId || '1';
      if (isDevFallback) {
        console.warn('[DEV] No vendor ID found, using vendorId = 1 for development');
        await AsyncStorage.setItem('vendorId', vendorIdStr);
      }

      const vendorId = parseInt(vendorIdStr, 10);

      try {
        const response = await vendorApiService.getVendor(vendorId);
        return response.data;
      } catch (apiError: any) {
        const errorStatus = apiError?.response?.status || apiError?.status;

        // If vendor not found (404), clear stale data so user can re-register
        if (errorStatus === 404 && !isDevFallback) {
          console.warn('[hydrateRestaurant] Vendor not found (404), clearing stale data...');
          await AsyncStorage.removeItem('vendorId');
          await AsyncStorage.removeItem('branchId');
          // Also clear the user UUID mapping for this Firebase user
          const { auth } = await import('../../core/config/firebase');
          const firebaseUid = auth.currentUser?.uid;
          if (firebaseUid) {
            await AsyncStorage.removeItem(`user_uuid_${firebaseUid}`);
          }
          console.warn('[hydrateRestaurant] Stale data cleared. User will need to complete onboarding again.');
          return rejectWithValue('Vendor not found. Please complete onboarding.');
        }

        // If API call fails and this is a dev fallback, return a mock vendor
        if (isDevFallback || vendorId === 1) {
          console.warn('[DEV] API call failed for vendorId=1, using mock vendor data for development');
          // Return a mock vendor response so the app can continue
          return {
            vendorId: 1,
            companyName: 'Dev Restaurant',
            brandName: 'Dev Restaurant',
            legalEntityName: '',
            companyEmail: 'dev@example.com',
            companyPhone: '9999999999',
            panNumber: '',
            gstNumber: '',
            images: {},
            metadata: {},
            tags: [],
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          } as Vendor;
        }
        throw apiError;
      }
    } catch (error: any) {
      return rejectWithValue(error.message || 'Failed to hydrate restaurant');
    }
  }
);

export const createBranch = createAsyncThunk(
  'restaurant/createBranch',
  async (params: { vendorId: number; branchData: BranchCreateRequest }) => {
    const response = await vendorApiService.createBranch(params.vendorId, params.branchData);
    return response.data;
  }
);

export const toggleBranchStatus = createAsyncThunk(
  'restaurant/toggleBranchStatus',
  async (params: { branchId: number; isOpen: boolean }) => {
    const response = await vendorApiService.toggleBranchStatus(params.branchId, { isOpen: params.isOpen });
    return response.data;
  }
);

export const updateVendorProfile = createAsyncThunk(
  'restaurant/updateVendor',
  async (params: { vendorId: number; data: any }) => {
    const response = await vendorApiService.updateVendor(params.vendorId, params.data);
    return response.data;
  }
);

export const updateBranchProfile = createAsyncThunk(
  'restaurant/updateBranch',
  async (params: { vendorId: number; branchId: number; data: any }) => {
    const response = await vendorApiService.updateBranch(params.vendorId, params.branchId, params.data);
    return response.data;
  }
);

export const uploadVendorMedia = createAsyncThunk(
  'restaurant/uploadMedia',
  async (params: {
    vendorId: number;
    file: { uri: string; name: string; type: string };
    target: string;
    fileType: string;
    branchId?: number;
    additionalData?: Record<string, string>;
  }) => {
    // If branchId is provided, include it in additionalData for branch-level uploads
    const mergedAdditionalData = params.branchId
      ? { ...params.additionalData, branchId: params.branchId.toString() }
      : params.additionalData;

    const response = await vendorApiService.uploadVendorFile(
      params.vendorId,
      params.file,
      params.target,
      params.fileType,
      mergedAdditionalData
    );
    return { ...response.data, fileType: params.fileType, target: params.target, uri: params.file.uri };
  }
);

const restaurantSlice = createSlice({
  name: 'restaurant',
  initialState,
  reducers: {
    setRestaurant: (state, action: PayloadAction<RestaurantStateData>) => {
      state.restaurant = action.payload;
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
    resetRegistrationStatus: (state) => {
      state.registrationStatus = 'idle';
    }
  },
  extraReducers: (builder) => {
    // Hydrate Restaurant
    builder.addCase(hydrateRestaurant.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(hydrateRestaurant.fulfilled, (state, action) => {
      state.isLoading = false;
      state.restaurant = {
        vendorId: action.payload.vendorId,
        branchId: 1, // Default to 1 for dev fallback
        name: action.payload.companyName,
        brandName: action.payload.brandName,
        cuisineType: '',
        description: '',
        address: '',
        phone: action.payload.companyPhone,
        email: action.payload.companyEmail,
        isOpen: false,
        operatingHours: {},
        gstNumber: action.payload.gstNumber,
        staff: [],
        vendorData: action.payload,
        branchData: null,
        branches: [],
      };
    });
    builder.addCase(hydrateRestaurant.rejected, (state, action) => {
      state.isLoading = false;
      // Don't set error for hydration failure as it might just mean not logged in
      console.log('Hydration failed:', action.payload);
    });

    // Register Vendor
    builder.addCase(registerVendor.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(registerVendor.fulfilled, (state, action) => {
      state.isLoading = false;
      // Initialize restaurant state with vendor data
      state.restaurant = {
        vendorId: action.payload.vendorId,
        branchId: 0, // Will be set after branch creation
        name: action.payload.companyName,
        brandName: action.payload.brandName,
        cuisineType: '',
        description: '',
        address: '',
        phone: action.payload.companyPhone,
        email: action.payload.companyEmail,
        isOpen: false,
        operatingHours: {},
        gstNumber: action.payload.gstNumber,
        staff: [],
        vendorData: action.payload,
        branchData: null,
        branches: [],
      };
    });
    builder.addCase(registerVendor.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to register vendor';
    });

    // Create Branch
    builder.addCase(createBranch.pending, (state) => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(createBranch.fulfilled, (state, action) => {
      state.isLoading = false;
      if (state.restaurant) {
        state.restaurant.branchId = action.payload.branchId;
        state.restaurant.branchData = action.payload;
        state.restaurant.address = `${action.payload.address?.street || ''}, ${action.payload.address?.area || ''}, ${action.payload.city}`;
        state.restaurant.phone = action.payload.branchPhone;
        state.restaurant.email = action.payload.branchEmail;
        // Add to branches array
        state.restaurant.branches = [...state.restaurant.branches, action.payload];
      }
    });
    builder.addCase(createBranch.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.error.message || 'Failed to create branch';
    });

    // Update Vendor
    builder.addCase(updateVendorProfile.fulfilled, (state, action) => {
      if (state.restaurant) {
        state.restaurant.vendorData = action.payload;
        state.restaurant.name = action.payload.companyName;
        state.restaurant.brandName = action.payload.brandName;
      }
    });

    // Update Branch
    builder.addCase(updateBranchProfile.fulfilled, (state, action) => {
      if (state.restaurant) {
        state.restaurant.branchData = action.payload;
        // Update derived fields
        if (action.payload.address) {
          state.restaurant.address = `${action.payload.address.street || ''}, ${action.payload.address.area || ''}, ${action.payload.city}`;
        }
      }
    });

    // Upload Media
    builder.addCase(uploadVendorMedia.fulfilled, (state, action) => {
      if (state.restaurant) {
        if (action.payload.fileType === 'logo') {
          state.restaurant.logoUrl = action.payload.uri;
        } else if (action.payload.fileType === 'cover') {
          state.restaurant.coverPhotoUrl = action.payload.uri;
        } else if (['fssai', 'gst', 'shop_act'].includes(action.payload.fileType)) {
          state.restaurant.licenseDocuments = [
            ...(state.restaurant.licenseDocuments || []),
            action.payload.uri
          ];
        }
      }
    });

    // Toggle Branch Status
    builder.addCase(toggleBranchStatus.pending, (state) => {
      state.branchStatusLoading = true;
    });
    builder.addCase(toggleBranchStatus.fulfilled, (state, action) => {
      state.branchStatusLoading = false;
      if (state.restaurant) {
        // Update the branch in the branches array
        state.restaurant.branches = state.restaurant.branches.map(branch =>
          branch.branchId === action.payload.branchId
            ? { ...branch, isOpen: action.payload.isOpen }
            : branch
        );
        // Update branchData if it's the current branch
        if (state.restaurant.branchData?.branchId === action.payload.branchId) {
          state.restaurant.branchData.isOpen = action.payload.isOpen;
          state.restaurant.isOpen = action.payload.isOpen;
        }
      }
    });
    builder.addCase(toggleBranchStatus.rejected, (state, action) => {
      state.branchStatusLoading = false;
      state.error = action.error.message || 'Failed to toggle branch status';
    });
  },
});

export const { setRestaurant, addStaff, updateStaff, removeStaff, setLoading, setError, resetRegistrationStatus } = restaurantSlice.actions;
export default restaurantSlice.reducer;