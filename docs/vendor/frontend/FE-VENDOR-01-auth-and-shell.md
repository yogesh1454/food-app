# Story: FE-VENDOR-01 — Authentication & App Shell

## Status
- Backend login: Implemented (see `docs/stories/BE-002-02-authentication-service.md`)
- Frontend: Start with MSW mocks; switch to live once base URL/CORS confirmed

## User Story
As a vendor, I can log in and access protected vendor routes within an application shell.

## Acceptance Criteria
- Login screen with email/phone + password
- On success, store JWT, hydrate user/vendor context, navigate to dashboard
- Protected routes (onboarding, profile, menu, orders) gated by auth
- Role guard for VENDOR; show error on unauthorized
- App shell with nav, header, and route outlet
- Matches Figma spacing, typography, and interactive states (see Figma References)

## API Integration
- Live:
  - `POST /auth/login` (ready)
  - `GET /users/{userId}` (profile minimal)
- Mock (MSW):
  - `POST /auth/login` returns sample token while backend env not reachable
  - `GET /users/{userId}` returns vendor-linked user stub

## Tasks
- Create `apiClient` with interceptors (auth header, error normalization)
- Implement `AuthContext` and route guards
- Build Login screen; success path to `/dashboard`
- Wire MSW handlers behind `VITE_API_USE_MOCKS`
- Basic unit tests for auth flows

## MSW Handlers (example)
```ts
// msw/handlers/auth.ts
import { http, HttpResponse } from 'msw'

export const authHandlers = [
  http.post('/auth/login', async () => {
    return HttpResponse.json({
      access_token: 'mock-token',
      refresh_token: 'mock-refresh',
      token_type: 'Bearer',
      expires_in: 3600,
    })
  }),
  http.get('/users/:userId', async ({ params }) => {
    return HttpResponse.json({
      user_id: params.userId,
      user_type: 'VENDOR',
      email: 'owner@example.com',
      phone_number: '+91-9999999999',
    })
  }),
]
```

## Figma References
- Login Screen: `https://www.figma.com/file/FILE_KEY?node-id=LOGIN_NODE_ID`
- Error State (Invalid Credentials): `https://www.figma.com/file/FILE_KEY?node-id=LOGIN_ERROR_NODE_ID`
- App Shell / Header / Navigation: `https://www.figma.com/file/FILE_KEY?node-id=APP_SHELL_NODE_ID`
- Loading/Skeleton States: `https://www.figma.com/file/FILE_KEY?node-id=SHELL_SKELETON_NODE_ID`

Replace `FILE_KEY` and `*_NODE_ID` with actual values.

## Component Mapping Checklist
- Buttons → shared `Button` variant per Figma (sizes, radius, states)
- Inputs → shared `TextField` with error/help text spacing
- Alerts/Toasts → shared `Alert`/`Toast` components
- Header/Nav → reusable `AppShell` with slots

## Visual QA Checklist
- Typography and spacing tokens match Figma
- Focus rings and hover/pressed states match
- Error banners and helper texts align per spec
- Mobile breakpoints and alignment verified

## Standards to Apply
- React standards: `Restaurant Partner App UI/docs/react-standard.md`
  - Structure (features/components/shared), hooks, form libs, accessibility, performance

## Definition of Done
- Works with `VITE_API_USE_MOCKS=true` and with live backend
- Unit tests for `AuthContext` and guard logic
- Error toasts on invalid credentials
- Visual QA passed against Figma frames
- Docs updated in `docs/vendor/frontend/README.md` 