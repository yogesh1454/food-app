# Story: FE-VENDOR-04 — Menu Management

## Status
- Mock-first; switch to live when menu endpoints Ready

## User Story
As a vendor, I can manage my menu items (list, add, edit, delete, availability) and perform bulk updates.

## Acceptance Criteria
- List with search/filter by category and availability
- Add/Edit modal with validation
- Delete with confirmation
- Availability toggle with optimistic update
- Bulk upsert from table edits
- Image upload stub with preview/fallback
- Matches Figma table, modal, and empty/error states

## API Integration
- Live (later): `GET /vendors/{vendorId}/menu`, `PUT /vendors/{vendorId}/menu`, `POST/GET/PUT/DELETE /menu-items*`
- Mock (now): MSW handlers for all above

## Figma References
- Menu List/Table: `https://www.figma.com/file/FILE_KEY?node-id=MENU_LIST`
- Item Modal: `https://www.figma.com/file/FILE_KEY?node-id=MENU_MODAL`
- Empty/Loading/Error states: `https://www.figma.com/file/FILE_KEY?node-id=MENU_STATES`

Replace placeholders with actual values.

## Component Mapping Checklist
- Table → shared `Table` with toolbar (search/filter)
- Modal → shared `Dialog` with form fields
- Image → `ImageWithFallback`
- Availability → `Switch` with immediate UI update

## Visual QA Checklist
- Row height, padding, and typography match Figma
- Modal spacing and button groups match
- Empty and error state illustrations/text match

## Standards to Apply
- React standards: `Restaurant Partner App UI/docs/react-standard.md`

## Definition of Done
- Optimistic UX; rollback on failure
- Works with mocks and live without code changes
- Unit tests for reducers and handlers
- Visual QA passed against Figma frames 