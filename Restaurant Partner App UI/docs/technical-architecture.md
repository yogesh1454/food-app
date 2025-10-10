# Restaurant Partner App - Technical Architecture Document

## Overview

The Restaurant Partner App is a comprehensive web application designed for restaurant partners to manage their operations, orders, menu, and business analytics. The application is built using modern React technologies with a focus on scalability, maintainability, and user experience.

## Application Architecture

### Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Frontend Framework** | React 18.3.1 | Core UI framework |
| **Build Tool** | Vite 6.3.5 | Fast development and building |
| **Styling** | Tailwind CSS | Utility-first CSS framework |
| **UI Components** | Radix UI + Custom Components | Accessible, composable UI primitives |
| **Icons** | Lucide React | Modern icon library |
| **Forms** | React Hook Form | Form handling and validation |
| **Charts** | Recharts | Data visualization |
| **State Management** | React useState/Context | Client-side state management |
| **TypeScript** | TypeScript | Type safety and developer experience |

### Project Structure

```
src/
├── components/
│   ├── ui/                    # Reusable UI components (Radix-based)
│   ├── figma/                 # Figma-specific components
│   ├── dashboard.tsx          # Main dashboard screen
│   ├── menu-management.tsx    # Menu management functionality
│   ├── order-management.tsx   # Order processing and tracking
│   ├── profile-settings.tsx   # Restaurant profile and settings
│   ├── onboarding-flow.tsx    # Restaurant onboarding process
│   └── welcome-screen.tsx     # Landing/welcome screen
├── styles/
│   └── globals.css           # Global styles and Tailwind config
├── guidelines/
│   └── Guidelines.md         # Design and development guidelines
├── App.tsx                   # Main application component
├── main.tsx                  # Application entry point
└── index.css                 # Base styles
```

## Screen Architecture

### 1. Welcome Screen (`welcome-screen.tsx`)
**Purpose**: Landing page for new restaurant partners

**Key Features**:
- Brand introduction with Nashtto logo
- Feature highlights (Easy Setup, Real-time Analytics, Efficient Orders)
- Trust indicators (AI-Powered, Secure & Reliable, Lightning Fast)
- Call-to-action to start onboarding

**Technical Implementation**:
- Gradient background with brand colors
- Responsive grid layout for feature cards
- Image fallback handling for brand assets

### 2. Onboarding Flow (`onboarding-flow.tsx`)
**Purpose**: Multi-step restaurant registration process

**Key Features**:
- 5-step progressive form
- Restaurant details collection
- Contact information setup
- License and registration verification
- Logo and photo upload
- Menu upload functionality

**Technical Implementation**:
- Multi-step wizard with progress tracking
- Form validation using React Hook Form
- File upload handling
- State management for form data persistence

### 3. Dashboard (`dashboard.tsx`)
**Purpose**: Main operational dashboard with key metrics

**Key Features**:
- Revenue tracking (Today's Revenue: ₹12,450)
- Order metrics (47 orders today)
- Average order value tracking
- Popular items analytics
- Quick action buttons
- Real-time notifications

**Technical Implementation**:
- Grid-based layout for metrics cards
- Chart integration using Recharts
- Dynamic data visualization
- Real-time updates simulation

### 4. Menu Management (`menu-management.tsx`)
**Purpose**: Complete menu item management system

**Key Features**:
- Menu item CRUD operations
- Category-based organization
- Search and filtering
- Image upload and management
- Pricing and variant management
- Availability toggles
- AI-powered nutrition insights

**Technical Implementation**:
- Tabbed interface for different views
- Modal dialogs for add/edit operations
- Image upload with fallback handling
- Complex form validation
- State management for menu items

### 5. Order Management (`order-management.tsx`)
**Purpose**: Real-time order processing and tracking

**Key Features**:
- Order status tracking (New, Preparing, Ready, Delivered)
- Customer information display
- Order details and customizations
- Delivery time estimation
- Priority management
- Communication tools

**Technical Implementation**:
- Tabbed interface for order status
- Real-time order updates
- Customer avatar and contact integration
- Order timeline visualization

### 6. Profile Settings (`profile-settings.tsx`)
**Purpose**: Restaurant profile and system configuration

**Key Features**:
- Restaurant information management
- Staff management and permissions
- Business hours configuration
- Payment settings
- Notification preferences
- Analytics and reporting

**Technical Implementation**:
- Multi-tab settings interface
- Form validation for business data
- Role-based access control simulation
- File upload for documents

## Component Architecture

### UI Component Library

The application uses a comprehensive UI component library built on Radix UI primitives:

#### Core Components
- **Card**: Container components with header, content, and description
- **Button**: Multiple variants (default, ghost, outline, destructive)
- **Input**: Form input fields with validation
- **Badge**: Status indicators and labels
- **Tabs**: Tabbed navigation interface
- **Dialog**: Modal dialogs for forms and confirmations
- **Avatar**: User profile images with fallbacks
- **Switch**: Toggle controls for settings

#### Advanced Components
- **Table**: Data display with sorting and filtering
- **Progress**: Progress indicators and loading states
- **Select**: Dropdown selection components
- **Textarea**: Multi-line text input
- **Separator**: Visual dividers
- **Alert**: Notification and status messages

### Custom Components

#### ImageWithFallback (`figma/ImageWithFallback.tsx`)
- Handles image loading errors gracefully
- Provides fallback SVG for broken images
- Maintains aspect ratio and styling
- Includes original URL tracking for debugging

## Data Flow Architecture

### State Management
- **Local State**: React useState for component-level state
- **Form State**: React Hook Form for complex form handling
- **Global State**: Context API for app-wide settings (dark mode, user preferences)

### Data Flow Patterns
1. **Parent-Child Communication**: Props for data passing
2. **Callback Functions**: Event handling between components
3. **Context Sharing**: Theme and user preferences
4. **Form Validation**: Real-time validation with error states

## Styling Architecture

### Design System
- **Color Palette**: Green-based brand colors (#16a34a, #15803d)
- **Typography**: System fonts with weight variations
- **Spacing**: Consistent spacing scale using Tailwind
- **Components**: Radix UI for accessibility and consistency

### Responsive Design
- **Mobile-First**: Responsive design starting from mobile
- **Breakpoints**: Standard Tailwind breakpoints (sm, md, lg, xl)
- **Layout**: CSS Grid and Flexbox for layouts
- **Dark Mode**: Theme switching with CSS variables

## Performance Considerations

### Optimization Strategies
- **Code Splitting**: Component-based lazy loading
- **Image Optimization**: Fallback handling and lazy loading
- **Bundle Size**: Tree shaking with Vite
- **Caching**: Browser caching for static assets

### Performance Features
- **Virtual Scrolling**: For large lists (menu items, orders)
- **Memoization**: React.memo for expensive components
- **Debouncing**: Search input optimization
- **Image Compression**: Optimized image formats

## Security Architecture

### Client-Side Security
- **Input Validation**: Form validation and sanitization
- **XSS Prevention**: React's built-in XSS protection
- **CSRF Protection**: Token-based request validation
- **Secure Storage**: Local storage encryption for sensitive data

### Data Protection
- **PII Handling**: Secure handling of customer information
- **File Upload**: Secure file upload with type validation
- **API Security**: Secure API communication patterns

## Integration Points

### External Services
- **Payment Gateway**: Integration for transaction processing
- **Maps API**: Location services for delivery tracking
- **Notification Service**: Real-time notifications
- **File Storage**: Image and document storage
- **Analytics**: Business intelligence and reporting

### API Architecture
- **RESTful APIs**: Standard REST endpoints
- **WebSocket**: Real-time order updates
- **File Upload**: Multipart form data handling
- **Authentication**: JWT-based authentication

## Scalability Considerations

### Frontend Scalability
- **Component Reusability**: Shared component library
- **State Management**: Scalable state architecture
- **Code Organization**: Feature-based structure
- **Performance**: Optimization for large datasets

### Backend Integration
- **API Design**: RESTful and GraphQL options
- **Caching**: Client-side and server-side caching
- **Database**: Optimized queries and indexing
- **Microservices**: Service-oriented architecture

## Development Workflow

### Build Process
- **Development**: Vite dev server with hot reload
- **Production**: Optimized build with code splitting
- **Testing**: Component and integration testing
- **Deployment**: Static hosting with CDN

### Code Quality
- **TypeScript**: Type safety and better DX
- **ESLint**: Code quality and consistency
- **Prettier**: Code formatting
- **Git Hooks**: Pre-commit validation

## Future Enhancements

### Planned Features
- **Mobile App**: React Native implementation
- **PWA Support**: Progressive Web App capabilities
- **Real-time Collaboration**: Multi-user order management
- **AI Integration**: Predictive analytics and recommendations
- **Multi-language**: Internationalization support

### Technical Debt
- **State Management**: Migration to Zustand or Redux Toolkit
- **Testing**: Comprehensive test coverage
- **Documentation**: API and component documentation
- **Performance**: Advanced optimization techniques

## Conclusion

The Restaurant Partner App demonstrates a well-architected React application with:
- Modern technology stack
- Scalable component architecture
- Comprehensive feature set
- Performance optimization
- Security considerations
- Future-ready design

The application successfully addresses the core needs of restaurant partners while maintaining code quality, user experience, and scalability for future growth.