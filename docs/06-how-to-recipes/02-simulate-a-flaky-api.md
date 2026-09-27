# How-To: Simulate a Flaky API
*For: developers testing automated retries, exponential backoff, and intermittent network resilience.*

## Goal

Configure EntropyLab to randomly fail roughly 30% of requests with an HTTP `503 Service Unavailable` error, simulating an unstable, overloaded third-party service to verify your client application's retry logic.

## Prerequisites

Before following this recipe, ensure you understand:
- How to create and enable a proxy route (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- How status overrides and probabilistic failures work (see [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy status is green: `Running on port 8080`.
2. Click the **Routes** tab and ensure you have an active route (e.g., **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Click the **Chaos Rules** tab in the top navigation bar.
4. Select the row for your route (e.g., `/github`) and click **Edit Chaos Rule** (or double-click the row).
5. In the dialog, check the **Enable Status Override** checkbox.
6. In the **Status Code** dropdown, select:
   ```text
   503
   ```
   *(Why 503? HTTP 503 Service Unavailable is the standard code returned by real-world load balancers when upstream servers are overloaded or rate-limiting traffic).*
7. In the **Failure %** spinner, enter:
   ```text
   30
   ```
8. Click **OK** to save the rule.
9. Verify that the **Chaos Rules** table displays:
   ```text
   Latency: OFF | Status: 503 (30%) | Reset: OFF | Mutation: OFF
   ```
10. Open your web browser, terminal, or API testing tool, and make 10 consecutive requests in quick succession to:
    ```text
    http://localhost:8080/github/users/octocat
    ```

---

## Expected Result

- **In Your Client / Browser**: 
  - Approximately 7 out of 10 requests succeed normally, returning `200 OK` and displaying GitHub's JSON user profile.
  - Approximately 3 out of 10 requests fail immediately with an HTTP `503 Service Unavailable` error containing the body `Service Unavailable (Chaos Override)`.
  - The failures occur randomly across your test sequence (for example, calls 2, 6, and 9 might fail).
- **In EntropyLab's Inspector**:
  - Open the **Inspector** tab to view your 10 requests.
  - Successful calls appear with Status **`200`** and Type **`FORWARDED`**.
  - Failed calls appear with Status **`503`**, Type **`CHAOS_STATUS`**, and execution duration under `10 ms` (confirming EntropyLab faked the failure locally without touching GitHub).

---

## Variations

- **Want to test worst-case complete outage?**
  Set **Failure %** to `100`. Every single call will deterministically return a `503` error. This is ideal when styling an outage alert banner or verifying that a critical checkout workflow prevents submission during down-time.
- **Want to simulate an edge gateway timeout?**
  Change the **Status Code** dropdown from `503` to `504` (Gateway Timeout). Use this to test whether your application distinguishes between a temporarily unavailable service (which can be retried) and a timed-out gateway.
- **Want to make the flakiness feel like a struggling network?**
  In the same **Edit Chaos Rule** dialog, check **Enable Latency** and set **Latency (ms)** to `1500`. Now, every request experiences a 1.5-second struggle, and 30% of those slow requests ultimately fail with a 503 error.

---

## Related Reading

- [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)
- [How-To: Simulate a Slow API](./01-simulate-a-slow-api.md)
- [How-To: Combine Latency and Failure](./04-combine-latency-and-failure.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
