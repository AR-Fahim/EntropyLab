# How-To: Simulate a Slow API
*For: developers testing UI loading spinners, skeleton screens, and client timeout thresholds.*

## Goal

Configure EntropyLab to artificially delay a specific API route by 3,000 milliseconds, allowing you to test how your application's user interface and networking layers behave during high network latency.

## Prerequisites

Before following this recipe, ensure you understand:
- How to create and enable a proxy route (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- The fundamentals of delay injection (see [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy status is green: `Running on port 8080` (if stopped, click **Start Proxy**).
2. Click the **Routes** tab and ensure you have an active route mapping (for example, **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Click the **Chaos Rules** tab in the top navigation bar.
4. Click the row for your route (e.g., `/github`) to highlight it.
5. Click the **Edit Chaos Rule** button in the toolbar (or double-click the row).
6. In the dialog, check the **Enable Latency** checkbox.
7. In the **Latency (ms)** field, enter:
   ```text
   3000
   ```
8. Click **OK** to save the chaos rule.
9. Verify that the **Chaos Rules** table displays:
   ```text
   Latency: 3000ms | Status: OFF | Reset: OFF
   ```
10. Open your web browser or API client and send a request to:
    ```text
    http://localhost:8080/github/users/octocat
    ```

---

## Expected Result

- **In Your Client / Browser**: The request visibly pauses for 3 full seconds before completing and displaying the JSON response. If your frontend has a loading spinner, skeleton placeholder, or disabled submit button, it remains visible for the entire 3-second duration.
- **In EntropyLab's Inspector**: Switch to the **Inspector** tab. The top row shows:
  - **Method**: `GET`
  - **Path**: `/github/users/octocat`
  - **Status**: `200`
  - **Duration (ms)**: Approximately `3100 ms` to `3300 ms` (3,000 ms injected delay plus the real network round-trip time).
  - **Type**: `FORWARDED`

---

## Variations

- **Want to test hard timeout handling?**
  Increase the **Latency (ms)** field to `10000` (10 seconds) or `15000` (15 seconds). Verify whether your application's HTTP client aborts cleanly with a timeout notification, or whether it hangs indefinitely.
- **Want to slow down only one specific endpoint?**
  In the **Edit Chaos Rule** dialog, populate the **Sub-path filter (optional)** field with `/users`. Requests to `/github/users/octocat` will incur the 3-second delay, while calls to other paths like `/github/emojis` will complete instantly.
- **Want to simulate an overloaded server that occasionally crashes?**
  In addition to enabling Latency (`3000 ms`), check **Enable Status Override**, select status `503`, and set **Failure %** to `25%`. Every request will take 3 seconds, and roughly 1 out of 4 requests will fail with a `503 Service Unavailable` error.

---

## Related Reading

- [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md)
- [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [How-To: Simulate a Flaky API](./02-simulate-a-flaky-api.md)
- [How-To: Combine Latency and Failure](./04-combine-latency-and-failure.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
