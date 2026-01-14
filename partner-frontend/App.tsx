import React from 'react';
import { Provider } from 'react-redux';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { store } from './src/store';
import AppNavigator from './src/navigation/AppNavigator';

import { useNotifications } from './src/core/hooks/useNotifications';

// Wrapper component to use the hook inside Provider
const NotificationWrapper = ({ children }: { children: React.ReactNode }) => {
  useNotifications();
  return <>{children}</>;
};

export default function App() {
  return (
    <Provider store={store}>
      <SafeAreaProvider>
        <NotificationWrapper>
          <AppNavigator />
        </NotificationWrapper>
        <StatusBar style="auto" />
      </SafeAreaProvider>
    </Provider>
  );
}