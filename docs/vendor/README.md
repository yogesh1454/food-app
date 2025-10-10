# Vendor App — Execution Plan

## Goal
Deliver a production-ready Vendor App with parallel Frontend (Dev1) and Backend (Dev2) workstreams, minimizing cross-team blocking. This plan references the UI epic breakdown (`Restaurant Partner App UI/docs/epics-breakdown.md`) and backend architecture in `docs/architecture/*`.

## Tracks & Owners
- Frontend (Dev1): React + Vite app in `Restaurant Partner App UI/`
- Backend (Dev2): Spring Boot services (UMS + Order-Catalog-Service) in `tea-snacks-delivery-aggregator/`

## Milestones (No-Blocker Alignment)
- M0 (Day 0–2): Contracts Ready
  - Backend: Publish OpenAPI v0.1 for Auth, Vendor, Menu, Orders (read).
  - Frontend: Generate types; wire MSW mocks to unblock UI while APIs stabilize.
- M1 (Week 1): Auth + Vendor Profile
  - Backend: UMS login/JWT; OCVMS vendor CRUD (POST/GET/PUT/PATCH) + ownership checks.
  - Frontend: Auth + protected routes; onboarding steps 1–3; vendor profile edit.
- M2 (Week 2): Menu Management
  - Backend: Menu list + bulk upsert; single-item CRUD; cache with `menu_version`.
  - Frontend: Menu UI (list/edit/create/delete, availability toggle), optimistic updates.
- M3 (Week 3): Orders Read + Dashboard
  - Backend: `GET /orders` (vendor scoped), `GET /orders/{id}`; basic reporting.
  - Frontend: Orders tabs (New/Preparing/Ready/Delivered) + dashboard metrics.
- M4 (Week 4): Hardening & Release
  - Backend: tests, observability, CORS, staging seed; OpenAPI v1.0.
  - Frontend: E2E happy path, responsiveness, accessibility, smoke on staging.

## Dependency Matrix
- Frontend → Backend: OpenAPI, CORS, auth rules, error model, image upload (stub initially).
- Backend → Frontend: None (uses tests and Postman for validation). Frontend can proceed on mocks when backend not ready.

## Working Agreements
- OpenAPI as contract. Changes require version bump and deprecation notes.
- MSW mocks derived from OpenAPI to keep parity.
- Daily 10-min sync; shared Slack channel for contract questions.

## Deliverables
- Frontend: Onboarding, Profile, Menu Management, Orders (read), Dashboard placeholders, typed API client, tests.
- Backend: UMS auth/JWT, Vendor + Menu APIs with Redis cache, Orders read, OpenAPI, tests, staging deploy.

## References
- UI Epics: `Restaurant Partner App UI/docs/epics-breakdown.md`
- Backend Architecture: `docs/architecture/index.md`
- API Spec: `docs/architecture/8-rest-api-spec.md`
- DB Schema: `docs/architecture/9-database-schema.md`

See:
- `docs/vendor/frontend-epics.md`
- `docs/vendor/backend-epics.md` 