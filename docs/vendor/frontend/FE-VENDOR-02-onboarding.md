# Story: FE-VENDOR-02 — Onboarding Wizard

## Status
- Uses mocks for vendor creation/update and menu bulk until backend BE-VENDOR-02/03 are Ready

## User Story
As a new vendor, I can complete a 5-step onboarding to activate my account and create an initial menu.

## Steps
1) Restaurant details 2) Contact info 3) Business hours 4) Documents upload (stub) 5) Initial menu (bulk items)

## Acceptance Criteria
- Stepper UI with progress, save-as-draft (local)
- Validation per field; error banners and inline errors
- Initial menu entry supports multiple items and categories
- On completion, vendor status becomes ACTIVE (if allowed) or PENDING_APPROVAL
- Matches Figma spacing, typography, control states per step

## API Integration
- Live (later): `POST /vendors`, `PUT /vendors/{vendorId}`, `PUT /vendors/{vendorId}/menu`
- Mock (now): MSW handlers for the above with deterministic responses

## MSW Handlers (example)
```ts
// msw/handlers/vendor.ts
import { http, HttpResponse } from 'msw'

export const vendorHandlers = [
  http.post('/vendors', async () =>
    HttpResponse.json({ vendor_id: 'v-123', status: 'PENDING_APPROVAL' }, { status: 201 })
  ),
  http.put('/vendors/:vendorId', async ({ params, request }) => {
    const body = await request.json()
    return HttpResponse.json({ vendor_id: params.vendorId, ...body, status: 'ACTIVE' })
  }),
  http.put('/vendors/:vendorId/menu', async ({ params, request }) => {
    const items = await request.json()
    return HttpResponse.json(items.map((it: any, i: number) => ({
      menu_item_id: `mi-${i+1}`,
      vendor_id: params.vendorId,
      ...it,
      is_available: true,
    })))
  }),
]
```

## Figma References
- Onboarding Step 1 (Restaurant Details): `https://www.figma.com/file/FILE_KEY?node-id=ONB_STEP1`
- Step 2 (Contact Info): `https://www.figma.com/file/FILE_KEY?node-id=ONB_STEP2`
- Step 3 (Business Hours): `https://www.figma.com/file/FILE_KEY?node-id=ONB_STEP3`
- Step 4 (Documents): `https://www.figma.com/file/FILE_KEY?node-id=ONB_STEP4`
- Step 5 (Initial Menu): `https://www.figma.com/file/FILE_KEY?node-id=ONB_STEP5`
- Error/Empty states: `https://www.figma.com/file/FILE_KEY?node-id=ONB_ERRORS`

Replace with actual file key and node ids.

## Component Mapping Checklist
- Stepper → shared `Stepper` with labels and progress
- Inputs → `TextField`, `Select`, `Textarea`, `Switch`, `TimePicker`
- Hours editor → reusable list with add/remove rows
- Menu item rows → shared item form fields with validation
- Upload → stubbed `FileUpload` with preview & fallback

## Visual QA Checklist
- Spacing between controls per Figma
- Validation error placement and copy match Figma
- Mobile breakpoints for each step verified
- Keyboard order aligns with visual order

## Standards to Apply
- React standards: `Restaurant Partner App UI/docs/react-standard.md`
  - Forms library, reusable components, responsive, accessibility

## Definition of Done
- Wizard fully navigable with draft persistence
- Works end-to-end on mocks; switches to live without code changes
- Unit tests for validation and step transitions
- Visual QA passed against Figma frames 