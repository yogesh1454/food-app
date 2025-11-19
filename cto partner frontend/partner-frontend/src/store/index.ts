import { configureStore } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import restaurantSlice from './slices/restaurantSlice';
import ordersSlice from './slices/ordersSlice';
import menuSlice from './slices/menuSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    restaurant: restaurantSlice,
    orders: ordersSlice,
    menu: menuSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;