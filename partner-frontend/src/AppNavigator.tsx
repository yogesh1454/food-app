import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import { useSelector } from 'react-redux';
import { RootState } from './store';

// Import screens (we'll create these next)
import WelcomeScreen from './screens/WelcomeScreen';
import OnboardingScreen from './screens/OnboardingScreen';
import PostOnboardingScreen from './screens/PostOnboardingScreen';
import UploadTestScreen from './screens/UploadTestScreen';
import DashboardScreen from './screens/DashboardScreen';
import DashboardScreenImproved from './screens/DashboardScreenImproved';
import MenuScreen from './screens/MenuScreen';
import MenuScreenImproved from './screens/MenuScreenImproved';
import OrdersScreen from './screens/OrdersScreen';
import OrdersScreenImproved from './screens/OrdersScreenImproved';
import ProfileScreen from './screens/ProfileScreen';

export type RootStackParamList = {
  Welcome: undefined;
  Onboarding: undefined;
  PostOnboarding: undefined;
  Main: undefined;
  UploadTest: undefined;
};

export type MainTabParamList = {
  Dashboard: undefined;
  Menu: undefined;
  Orders: undefined;
  Profile: undefined;
};

const Stack = createStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<MainTabParamList>();

function MainTabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          if (route.name === 'Dashboard') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Menu') {
            iconName = focused ? 'restaurant' : 'restaurant-outline';
          } else if (route.name === 'Orders') {
            iconName = focused ? 'receipt' : 'receipt-outline';
          } else if (route.name === 'Profile') {
            iconName = focused ? 'person' : 'person-outline';
          } else {
            iconName = 'ellipse-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#16a34a',
        tabBarInactiveTintColor: 'gray',
        headerShown: false,
      })}
    >
      <Tab.Screen
        name="Dashboard"
        component={DashboardScreen}
        options={{ title: 'Home' }}
      />
      <Tab.Screen
        name="Menu"
        component={MenuScreen}
        options={{ title: 'Menu' }}
      />
      <Tab.Screen
        name="Orders"
        component={OrdersScreen}
        options={{ title: 'Orders' }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileScreen}
        options={{ title: 'Profile' }}
      />
    </Tab.Navigator>
  );
}

export default function AppNavigator() {
  const isFirstTime = useSelector((state: RootState) => state.auth.isFirstTime);

  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={isFirstTime ? "Onboarding" : "Main"}
        screenOptions={{ headerShown: false }}
      >
        <Stack.Screen name="Welcome" component={WelcomeScreen} />
        <Stack.Screen name="Onboarding" component={OnboardingScreen} />
        <Stack.Screen name="PostOnboarding" component={PostOnboardingScreen} />
        <Stack.Screen name="UploadTest" component={UploadTestScreen} />
        <Stack.Screen name="Main" component={MainTabNavigator} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}