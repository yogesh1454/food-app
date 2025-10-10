# Story: BE-VENDOR-02 — Vendor Management (OCVMS)

## Scope
- CRUD vendor: `POST /vendors`, `GET/PUT /vendors/{vendorId}`, list `GET /vendors` with filters
- Status toggle: `PATCH /vendors/{vendorId}/status`
- Ownership: vendor mutations only by `vendors.user_id` or `ADMIN`

## Data
- Table `vendors` per `docs/architecture/9-database-schema.md` (JSONB `address`, `operating_hours`)
- Indexes: `user_id`, `name`, `status` (optional geo index)

## Caching & Events
- Cache `vendor:{vendorId}`, evict on update
- Publish `VendorUpdated` (Kafka) for SDS indexing (optional for MVP)

## Acceptance Criteria
- DTOs, mappers, service, repository implemented
- Validation and error handling via `@Valid` and `@ControllerAdvice`
- OpenAPI with examples
- Unit + integration tests (Testcontainers PG) 