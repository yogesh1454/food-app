# Story: FE-VENDOR-03 — Vendor Profile & Settings

## Status
- Mock-first; switch to live when `GET/PUT /vendors/{vendorId}` is Ready

## User Story
As a vendor, I can edit my profile, business hours, and open/close status.

## Acceptance Criteria
- Tabs: Info, Hours, Status
- Form validation; confirmation on status change
- Persist updates and reflect immediately in UI
- Matches Figma layouts, spacing, and states

## API Integration
- Live (later): `GET/PUT /vendors/{vendorId}`, `PATCH /vendors/{vendorId}/status`
- Mock (now): MSW handlers return vendor object and echo updates

## Figma References
- Profile — Info Tab: `https://www.figma.com/file/FILE_KEY?node-id=PROF_INFO`
- Business Hours Editor: `https://www.figma.com/file/FILE_KEY?node-id=PROF_HOURS`
- Status Toggle / Alerts: `https://www.figma.com/file/FILE_KEY?node-id=PROF_STATUS`

Replace placeholders with actual values.

## Component Mapping Checklist
- Tabs → shared `Tabs`
- Forms → `TextField`, `Select`, `Switch`
- Hours rows → reusable row component
- Status confirmation → shared `Dialog`

## Visual QA Checklist
- Tab spacing and indicator match Figma
- Form field spacing and label alignment match
- Toggle states and alerts match Figma

## Standards to Apply
- React standards: `Restaurant Partner App UI/docs/react-standard.md`

## Definition of Done
- Full edit cycle works with mocks and live
- Unit tests for form validation and status toggles
- Visual QA passed against Figma frames 