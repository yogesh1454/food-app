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

    // Get Firebase UID for per-user storage
    const { auth } = await import('../../core/config/firebase');
    const firebaseUid = auth.currentUser?.uid;

    if (firebaseUid) {
      // Persist vendorId with per-user key
      await AsyncStorage.setItem(`vendorId_${firebaseUid}`, response.data.vendorId.toString());
      console.log('[registerVendor] Saved vendorId to AsyncStorage for user', firebaseUid, ':', response.data.vendorId);

      // Also save to Firestore for recovery across reinstalls
      try {
        const { saveVendorId } = await import('../../core/utils/userUuidService');
        await saveVendorId(response.data.vendorId);
        console.log('[registerVendor] Saved vendorId to Firestore for recovery');
      } catch (firestoreError) {
        console.warn('[registerVendor] Failed to save vendorId to Firestore:', firestoreError);
        // Don't fail registration if Firestore save fails - AsyncStorage is still the primary cache
      }
    } else {
      // Fallback to legacy global key (shouldn't happen in normal flow)
      await AsyncStorage.setItem('vendorId', response.data.vendorId.toString());
      console.log('[registerVendor] Saved vendorId to AsyncStorage (legacy):', response.data.vendorId);
    }

    return response.data;
  }
);

export const hydrateRestaurant = createAsyncThunk(
  'restaurant/hydrate',
  async (_, { rejectWithValue }) => {
    try {
      // Get the current Firebase user to key storage by their UID
      const { auth } = await import('../../core/config/firebase');
      const firebaseUid = auth.currentUser?.uid;

      if (!firebaseUid) {
        console.warn('[hydrateRestaurant] No Firebase user found');
        return rejectWithValue('User not authenticated');
      }

      // Use per-user storage key instead of global key
      const vendorIdKey = `vendorId_${firebaseUid}`;
      const storedVendorId = await AsyncStorage.getItem(vendorIdKey);
      console.log('[hydrateRestaurant] Loaded vendorId from storage for user', firebaseUid, ':', storedVendorId);

      // Also check legacy global key and migrate if found
      if (!storedVendorId) {
        const legacyVendorId = await AsyncStorage.getItem('vendorId');
        if (legacyVendorId) {
          console.log('[hydrateRestaurant] Found legacy vendorId, migrating to per-user key:', legacyVendorId);
          await AsyncStorage.setItem(vendorIdKey, legacyVendorId);
          await AsyncStorage.removeItem('vendorId'); // Clean up legacy key
        }
      }

      const finalVendorId = storedVendorId || (await AsyncStorage.getItem(vendorIdKey));

      // If still no vendorId in AsyncStorage, try to recover from Firestore
      let recoveredVendorId = finalVendorId;
      if (!recoveredVendorId) {
        console.log('[hydrateRestaurant] No vendorId in AsyncStorage, checking Firestore for recovery...');
        try {
          const { getVendorId } = await import('../../core/utils/userUuidService');
          const firestoreVendorId = await getVendorId();
          if (firestoreVendorId) {
            console.log('[hydrateRestaurant] Recovered vendorId from Firestore:', firestoreVendorId);
            // Cache it back to AsyncStorage
            await AsyncStorage.setItem(vendorIdKey, firestoreVendorId.toString());
            recoveredVendorId = firestoreVendorId.toString();
          }
        } catch (firestoreError) {
          console.warn('[hydrateRestaurant] Failed to recover vendorId from Firestore:', firestoreError);
        }
      }

      if (!recoveredVendorId) {
        console.log('[hydrateRestaurant] No vendorId found for user, need to complete onboarding');
        return rejectWithValue('No vendor found. Please complete onboarding.');
      }

      const vendorId = parseInt(recoveredVendorId, 10);
      console.log('[hydrateRestaurant] Fetching vendor:', vendorId);

      try {
        const response = await vendorApiService.getVendor(vendorId);
        return response.data;
      } catch (apiError: any) {
        const errorStatus = apiError?.response?.status || apiError?.status;

        // If vendor not found (404), clear stale data so user can re-register
        if (errorStatus === 404) {
          console.warn('[hydrateRestaurant] Vendor not found (404), clearing stale data...');
          await AsyncStorage.removeItem(vendorIdKey);
          await AsyncStorage.removeItem(`branchId_${firebaseUid}`);
          await AsyncStorage.removeItem(`user_uuid_${firebaseUid}`);
          console.warn('[hydrateRestaurant] Stale data cleared. User will need to complete onboarding again.');
          return rejectWithValue('Vendor not found. Please complete onboarding.');
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

      // Get branches from vendor response (API includes branches in vendor details)
      const vendorBranches = action.payload.branches || [];
      const firstBranch = vendorBranches.length > 0 ? vendorBranches[0] : null;

      state.restaurant = {
        vendorId: action.payload.vendorId,
        branchId: firstBranch?.branchId || 0,
        name: action.payload.companyName,
        brandName: action.payload.brandName,
        cuisineType: '',
        description: '',
        address: firstBranch?.address?.street || '',
        phone: firstBranch?.branchPhone || action.payload.companyPhone,
        email: firstBranch?.branchEmail || action.payload.companyEmail,
        isOpen: firstBranch?.isOpen || false,
        operatingHours: firstBranch?.operatingHours || {},
        gstNumber: action.payload.gstNumber,
        staff: [],
        vendorData: action.payload,
        branchData: firstBranch,
        branches: vendorBranches,
      };

      console.log('[restaurantSlice] Hydrated with', vendorBranches.length, 'branches');
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