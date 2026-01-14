import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import { config } from '../config/environment';

// Configure notification behavior when app is in foreground
Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: true,
        shouldShowBanner: true,
        shouldShowList: true,
    }),
});

export class NotificationService {
    /**
     * Register for push notifications and return the token
     */
    async registerForPushNotificationsAsync(): Promise<string | undefined> {
        let token;

        if (Platform.OS === 'android') {
            await Notifications.setNotificationChannelAsync('default', {
                name: 'default',
                importance: Notifications.AndroidImportance.MAX,
                vibrationPattern: [0, 250, 250, 250],
                lightColor: '#FF231F7C',
            });
        }

        if (Device.isDevice) {
            const { status: existingStatus } = await Notifications.getPermissionsAsync();
            let finalStatus = existingStatus;

            if (existingStatus !== 'granted') {
                const { status } = await Notifications.requestPermissionsAsync();
                finalStatus = status;
            }

            if (finalStatus !== 'granted') {
                console.log('Failed to get push token for push notification!');
                return;
            }

            // Get the token that uniquely identifies this device
            try {
                const tokenData = await Notifications.getExpoPushTokenAsync({
                    projectId: (config as any).expoProjectId || 'd482200c-a454-46c3-a0fa-8b5ec45e159f', // Fallback to ID found in app.json if not in config
                });
                token = tokenData.data;
                console.log('Push token:', token);
            } catch (error) {
                console.error('Error fetching push token:', error);
            }
        } else {
            console.log('Must use physical device for Push Notifications');
        }

        return token;
    }

    /**
     * Add listener for incoming notifications
     */
    addNotificationReceivedListener(callback: (notification: Notifications.Notification) => void) {
        return Notifications.addNotificationReceivedListener(callback);
    }

    /**
     * Add listener for user interaction with notification
     */
    addNotificationResponseReceivedListener(callback: (response: Notifications.NotificationResponse) => void) {
        return Notifications.addNotificationResponseReceivedListener(callback);
    }

    /**
     * Remove specific listener
     */
    removeSubscription(subscription: Notifications.Subscription) {
        subscription.remove();
    }
}

export const notificationService = new NotificationService();
