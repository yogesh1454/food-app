# Restaurant Partner App - Testing Setup Guide

*Generated on: November 6, 2025*

## Overview

This guide documents the comprehensive testing infrastructure setup for the Restaurant Partner App. The testing framework uses Jest with React Native Testing Library to provide unit tests, integration tests, and component testing capabilities.

## Current Status

**Testing Framework**: Jest + React Native Testing Library  
**Test Coverage Target**: 80%  
**Configuration Status**: ✅ Complete  
**Dependencies**: ⏳ Pending Installation

## Installed Configuration Files

### 1. Jest Configuration (`jest.config.js`)
- Comprehensive Jest configuration for React Native
- Module path mapping for clean imports
- Coverage thresholds set to 70%
- React Native specific mocks configured

### 2. Test Setup (`__tests__/setup/setupTests.js`)
- React Navigation mocks
- AsyncStorage mocks
- Redux mocks
- Expo modules mocks
- Global test environment setup

## Required Dependencies

To enable the testing infrastructure, install these dependencies:

### Core Testing Dependencies
```bash
npm install --save-dev jest @testing-library/react-native
npm install --save-dev @testing-library/jest-native
npm install --save-dev @types/jest
npm install --save-dev jest-environment-jsdom
```

### React Native Specific
```bash
npm install --save-dev react-native-testing-library
npm install --save-dev @testing-library/native
npm install --save-dev react-test-renderer
```

### Testing Utilities
```bash
npm install --save-dev jest-mock-async-storage
npm install --save-dev react-native-mock-render
npm install --save-dev babel-jest
npm install --save-dev ts-jest
```

### Add to package.json Scripts
```json
{
  "scripts": {
    "test": "jest",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "test:ci": "jest --ci --coverage --watchAll=false"
  }
}
```

## Test Structure

The testing directory follows a clear organization:

```
__tests__/
├── setup/
│   └── setupTests.js           # Global test setup and mocks
├── components/                 # Component unit tests
│   ├── Button.test.tsx
│   ├── TextInputField.test.tsx
│   ├── FeatureGate.test.tsx
│   └── Wrapper.test.tsx
├── hooks/                      # Custom hook tests
│   ├── useAlert.test.ts
│   ├── useFeatureFlags.test.ts
│   └── useFileUpload.test.ts
├── utils/                      # Utility function tests
│   ├── formatCurrency.test.ts
│   ├── validation.test.ts
│   └── deviceDetection.test.ts
├── services/                   # Service layer tests
│   ├── api.test.ts
│   ├── httpClient.test.ts
│   └── fileUploadService.test.ts
├── integration/                # Integration tests
│   ├── navigation.test.tsx
│   ├── userFlow.test.tsx
│   └── reduxIntegration.test.tsx
└── e2e/                        # End-to-end tests (future)
    └── appFlow.test.ts
```

## Testing Patterns

### Component Testing
```typescript
// Example: Button component test structure
import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import Button from '../../src/core/components/Button';

describe('Button Component', () => {
  it('renders correctly with default props', () => {
    const { getByText } = render(
      <Button onPress={jest.fn()}>Click Me</Button>
    );
    expect(getByText('Click Me')).toBeTruthy();
  });

  it('calls onPress when clicked', () => {
    const onPress = jest.fn();
    const { getByText } = render(
      <Button onPress={onPress}>Click Me</Button>
    );
    
    fireEvent.press(getByText('Click Me'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });
});
```

### Hook Testing
```typescript
// Example: Custom hook test structure
import { renderHook, act } from '@testing-library/react-hooks';
import { useAlert } from '../../src/core/hooks/useAlert';

describe('useAlert Hook', () => {
  it('should show alert correctly', () => {
    const { result } = renderHook(() => useAlert());
    
    act(() => {
      result.current.showAlert('Test message', 'info');
    });
    
    expect(result.current).toBeDefined();
  });
});
```

### Utility Function Testing
```typescript
// Example: Utility function test
import { formatCurrency, isValidEmail } from '../../src/utils';

describe('Utility Functions', () => {
  describe('formatCurrency', () => {
    it('should format Indian Rupees correctly', () => {
      expect(formatCurrency(100)).toBe('₹100');
      expect(formatCurrency(1000)).toBe('₹1,000');
    });
  });

  describe('isValidEmail', () => {
    it('should validate email correctly', () => {
      expect(isValidEmail('test@example.com')).toBe(true);
      expect(isValidEmail('invalid-email')).toBe(false);
    });
  });
});
```

### Integration Testing
```typescript
// Example: Navigation integration test
import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { NavigationContainer } from '@react-navigation/native';
import AppNavigator from '../../src/AppNavigator';

describe('Navigation Integration', () => {
  it('should navigate between screens correctly', () => {
    const { getByText } = render(
      <NavigationContainer>
        <AppNavigator />
      </NavigationContainer>
    );
    
    // Test navigation logic
    expect(getByText('Dashboard')).toBeTruthy();
  });
});
```

## Test Data Management

### Mock Data
```typescript
// __tests__/data/mockData.ts
export const mockMenuItem = {
  id: '1',
  name: 'Test Menu Item',
  price: 25.99,
  category: 'main',
  description: 'Test description',
  isAvailable: true,
};

export const mockOrder = {
  id: 'order-1',
  customerName: 'John Doe',
  items: [mockMenuItem],
  status: 'pending' as const,
  total: 25.99,
  orderTime: new Date(),
};
```

### Mock Services
```typescript
// __tests__/mocks/apiMock.ts
export const mockApi = {
  getMenu: jest.fn().mockResolvedValue([mockMenuItem]),
  createOrder: jest.fn().mockResolvedValue(mockOrder),
  uploadFile: jest.fn().mockResolvedValue({ uri: 'mock-uri' }),
};
```

## Coverage Goals

### Target Coverage by Module
- **Components**: 80% coverage
- **Hooks**: 90% coverage  
- **Utils**: 95% coverage
- **Services**: 75% coverage
- **Screens**: 70% coverage

### Coverage Areas
- **Line Coverage**: Ensure all code lines are executed
- **Branch Coverage**: Test all conditional branches
- **Function Coverage**: All functions are called
- **Statement Coverage**: All statements are executed

## Continuous Integration

### GitHub Actions Example
```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
        with:
          node-version: '16'
      - run: npm install
      - run: npm test -- --coverage --ci
      - uses: codecov/codecov-action@v1
```

### Pre-commit Hooks
```bash
# Install husky for git hooks
npm install --save-dev husky lint-staged

# .lintstagedrc
{
  "*.{ts,tsx}": [
    "npm test -- --findRelatedTests",
    "prettier --write"
  ]
}
```

## Test Best Practices

### 1. Component Testing
- Test user interactions, not implementation details
- Use semantic queries (getByRole, getByLabelText)
- Test accessibility features
- Mock external dependencies

### 2. Hook Testing
- Test business logic independently
- Test state updates and side effects
- Use `act()` for async operations
- Test error handling

### 3. Integration Testing
- Test real user flows
- Mock network requests appropriately
- Test navigation between screens
- Verify Redux state management

### 4. Performance Testing
- Test component re-renders
- Measure rendering time
- Test memory leaks
- Profile bundle size impact

## Running Tests

### Development
```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run specific test file
npm test Button.test.tsx

# Run tests with coverage
npm run test:coverage
```

### CI/CD
```bash
# Run tests for CI (no watch mode)
npm test -- --ci --coverage --watchAll=false

# Generate coverage report
npm run test:coverage

# Fail on coverage threshold
npm test -- --coverage --coverageThreshold='{"global":{"branches":80,"functions":80,"lines":80,"statements":80}}'
```

## Troubleshooting

### Common Issues

#### 1. Module Resolution
```bash
# Ensure Jest path mapping works
npm test -- --verbose

# Check module mapping in jest.config.js
```

#### 2. Async Testing
```typescript
// Use async/await with act
await act(async () => {
  await result.current.asyncFunction();
});
```

#### 3. Navigation Testing
```typescript
// Mock navigation properly
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({
    navigate: jest.fn(),
    goBack: jest.fn(),
  }),
}));
```

#### 4. Redux Testing
```typescript
// Mock Redux store properly
jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
```

## Next Steps

### Immediate (Week 1)
1. Install all testing dependencies
2. Create initial test files for core components
3. Run first test suite successfully
4. Set up pre-commit hooks

### Short-term (Week 2-3)
1. Achieve 60% test coverage
2. Add integration tests
3. Set up CI/CD pipeline
4. Add performance tests

### Long-term (Month 2)
1. Achieve 80% test coverage
2. Add E2E testing with Detox
3. Add visual regression testing
4. Implement continuous testing

## Benefits

### Developer Benefits
- **Confidence**: Changes don't break existing functionality
- **Speed**: Faster debugging and refactoring
- **Documentation**: Tests serve as living documentation
- **Quality**: Better code quality through TDD practices

### Business Benefits
- **Reliability**: Reduced production bugs
- **Velocity**: Faster development cycles
- **Maintenance**: Lower maintenance costs
- **User Experience**: More stable app experience

This testing infrastructure provides a solid foundation for maintaining code quality and ensuring the Restaurant Partner App remains reliable as it grows and evolves.