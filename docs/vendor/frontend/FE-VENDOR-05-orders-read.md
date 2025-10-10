# Story: FE-VENDOR-05 — Orders (Read-Only)

## Status
- Mock-first; switch to live when orders endpoints Ready

## User Story
As a vendor, I can view my orders by status with details for each order.

## Acceptance Criteria
- Tabs: New, Preparing, Ready, Delivered
- List with date/status filters and pagination
- Detail drawer with items, notes, totals, customer contact (masked)

## API Integration
- Live (later): `GET /orders?vendorId=&status=&from=&to=`, `GET /orders/{orderId}`
- Mock (now): MSW list and detail handlers with realistic data

## Definition of Done
- Filtering and pagination work smoothly
- Works with mocks and live interchangeably
- Unit tests for list and detail rendering 