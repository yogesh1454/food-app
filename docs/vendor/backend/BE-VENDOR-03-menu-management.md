# Story: BE-VENDOR-03 — Menu Management (OCVMS)

## Scope
- List vendor menu: `GET /vendors/{vendorId}/menu`
- Bulk upsert: `PUT /vendors/{vendorId}/menu` (transactional, all-or-nothing)
- Optional single-item CRUD: `POST/GET/PUT/DELETE /menu-items*`
- Availability toggle per item

## Data
- Table `menu_items` per schema; indexes on `vendor_id`, `name`, `category`

## Caching & Versioning
- Read cache: `vendor:{vendorId}:menu:v{menu_version}`
- On write: bump `menu_version` and evict menu cache

## Events
- Publish `MenuItemUpdated` (Kafka) on changes (future SDS)

## Acceptance Criteria
- Services and controllers implemented with validation
- Caching and invalidation verified
- OpenAPI updated with request/response examples
- Unit + integration tests (Testcontainers) 