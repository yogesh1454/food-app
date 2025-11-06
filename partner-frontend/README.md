# Nashtto Restaurant Partner App

A React Native Expo application for restaurant partners to manage their business operations including orders, menu, analytics, and profile settings.

## 🚀 Features

- **Dashboard**: Real-time analytics, revenue tracking, and performance insights
- **Menu Management**: Easy menu creation, editing, and organization
- **Order Management**: Real-time order tracking and status updates
- **Profile Settings**: Restaurant information and configuration management
- **Mobile-First Design**: Optimized for touch interactions and mobile UX
- **Offline Support**: Works seamlessly even with poor network connectivity
- **Cross-Platform**: Compatible with both iOS and Android devices

## 🛠 Technology Stack

- **React Native** - Cross-platform mobile framework
- **Expo** - Development platform and build tools
- **Redux Toolkit** - State management
- **React Navigation** - Navigation library with bottom tab navigation
- **TypeScript** - Type-safe development
- **Expo Linear Gradient** - Beautiful gradient components
- **React Native Vector Icons** - Icon library

## 📁 Project Structure

```
src/
├── components/          # Reusable UI components
├── screens/            # Screen components
│   ├── WelcomeScreen.tsx
│   ├── OnboardingScreen.tsx
│   ├── DashboardScreen.tsx
│   ├── MenuScreen.tsx
│   ├── OrdersScreen.tsx
│   └── ProfileScreen.tsx
├── store/              # Redux store and slices
│   ├── slices/
│   │   ├── authSlice.ts
│   │   ├── restaurantSlice.ts
│   │   ├── ordersSlice.ts
│   └── menuSlice.ts
│   └── index.ts
├── navigation/         # Navigation configuration
│   └── AppNavigator.tsx
├── services/           # API services and utilities
│   └── api.ts
├── utils/              # Utility functions
│   └── index.ts
├── constants/          # App constants and configuration
│   └── index.ts
└── App.tsx            # Main app component
```

## 🚀 Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Expo CLI
- iOS Simulator (for iOS development) or Android Emulator/Device (for Android development)

### Installation

1. **Install dependencies**
   ```bash
   npm install
   ```

2. **Start the development server**
   ```bash
   npx expo start
   ```

3. **Run on specific platforms**
   ```bash
   # For iOS
   npx expo run:ios

   # For Android
   npx expo run:android

   # For web (if needed)
   npx expo start --web
   ```

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory:

```env
API_BASE_URL=https://api.nashtto.com
ENVIRONMENT=development
```

### API Configuration

The app uses a mock API service that can be easily replaced with actual backend APIs. Update the `src/services/api.ts` file to connect to your backend.

## 🎨 Design System

### Colors

- Primary: `#16a34a` (Green)
- Primary Dark: `#15803d`
- Secondary: `#f59e0b` (Orange)
- Background: `#f9fafb`
- Surface: `#ffffff`
- Text: `#111827`

### Typography

- Headings: Bold, various sizes
- Body: Regular weight
- Captions: Smaller, muted text

### Spacing

- Consistent spacing scale: 4, 8, 16, 24, 32, 48px
- Responsive spacing based on screen size

## 🔄 State Management

The app uses Redux Toolkit for state management:

- **authSlice**: Authentication state
- **restaurantSlice**: Restaurant profile and settings
- **ordersSlice**: Order management and filtering
- **menuSlice**: Menu items and categories

## 🧭 Navigation

Bottom tab navigation with the following screens:

1. **Dashboard** - Analytics and insights
2. **Menu** - Menu management
3. **Orders** - Order tracking and management
4. **Profile** - Settings and configuration

## 🔌 API Integration

### Mock API Service

The app includes a comprehensive mock API service (`src/services/api.ts`) that simulates:

- Authentication endpoints
- Restaurant profile management
- Order CRUD operations
- Menu management
- Analytics and reporting

### Replacing with Real APIs

To integrate with actual backend APIs:

1. Update the API endpoints in `src/services/api.ts`
2. Replace mock responses with actual HTTP calls
3. Add error handling and retry logic
4. Implement proper authentication headers

## 📱 Mobile Features

### Responsive Design

- Optimized for various screen sizes
- Touch-friendly interactions
- Proper spacing and typography scaling

### Performance Optimizations

- Lazy loading of screens
- Optimized images and assets
- Efficient state management
- Minimal re-renders

### Offline Support

- Local data persistence
- Offline queue for API requests
- Sync when connection is restored

## 🧪 Testing

```bash
# Run tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

## 🚀 Deployment

### Building for Production

```bash
# Build for iOS
npx expo build:ios

# Build for Android
npx expo build:android
```

### Publishing Updates

```bash
# Publish update
npx expo publish

# Build and submit to stores
npx expo build:ios --type archive
npx expo build:android --type apk
```

## 🔒 Security

- Secure storage of authentication tokens
- API request validation
- Input sanitization
- Error handling without information leakage

## 📊 Analytics

The app includes built-in analytics for:

- User engagement tracking
- Performance monitoring
- Error reporting
- Feature usage statistics

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if necessary
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the documentation

## 🔄 Migration from Web Version

This React Native app was converted from a React web application with the following changes:

- **UI Components**: Converted from Radix UI + Tailwind CSS to React Native components
- **Navigation**: Implemented React Navigation with bottom tabs instead of sidebar
- **Styling**: Replaced CSS classes with React Native StyleSheet
- **State Management**: Maintained Redux Toolkit structure
- **API Layer**: Created mock API service for mobile compatibility
- **Assets**: Optimized images and icons for mobile platforms

The core functionality and business logic remain the same while providing a native mobile experience.