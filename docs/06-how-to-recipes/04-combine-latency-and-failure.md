# How-To: Combine Latency and Failure
*For: developers and QA engineers testing compound network degradation and stacked chaos scenarios.*

## Goal

Configure EntropyLab to simulate the most realistic production failure mode: an upstream API that is both sluggish (a 2,000 ms delay on all requests) and intermittently fails (a 25% failure probability with HTTP 500 errors).

## Prerequisites

Before following this recipe, ensure you understand:
- How to manage routes (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- How artificial delay is injected (see [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md)).
- How status overrides operate (see [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)).
- How chaos features stack in the execution pipeline (see [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy status reads: `Running on port 8080`.
2. Click the **Routes** tab and ensure you have an active route (e.g., **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Click the **Chaos Rules** tab in the top navigation bar.
4. Select the row for your route (e.g., `/github`) and click **Edit Chaos Rule** (or double-click the row).
5. In the **Latency** section:
   - Check the **Enable Latency** checkbox.
   - In the **Latency (ms)** field, enter `2000`.
6. In the **Status Override** section:
   - Check the **Enable Status Override** checkbox.
   - In the **Status Code** dropdown, select `500`.
   - In the **Failure %** field, enter `25`.
7. Click **OK** to save both features simultaneously.
8. Verify that the **Chaos Rules** table displays:
   ```text
   Latency: 2000ms | Status: 500 (25%) | Reset: OFF
   ```
9. Open your web browser or API testing tool, and make 8 to 10 consecutive requests to:
   ```text
   http://localhost:8080/github/users/octocat
   ```

---

## Expected Result

- **How Rules Stack**:
  Chaos features in EntropyLab are **not mutually exclusive—they stack**. Every single request passes through the latency stage first, and then evaluates the failure probability stage.
- **In Your Client / Browser**:
  - **Every single request** takes approximately 2 full seconds to complete. You will always observe your application's loading spinner for 2 seconds.
  - **Roughly 75% of requests**: Complete after 2 seconds with `200 OK` and render the GitHub user profile.
  - **Roughly 25% of requests**: Complete after 2 seconds with `500 Internal Server Error (Chaos Override)`.
- **In EntropyLab's Inspector**:
  - Open the **Inspector** tab to view your test requests.
  - **Duration (ms)**: Every single row shows a duration of approximately `2100 ms` to `2300 ms`.
  - **Type & Status**: You will observe a realistic mixture:
    - Successful requests: Status **`200`**, Type **`FORWARDED`**, Duration ~`2200 ms`.
    - Failed requests: Status **`500`**, Type **`CHAOS_STATUS`**, Duration ~`2005 ms` (2,000 ms delay + instantaneous local 500 return).

---

## Variations

- **Want to add occasional hard socket crashes too? (Tri-Feature Chaos)**
  Open **Edit Chaos Rule** and check **Enable Connection Reset** with **Reset %** set to `10`. 
  - Every request takes 2 seconds.
  - Roughly 10% of requests have their TCP socket abruptly severed after 2 seconds.
  - As explained in [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md), **Connection Reset takes priority over Status Code Override**. For the ~10% of requests where reset triggers, the socket is dropped immediately, and the 500 status override is skipped.
- **Want to scope compound degradation to a single endpoint?**
  In the **Edit Chaos Rule** dialog, populate the **Sub-path filter (optional)** field with `/checkout`. General API requests will remain fast and healthy, while checkout calls will experience the 2-second delay and 25% failure rate.
- **Simulate a deadlocked upstream service:**
  Set **Latency (ms)** to `5000`, **Status Code** to `504` (Gateway Timeout), and **Failure %** to `100`. Every request will hang for 5 seconds before returning a gateway timeout, allowing you to test client fallback mechanisms under severe upstream backlog.

---

## Related Reading

- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [How-To: Simulate a Slow API](./01-simulate-a-slow-api.md)
- [How-To: Simulate a Flaky API](./02-simulate-a-flaky-api.md)
- [How-To: Scope Chaos to One Endpoint](./07-scope-chaos-to-one-endpoint.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
