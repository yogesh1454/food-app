# Story: BE-VENDOR-01 — Auth & Identity (UMS)

## Status
- Core login implemented (see `docs/stories/BE-002-02-authentication-service.md` — COMPLETED)
- Authorization framework implemented (see `docs/stories/BE-002-03-authorization-framework.md` — COMPLETED)

## Scope
- Confirm JWT validation across OCVMS endpoints
- Expose `GET /users/{userId}` minimal profile required by FE shell
- Ensure roles include `VENDOR` and ownership checks can be enforced downstream

## Acceptance Criteria
- JWT filter active on OCVMS vendor/menu/orders routes
- Role `VENDOR` recognized; admin bypass supported
- OpenAPI updated with security schemes and examples

## Tasks
- Wire Spring Security Resource Server in OCVMS
- Add method-level guards with `@PreAuthorize`
- Document auth requirements in OpenAPI and Postman 