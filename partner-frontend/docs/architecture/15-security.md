# 15\. Security

## Input Validation

  - **Validation Library:** Spring Validator (Hibernate Validator)
  - **Validation Location:** At the API boundary (controller layer using `@Valid` annotation) and at the service layer for critical business logic before processing.
  - **Required Rules:**
      - All external inputs MUST be validated against expected formats, types, and constraints (e.g., length, regex).
      - Validation must occur at the API boundary to reject malformed requests early.
      - Whitelist approach (allowing only known good inputs) is preferred over blacklist (blocking known bad inputs).
      - Sanitize inputs to prevent injection attacks (SQL, XSS, Command Injection).

## Authentication & Authorization

  - **Auth Method:** OAuth 2.0 (for token issuance) and JWT (JSON Web Tokens) for stateless authentication.
  - **Session Management:** JWTs are stateless; no server-side sessions. Tokens are short-lived, refreshed using refresh tokens.
  - **Required Patterns:**
      - **Role-Based Access Control (RBAC):** Implement granular authorization checks based on user roles (`CUSTOMER`, `VENDOR`, `DELIVERY_PARTNER`, `ADMIN`) using Spring Security annotations (e.g., `@PreAuthorize`).
      - **Resource-Based Authorization:** For sensitive operations, ensure the authenticated user has permission to access or modify the specific resource (e.g., a customer can only view their own orders).

## Secrets Management

  - **Development:** Environment variables or Spring Boot's `application.properties`/`application.yml` (for non-sensitive dev secrets). For local sensitive secrets, use a `.env` file excluded from version control.
  - **Production:** AWS Secrets Manager or HashiCorp Vault.
  - **Code Requirements:**
      - NEVER hardcode secrets directly in source code.
      - Access secrets via configuration service or environment variables only.
      - Ensure secrets are NOT logged or exposed in error messages.

## API Security

  - **Rate Limiting:** Implement rate limiting at the API Gateway (AWS API Gateway's throttling) to protect against DDoS attacks and brute-force attempts.
  - **CORS Policy:** Configure Cross-Origin Resource Sharing (CORS) explicitly to allow requests only from trusted origins (frontend domains). Default to disallowing all origins.
  - **Security Headers:** Enforce standard security headers (e.g., `X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security`) via API Gateway or application configuration.
  - **HTTPS Enforcement:** All communication MUST be over HTTPS. HTTP requests should be redirected to HTTPS.

## Data Protection

  - **Encryption at Rest:** All sensitive data in PostgreSQL (AWS RDS), Elasticsearch, and VectorDB should be encrypted at rest using AWS KMS or native database encryption.
  - **Encryption in Transit:** All network communication (client-to-API Gateway, inter-service, service-to-database) MUST use TLS/SSL (HTTPS). Kafka communication should also be encrypted.
  - **PII Handling:** Identify and classify Personally Identifiable Information (PII). Implement data minimization (only collect necessary PII), pseudonymization/anonymization where possible, and strict access controls.
  - **Logging Restrictions:** **NEVER** log PII, passwords, payment details, or any other highly sensitive information. Mask sensitive fields in logs where necessary.

## Dependency Security

  - **Scanning Tool:** Use Snyk or OWASP Dependency-Check in the CI/CD pipeline.
  - **Update Policy:** Regularly review and update dependencies to their latest stable versions to mitigate known vulnerabilities. Automate dependency updates for minor/patch versions.
  - **Approval Process:** New major version dependency updates or introduction of new dependencies require explicit team review and approval.

## Security Testing

  - **SAST Tool:** SonarQube (integrated into CI pipeline) for static analysis of code for security vulnerabilities.
  - **DAST Tool:** OWASP ZAP or Burp Suite (during staging deployments) for dynamic analysis of the running application for vulnerabilities.
  - **Penetration Testing:** Engage third-party security experts for periodic penetration testing and security audits.
