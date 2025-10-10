# Vendor App — Backend Epics (Dev2)

Aligned to Tea & Snacks Architecture: `docs/architecture/index.md`, Tech Stack `docs/architecture/3-tech-stack.md`, DB Schema `docs/architecture/9-database-schema.md`, Error Handling `docs/architecture/12-error-handling-strategy.md`, Security `docs/architecture/15-security.md`, Coding Standards `docs/architecture/13-coding-standards.md`.

## Service Scope
- UMS: Authentication/JWT; profile minimal (`GET /users/{userId}`)
- OCVMS: Vendor + Menu + Orders read + Reporting
- SDS: Event consumers only (indexing later)

## Common Contracts
- Security: bearerAuth (JWT) on protected routes
- Errors: { code, message, details } (per 12-error-handling)
- Pagination: page, size, total, items[]

## Epic BE-1: Auth & Identity (UMS)
- APIs
  - `POST /auth/login` → 200 { access_token, refresh_token, token_type, expires_in }
  - `POST /auth/refresh` → 200 { access_token, expires_in }
  - `GET /users/{userId}` → 200 User
- Requirements
  - Roles include VENDOR; RBAC enforced downstream
  - OpenAPI securitySchemes configured; examples present

## Epic BE-2: Vendor (Restaurant) Management (OCVMS)
- APIs
  - `POST /vendors` → 201 Vendor
  - `GET /vendors/{vendorId}` → 200 Vendor | 404
  - `PUT /vendors/{vendorId}` → 200 Vendor | 403 on ownership
  - `PATCH /vendors/{vendorId}/status` → 200 Vendor
  - `GET /vendors?q&status&latitude&longitude&radius` → 200 { items: Vendor[], page, size, total }
- Data
  - `vendors` JSONB `address`, `operating_hours`; indexes: user_id, name, status; optional geo
- Caching
  - Redis `vendor:{vendorId}`; evict on update
- Events
  - `VendorUpdated` published
- Tests
  - Controller, Service, Repository; ownership guard tests; Testcontainers

## Epic BE-3: Menu Management (OCVMS)
- APIs
  - `GET /vendors/{vendorId}/menu` → 200 MenuItem[]
  - `PUT /vendors/{vendorId}/menu` → 200 MenuItem[] (bulk upsert; all-or-nothing MVP)
  - `POST /menu-items` | `GET/PUT/DELETE /menu-items/{id}` (optional)
- Data
  - `menu_items` with indexes on vendor_id, name, category
- Caching
  - `vendor:{vendorId}:menu:v{menu_version}`; bump version on write
- Events
  - `MenuItemUpdated` published
- Tests
  - Bulk validation, transactionality, cache invalidation; Testcontainers

## Epic BE-4: Orders Read + Reporting (OCVMS)
- APIs
  - `GET /orders?vendorId&status&from&to&page&size` → 200 { items: Order[], page, size, total }
  - `GET /orders/{orderId}` → 200 Order | 404 | 403 if not vendor-owned
  - `GET /reports/daily-sales?vendorId&date` → 200 { date, vendorId, totalRevenue, totalOrders, aov }
  - `GET /reports/top-items?vendorId&range` → 200 [{ menu_item_id, name, qty, revenue }]
- Data
  - Indexes: vendor_id, order_status, ordered_at
- Tests
  - Repo queries, aggregation accuracy; security scope checks

## Epic BE-5: Cross-Cutting (Errors, Security, Observability, CI)
- Errors: `@ControllerAdvice` with codes; examples in OpenAPI
- Security: `@PreAuthorize`, role checks, ownership util
- Observability: health, metrics, structured logs; correlation IDs
- CI: OpenAPI artifact, Postman collection, unit+integration gates
- Staging seed scripts for vendor/menu/orders 