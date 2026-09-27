# Capstone: Full End-to-End Resilience Testing & Mocking Workflow
*For: all developers and QA teams running an exhaustive, production-grade resilience test.*

## Goal

Run a complete, end-to-end resilience test against a critical application workflow (such as an e-commerce checkout), systematically validating baseline traffic, injecting targeted latency and error overrides, capturing a stable offline mock, and auditing the entire lifecycle in the Traffic Inspector.

## Prerequisites

This capstone recipe synthesizes all of EntropyLab's core capabilities. Before beginning, ensure you are familiar with:
- [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)
- [Quick Start Tutorial: Your First Route & Inspected Request](../00-orientation/04-quick-start-tutorial.md)
- [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)

---

## Step-by-Step Walkthrough

### Scenario Setup

In this capstone, we will simulate testing an online store application whose payment processing depends on an external payment provider (`https://api.stripe.com` or a similar backend). We want to guarantee that our checkout screen handles slow networks and server crashes gracefully, without breaking the rest of our store.

---

### Phase 1: Route Setup & Baseline Validation

First, we route our application's payment calls through EntropyLab to establish a healthy baseline.

1. Open EntropyLab, go to the **Proxy Control** tab, and ensure the status is green: `Running on port 8080`. (If stopped, click **Start Proxy**).
2. Click the **Routes** tab and click **Add Route**.
3. In the dialog, enter:
   - **Local Path**: `/stripe`
   - **Target Base URL**: `https://api.stripe.com`
4. Click **Add**. The route is now active.
5. In your application (or terminal/browser), trigger a healthy baseline request:
   ```text
   http://localhost:8080/stripe/v1/tokens
   ```
6. Switch to the **Inspector** tab in EntropyLab:
   - Confirm a new entry appears at the top.
   - Verify the **Status** is `200` (or `401` if unauthenticated, proving the real Stripe API was reached).
   - Verify the **Type** column reads **`FORWARDED`**. 
   - *Baseline established:* Traffic flows seamlessly through your local reverse proxy to the live internet.

---

### Phase 2: Targeted Chaos Injection (Scoped Latency + Flakiness)

Now, let's stress-test the checkout flow. We will inject a 2,500 ms delay and a 40% failure rate, but **scope it strictly to the checkout endpoint** so our product catalog and user profile calls remain fast.

1. Click the **Chaos Rules** tab.
2. Select the `/stripe` route row and click **Edit Chaos Rule** (or double-click the row).
3. In the **Latency** section:
   - Check **Enable Latency**.
   - Set **Latency (ms)** to `2500`.
4. In the **Status Override** section:
   - Check **Enable Status Override**.
   - Set **Status Code** to `500`.
   - Set **Failure %** to `40`.
5. In the **Sub-path filter (optional)** field, enter:
   ```text
   /checkout
   ```
6. Click **OK** to save the rule.
7. Confirm the **Chaos Rules** table displays:
   ```text
   Latency: 2500ms | Status: 500 (40%) | Reset: OFF | Mutation: OFF
   ```

---

### Phase 3: Observing Application UI & Error Recovery

Now, point your real application's payment client to `http://localhost:8080/stripe` and execute several transactions through your UI:

1. **Test Sister Endpoints (Catalog / Customer Details)**:
   - Browse products or fetch customer profile data (`http://localhost:8080/stripe/customers`).
   - *Observation:* These requests complete instantly (< 200 ms). The sub-path filter ensures chaos is not triggered.
2. **Test the Target Endpoint (Checkout Submission)**:
   - Click your application's **Place Order** button (`http://localhost:8080/stripe/checkout`).
   - *Observation:* 
     - **Every single checkout call pauses for 2.5 seconds.** Verify that your button shows a loading spinner, disables repeated clicks, and does not freeze the browser window.
     - **Roughly 60% of attempts**: Succeed after 2.5 seconds, redirecting to the order confirmation screen.
     - **Roughly 40% of attempts**: Fail after 2.5 seconds with HTTP `500 Internal Server Error (Chaos Override)`. Verify that your UI displays an informative error toast (e.g., *"Payment server temporarily down. Please try again."*) instead of crashing with a blank white screen.

---

### Phase 4: Auto-Mock Snapshot for Offline Development

Once your UI error handling is validated, you may want to continue styling order confirmation components without consuming live API tokens, risking remote rate limits, or requiring internet access.

1. Switch back to EntropyLab and open the **Inspector** tab.
2. Locate one of the successful baseline requests from Phase 1 (or any `FORWARDED` response with a 200 status code).
3. Click the row to select it, then click the **Save as Mock** button in the toolbar.
4. An alert confirms: `Mock saved and activated for /stripe/v1/tokens`. Click **OK**.
5. Switch to the **Mocks** tab:
   - Confirm a new entry exists for `/stripe/v1/tokens`.
   - Confirm **Auto-Generated** displays **`Yes`**, and **Enabled** is checked.
6. Now re-request `http://localhost:8080/stripe/v1/tokens`:
   - It returns **instantaneously (< 5 ms)**.
   - It serves the captured JSON payload directly from your local disk (`%APPDATA%\EntropyLab\mocks\`).
   - You can disconnect your machine from Wi-Fi completely and continue building your UI offline.

---

### Phase 5: Auditing the Full Session in the Traffic Inspector

Open the **Inspector** tab to review the historical record of your testing session. You will observe all major classifications of proxy traffic coexisting in a single unified audit log:

| Time | Method | Path | Status | Duration (ms) | Type | What This Entry Represents |
|---|---|---|---|---|---|---|
| `14:35:10` | `GET` | `/stripe/v1/tokens` | `200` | `4 ms` | **`MOCKED`** | Phase 4: Snapshot served locally from disk; zero network transit. |
| `14:34:42` | `POST` | `/stripe/checkout` | `500` | `2504 ms` | **`CHAOS_STATUS`** | Phase 3: Scoped delay (2.5s) followed by forced 500 error. |
| `14:34:18` | `POST` | `/stripe/checkout` | `200` | `2710 ms` | **`FORWARDED`** | Phase 3: Scoped delay (2.5s) followed by successful real API call. |
| `14:33:50` | `GET` | `/stripe/customers` | `200` | `185 ms` | **`FORWARDED`** | Phase 3: Sibling endpoint unaffected by sub-path chaos. |
| `14:31:02` | `GET` | `/stripe/v1/tokens` | `200` | `210 ms` | **`FORWARDED`** | Phase 1: Original live baseline request to the real API. |

Double-click any row to view its exact headers and formatted JSON body, proving that every stage of your testing session is permanently retained.

---

## Variations

- **Simulate Catastrophic Socket Severing**:
  In Phase 2, open **Edit Chaos Rule** and check **Enable Connection Reset** at `20%`. Now, on top of delays and 500 errors, roughly 1 in 5 checkout attempts will suffer a raw TCP socket drop (`CHAOS_RESET`), testing whether your frontend networking layer recovers from unhandled socket terminations.
- **Toggling Mock vs. Live Traffic**:
  In Phase 4, toggle the **Enabled** checkbox in the **Mocks** tab off and on. Notice how your application flips instantly between lightning-fast local mock data and live remote API calls with zero restarts.

---

## What You Just Achieved: Full Workflow Reflection

In this single end-to-end exercise, you have exercised every core building block of EntropyLab:

1. **Reverse Proxying**: You intercepted and relayed live internet traffic via a custom route prefix.
2. **Targeted Chaos Engineering**: You scoped a combination of latency injection and error overrides to a sensitive sub-path using the sub-path filter.
3. **Resilience Verification**: You validated your application's UI spinners, retry policies, and error banners under hostile conditions.
4. **Auto-Mock Snapshotting**: You captured live production payloads and converted them into permanent offline mocks with one click.
5. **Traffic Auditing**: You used the Inspector to analyze timing metrics, verify payload fidelity, and differentiate between forwarded, mocked, and chaos-altered requests.

You now possess the complete toolkit needed to guarantee application resilience across any service dependency.

---

## Related Reading

- [Quick Start Tutorial: Your First Route & Inspected Request](../00-orientation/04-quick-start-tutorial.md)
- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
- [Common Errors and Fixes](../07-troubleshooting/01-common-errors-and-fixes.md)
