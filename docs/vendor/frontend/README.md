# Vendor App — Frontend Stories (Dev1)

## Working Mode
- Use mock API responses via MSW until corresponding backend endpoints are marked Ready.
- Toggle with `VITE_API_USE_MOCKS=true` in `.env`.

## Story Order
1. FE-VENDOR-01 — Auth & App Shell (uses mock initially; backend login available per BE-002-02)
2. FE-VENDOR-02 — Onboarding Wizard (mock for vendor/menu until backend M1)
3. FE-VENDOR-03 — Vendor Profile & Settings (switch to live when `GET/PUT /vendors/{vendorId}` ready)
4. FE-VENDOR-04 — Menu Management (switch to live when vendor menu endpoints ready)
5. FE-VENDOR-05 — Orders Read (switch to live when read endpoints ready)
6. FE-VENDOR-06 — Dashboard (basic reporting)
7. FE-VENDOR-07 — UX Quality, PWA & Accessibility

## Backend Readiness Signals
- Login: Ready (per `docs/stories/BE-002-02-authentication-service.md`)
- Vendor CRUD: Ready when OCVMS BE-VENDOR-02 Done
- Menu endpoints: Ready when OCVMS BE-VENDOR-03 Done
- Orders read/reporting: Ready when OCVMS BE-VENDOR-04 Done

## Files
- `FE-VENDOR-01-auth-and-shell.md`
- `FE-VENDOR-02-onboarding.md`
- `FE-VENDOR-03-profile-settings.md`
- `FE-VENDOR-04-menu-management.md`
- `FE-VENDOR-05-orders-read.md`
- `FE-VENDOR-06-dashboard.md`
- `FE-VENDOR-07-ux-pwa.md` 