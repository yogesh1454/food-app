# Story: BE-VENDOR-04 — Orders Read + Reporting (OCVMS)

## Scope
- List vendor orders: `GET /orders?vendorId=&status=&from=&to=`
- Order detail: `GET /orders/{orderId}` (vendor-scoped)
- Reporting: `GET /reports/daily-sales`, `GET /reports/top-items`

## Data
- Ensure indexes on `orders.vendor_id`, `orders.order_status`, `orders.ordered_at`

## Acceptance Criteria
- Pagination and filtering implemented; validated input
- Read-only; consistent error model
- OpenAPI examples for lists and aggregates
- Unit + integration tests (repository/service/controller) 