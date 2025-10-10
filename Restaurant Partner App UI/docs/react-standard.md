You are an expert React Native engineer. Help me develop a **scalable, maintainable, and reusable** React Native app targeting **mobile (iOS, Android) and web (PWA)** using **Expo** and **React Native Web**.

Please follow these **industry best practices** and enforce reusable design through component libraries, consistent theming, and shared utility layers.

---

🔷 PROJECT SETUP
- Use **Expo** with React Native Web support (`expo-router` or `react-navigation`)
- Use **TypeScript**
- Enable **absolute imports** via `tsconfig.paths`
- Support **multi-platform builds** (mobile + web)
- Use **EAS Build** for production pipelines
- Setup **Prettier**, **ESLint** (Airbnb or custom), **Husky**, **Lint-Staged** for formatting and linting

---

🔷 CODE STRUCTURE
- Organize the code in a **feature-based modular structure**
  - `/features` – business logic and screens
  - `/components` – shared, reusable UI components
  - `/shared` – design system, tokens, constants, hooks
  - `/assets` – images, icons, fonts
  - `/api` – services and endpoints
- Structure components using **Atomic Design principles** (Atoms → Molecules → Organisms)

---

🔷 REUSABILITY & SHARED LIBRARIES

✅ Use a **`shared` folder** (or library) for:

- **shared/components**: Buttons, Inputs, Modals, Cards, Avatars, Lists
- **shared/hooks**: useDebounce, useMediaQuery, useDarkMode
- **shared/utils**: formatting, date/time utils, validation
- **shared/constants**: routes, colors, dimensions
- **shared/theme**: colors, typography, spacing
- **shared/assets**:
  - `images/`: Static image assets
  - `icons/`: SVG or vector icons (preferably using `react-native-vector-icons` or `@expo/vector-icons`)
  - `fonts/`: Custom fonts registered via `expo-font`

---

🔷 STYLING & THEME SYSTEM

✅ Use:
- **NativeWind** for Tailwind-like styling across mobile & web
- OR `StyleSheet.create()` + **utility functions** for style reuse
- `Platform.select()` to support platform-specific overrides
- Define a shared **theme system** with:
  - Colors, typography, spacing
  - Light/dark theme support using `useColorScheme`
- Use **responsive design** helpers:
  - `useWindowDimensions`
  - `react-native-responsive-screen`
  - Tailwind media queries (NativeWind)

---

🔷 NAVIGATION
- Use **React Navigation 6** with shared navigation config
- Or **Expo Router** (if file-based routing preferred)
- Setup platform-aware routing for native and web
- Implement auth flow with protected screens
- Nest navigators: Stack + Bottom Tabs + Drawer as needed

---

🔷 FORM HANDLING
- Use **Formik** + **Yup** for form validation
- Wrap common inputs into reusable `TextField`, `SelectField`, `PasswordField`
- Show real-time validation messages

---

🔷 STATE & API MANAGEMENT
- Use:
  - **Zustand** or **Jotai** for client state
  - **React Query (TanStack Query)** for async data fetching and caching
  - Custom hooks for encapsulating business logic
- Centralize all API services inside `/api/` with Axios or Fetch
- Create an `apiClient` instance for reusability (with token injection, interceptors)

---

🔷 PLATFORM SUPPORT
- Use `Platform.OS` and `Platform.select()` for platform-specific behavior
- Use **SafeAreaView**, `react-native-safe-area-context` for notched devices
- Implement **web-specific navigation** and keyboard accessibility

---

🔷 PERFORMANCE
- Use `FlatList`, `SectionList` for rendering lists
- Memoize components (`React.memo`, `useMemo`, `useCallback`)
- Lazy load screens/components
- Use vector icons instead of heavy images
- Debounce inputs & search fields
- Compress and preload images using `expo-asset`

---

🔷 TESTING & DEBUGGING
- Use **Jest** and **React Native Testing Library**
- For E2E, optionally use **Detox** or **Playwright**
- Add tests for shared components and hooks
- Integrate **Sentry** or **BugSnag** for error tracking

---

🔷 FAST DEV UTILITIES & LIBRARIES

✅ Use these libraries to speed up development:

| Purpose | Library |
|--------|---------|
| Icon Library | `@expo/vector-icons`, `react-native-vector-icons` |
| Fonts | `expo-font`, Google Fonts via `@expo-google-fonts` |
| SVG Support | `react-native-svg` |
| Responsive Layout | `react-native-responsive-screen`, `NativeWind`, `useWindowDimensions` |
| Theming | `@rneui/theming`, `react-native-paper`, or custom context |
| Forms | `formik`, `yup` |
| State | `zustand`, `jotai`, `redux-toolkit` |
| Data Fetching | `@tanstack/react-query` |
| Assets | `expo-asset`, SVGs, compressed images |
| Auth | `expo-auth-session`, `expo-secure-store`, `AsyncStorage` |
| Testing | `jest`, `@testing-library/react-native` |

---

🔷 DEV EXPERIENCE
- Enable hot reloading & fast refresh
- Use **Expo Go** for mobile testing
- Use **PWA preview** for web
- Support **.env** config using `expo-constants` or `react-native-dotenv`
- Setup CI/CD for builds using GitHub Actions or Bitrise

---

📦 Based on this setup, assist me in:
1. Creating the initial reusable architecture with shared folders
2. Scaffolding common components, hooks, utilities
3. Building screen-level features by reusing shared assets & UI
4. Maintaining mobile-web compatibility with responsive styles
5. Following the above best practices for performance, styling, and accessibility
