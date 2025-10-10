# Vendor App — Frontend Epics (Dev1)

Source reference: `Restaurant Partner App UI/docs/epics-breakdown.md` and Figma vendor flows.

## Design & Standards Compliance (Mandatory)
- Figma: Implement pixel-accurate screens; reuse the design system tokens (colors, typography, spacing, radii) and components as per Figma.
- React Standards: Follow `Restaurant Partner App UI/docs/react-standard.md` for:
  - Project structure (features/components/shared)
  - Reusable components and hooks
  - Form handling (Formik/Yup or React Hook Form)
  - State and data fetching (Zustand/React Query if introduced)
  - Styling/theming (NativeWind or Tailwind + utilities), accessibility, performance
- Accessibility: Keyboard nav, aria labels, contrast, focus management.
- Responsiveness: Mobile-first; verify breakpoints.
- Testing: Unit for components and hooks; E2E for happy paths.

## Epic FE-1: Authentication & App Shell
- Figma Mapping: Login, Error states, App shell (navigation/header), Loader screens
- Stories
  - FE-VENDOR-01 Auth & App Shell (login form, route guards, role gating)
  - FE-VENDOR-00 Mocks & Standards (MSW setup, theme/tokens bootstrapping)
- Deliverables
  - `AuthContext`, API client with interceptors
  - App shell with brand header per Figma
- API deps: `POST /auth/login`, `GET /users/{userId}`

## Epic FE-2: Onboarding Wizard (5-step)
- Figma Mapping: Vendor Onboarding Step 1–5 frames, validations, error states
- Steps: Restaurant details → Contact → Business hours → Documents (stub) → Initial menu
- Stories
  - FE-VENDOR-02 Onboarding Wizard (forms, progression, draft persistence)
- Deliverables: `onboarding-flow.tsx`, reusable form fields, stepper
- API deps: `POST /vendors`, `PUT /vendors/{vendorId}`, `PUT /vendors/{vendorId}/menu`
- Notes: Start with MSW; wire images later

## Epic FE-3: Vendor Profile & Settings
- Figma Mapping: Profile tabs (Info, Hours, Status), edit flows, toasts
- Stories
  - FE-VENDOR-03 Profile & Settings (edit, hours editor, open/close)
- Deliverables: `profile-settings.tsx` tabs
- API deps: `GET/PUT /vendors/{vendorId}`, `PATCH /vendors/{vendorId}/status`

## Epic FE-4: Menu Management
- Figma Mapping: Menu list, filters, item modal, image, bulk edit
- Stories
  - FE-VENDOR-04 Menu Management (list, CRUD, availability, bulk upsert)
- Deliverables: `menu-management.tsx` + dialogs; image fallback
- API deps: `GET /vendors/{vendorId}/menu`, `PUT /vendors/{vendorId}/menu`, `POST/PUT/DELETE /menu-items*`

## Epic FE-5: Orders (Read-Only MVP)
- Figma Mapping: Orders tabs, list item design, detail drawer, empty states
- Stories
  - FE-VENDOR-05 Orders Read (tabbed list, detail view, filters)
- Deliverables: `order-management.tsx` read-only
- API deps: `GET /orders`, `GET /orders/{orderId}`

## Epic FE-6: Dashboard & Analytics (MVP)
- Figma Mapping: Metrics cards, charts, quick actions, skeletons
- Stories
  - FE-VENDOR-06 Dashboard (cards, chart, quick actions)
- Deliverables: `dashboard.tsx` with Recharts, skeleton loading
- API deps: `GET /reports/daily-sales`, `GET /reports/top-items`

## Epic FE-7: UX Quality, PWA & Accessibility
- Figma Mapping: Loaders, empty states, error banners, toasts
- Stories
  - FE-VENDOR-07 UX/PWA (skeletons, toasts, manifest, accessibility)
- Deliverables: Shared UI refinements; manifest

## Definition of Ready (per story)
- Linked Figma frames with component inventory
- Field-level validation rules; i18n text where applicable
- API shapes (OpenAPI examples) or MSW fixtures available

## Definition of Done (per story)
- Matches Figma spacing, typography, and interactive states
- Passes accessibility linting and keyboard flows
- Unit tests added; E2E flow covered where applicable
- Works with MSW and with live API by toggling env 