import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface NotificationState {
    pushToken: string | null;
    permissionStatus: 'granted' | 'denied' | 'undetermined';
    unreadCount: number;
    lastNotification: any | null;
}

const initialState: NotificationState = {
    pushToken: null,
    permissionStatus: 'undetermined',
    unreadCount: 0,
    lastNotification: null,
};

const notificationSlice = createSlice({
    name: 'notification',
    initialState,
    reducers: {
        setPushToken: (state, action: PayloadAction<string | null>) => {
            state.pushToken = action.payload;
        },
        setPermissionStatus: (state, action: PayloadAction<'granted' | 'denied' | 'undetermined'>) => {
            state.permissionStatus = action.payload;
        },
        incrementUnreadCount: (state) => {
            state.unreadCount += 1;
        },
        resetUnreadCount: (state) => {
            state.unreadCount = 0;
        },
        setLastNotification: (state, action: PayloadAction<any>) => {
            state.lastNotification = action.payload;
            state.unreadCount += 1;
        },
    },
});

export const {
    setPushToken,
    setPermissionStatus,
    incrementUnreadCount,
    resetUnreadCount,
    setLastNotification
} = notificationSlice.actions;

export default notificationSlice.reducer;
