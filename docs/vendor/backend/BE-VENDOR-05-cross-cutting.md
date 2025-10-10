# Story: BE-VENDOR-05 — Cross-Cutting (Errors, Security, Observability, CI)

## Scope
- Error model per `docs/architecture/12-error-handling-strategy.md`
- Security per `docs/architecture/15-security.md` (JWT, RBAC)
- Logging/metrics/tracing; health endpoints
- OpenAPI publish in CI; Postman collection
- Staging seed (vendor/menu/orders) to unblock FE

## Acceptance Criteria
- `@ControllerAdvice` with standardized error payload
- Security annotations on all protected endpoints
- Prometheus metrics and health ready
- OpenAPI artifact available; examples included
- Seed scripts documented and runnable 