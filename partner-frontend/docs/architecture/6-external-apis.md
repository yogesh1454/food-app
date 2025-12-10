# 6\. External APIs

## MapmyIndia API

  - **Purpose:** Provides mapping, geolocation, and routing services for delivery management.
  - **Documentation:** (To be provided by user or found via search)
  - **Base URL(s):** `https://apis.mapmyindia.com`
  - **Authentication:** API Key (via headers or query parameters).
  - **Rate Limits:** Depends on subscription plan (e.g., QPM/RPM).

**Key Endpoints Used:**

  - `GET /v3/places/geocode` - Geocoding addresses to coordinates.
  - `GET /v3/places/nearby` - Finding nearby points of interest (e.g., vendors, delivery partners).
  - `GET /v3/routes` - Calculating optimal delivery routes.
  - `GET /v3/directions` - Providing turn-by-turn navigation.

**Integration Notes:** Implement retries with exponential backoff for transient errors. Cache frequently accessed static map data where permissible. Monitor usage against rate limits.

## UPI Payment Gateway (e.g., Razorpay, PayU) API

  - **Purpose:** Facilitates UPI-based payments, transaction management, and refunds.
  - **Documentation:** (Specific to chosen provider, e.g., Razorpay API Docs)
  - **Base URL(s):** (Specific to chosen provider, e.g., `https://api.razorpay.com/v1`)
  - **Authentication:** API Key and Secret (Basic Auth or OAuth).
  - **Rate Limits:** Depends on provider and account tier.

**Key Endpoints Used:**

  - `POST /orders` - Create a payment order.
  - `GET /payments/{payment_id}` - Fetch payment status.
  - `POST /payments/{payment_id}/capture` - Capture authorized payment.
  - `POST /refunds` - Initiate a refund.

**Integration Notes:** Ensure secure handling of API keys. Implement webhooks for real-time payment status updates. Handle idempotency for payment creation and capture to prevent duplicate transactions.

## Firebase Cloud Messaging (FCM) API

  - **Purpose:** Send push notifications to customer and delivery partner mobile applications.
  - **Documentation:** [https://firebase.google.com/docs/cloud-messaging](https://firebase.google.com/docs/cloud-messaging)
  - **Base URL(s):** `https://fcm.googleapis.com/v1/projects/{project-id}/messages:send`
  - **Authentication:** OAuth 2.0 with service account keys.
  - **Rate Limits:** High, but monitor for bursts.

**Key Endpoints Used:**

  - `POST /v1/projects/{project-id}/messages:send` - Send a message to a specific device or topic.

**Integration Notes:** Manage FCM device tokens securely. Implement topics for broadcasting notifications to groups (e.g., all delivery partners). Handle token expiration and invalidation.

## SendGrid API

  - **Purpose:** Send transactional emails (e.g., order confirmations, status updates).
  - **Documentation:** [https://docs.sendgrid.com/api-reference/](https://docs.sendgrid.com/api-reference/)
  - **Base URL(s):** `https://api.sendgrid.com/v3`
  - **Authentication:** API Key.
  - **Rate Limits:** Based on plan.

**Key Endpoints Used:**

  - `POST /mail/send` - Send emails.

**Integration Notes:** Use templates for common email types. Monitor delivery rates and bounces.

## Gupshup API

  - **Purpose:** Send SMS and WhatsApp messages for notifications.
  - **Documentation:** [https://www.gupshup.io/docs](https://www.gupshup.io/docs)
  - **Base URL(s):** `https://api.gupshup.io/sm/api/v1/msg`
  - **Authentication:** API Key/Basic Auth.
  - **Rate Limits:** Specific to account plan.

**Key Endpoints Used:**

  - `POST /sm/api/v1/msg` - Send SMS/WhatsApp messages.

**Integration Notes:** Ensure compliance with local messaging regulations. Manage message templates effectively.
