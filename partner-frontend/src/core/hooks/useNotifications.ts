import { useEffect, useRef } from 'react';
import { Platform } from 'react-native';
import * as Notifications from 'expo-notifications';
import { useAppDispatch, useAppSelector } from '../../store';
import {
    setPushToken,
    setPermissionStatus,
    setLastNotification
} from '../../store/slices/notificationSlice';
import { notificationService } from '../services/notificationService';
import { vendorApiService } from '../api/vendorApiService';

export const useNotifications = () => {
    const dispatch = useAppDispatch();
    const { pushToken, permissionStatus } = useAppSelector(state => state.notification);
    const notificationListener = useRef<Notifications.Subscription>(undefined);
    const responseListener = useRef<Notifications.Subscription>(undefined);

    useEffect(() => {
        registerForPushNotifications();

        // Listener for incoming notifications while app is foregrounded
        notificationListener.current = notificationService.addNotificationReceivedListener(notification => {
            console.log('Notification received:', notification);
            dispatch(setLastNotification(notification));
        });

        // Listener for user interaction with notification (tapping it)
        responseListener.current = notificationService.addNotificationResponseReceivedListener(response => {
            console.log('Notification response:', response);
            // Here we can handle navigation based on notification data
            // For example, navigate to Orders screen if it's a new order
        });

        return () => {
            if (notificationListener.current) {
                notificationService.removeSubscription(notificationListener.current);
            }
            if (responseListener.current) {
                notificationService.removeSubscription(responseListener.current);
            }
        };
    }, []);

    const registerForPushNotifications = async () => {
        try {
            const token = await notificationService.registerForPushNotificationsAsync();

            if (token) {
                dispatch(setPushToken(token));
                dispatch(setPermissionStatus('granted'));

                // Register token with backend if user is logged in
                // We'll handle this in the auth flow or here if we have the user ID
                // await vendorApiService.registerPushToken(token);
            } else {
                dispatch(setPermissionStatus('denied'));
            }
        } catch (error) {
            console.error('Error registering for push notifications:', error);
            dispatch(setPermissionStatus('denied'));
        }
    };

    return {
        pushToken,
        permissionStatus,
        registerForPushNotifications
    };
};
