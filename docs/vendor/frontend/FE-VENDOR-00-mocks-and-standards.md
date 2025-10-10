# Story: FE-VENDOR-00 — Mocks & Standards Bootstrap

## Goal
Guarantee adherence to Figma and `Restaurant Partner App UI/docs/react-standard.md` and enable mock-first development.

## Acceptance Criteria
- Design tokens (colors, typography, spacing) extracted from Figma and mapped to theme
- Component library usage aligned to react-standard (shared/components, hooks)
- MSW configured with handlers for Auth, Vendor, Menu, Orders
- Env flag `VITE_API_USE_MOCKS` toggles mocks without code changes
- Linting: ESLint + Prettier; Accessibility checks enabled
- Testing: Base harness for unit and E2E

## Tasks
- Setup MSW with handler modules per domain
- Create theme file from Figma tokens; wire into app provider
- Create shared form components (TextField, SelectField, PasswordField)
- Add project scripts: mock:start, mock:off
- Document developer guide in `Restaurant Partner App UI/docs/` on how to follow standards 