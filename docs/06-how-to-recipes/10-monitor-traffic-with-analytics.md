# How-To: Monitor Traffic with Analytics
*For: intermediate and advanced developers (Marcus & Priya) auditing session health, evaluating chaos impact, and tracking response percentiles.*

## Goal

Get a fast, aggregated summary of how your whole testing session performed, without manually scanning through or counting hundreds of individual rows in the Inspector.

## Prerequisites

Before following this recipe, ensure you understand:
- How to start the proxy and route traffic (see [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md)).
- How to configure chaos rules (see [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md) and [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)).
- The core metrics and percentile calculations used by the dashboard (see [Analytics Dashboard Reference](../03-inspector/03-analytics-dashboard.md)).

---

## Steps

1. In EntropyLab, verify that your proxy is active on the **Proxy Control** tab (`Running on port 8080`).
2. In the **Routes** tab, ensure you have an active route (e.g., **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. In the **Chaos Rules** tab, configure a probabilistic chaos rule to inject a controlled failure rate:
   - Select `/github` and click **Edit Chaos Rule**.
   - Check **Enable Status Override**.
   - Select status code **`503`**.
   - Set **Failure %** to **`30%`**.
   - Click **OK** to save.
4. Execute a batch of 20 requests against your proxied endpoint using a shell loop, your application, or curl:
   ```bash
   for i in {1..20}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/github/users/octocat; done
   ```
5. Click the **Analytics** tab in the top navigation bar.
6. Observe the dashboard as it queries the local SQLite database and computes aggregate metrics across your session.
7. Inspect the top row of **KPI Summary Cards**:
   - Check **TOTAL REQUESTS** to verify the total volume of requests processed.
   - Check **ERROR RATE** to see the overall failure percentage highlighted in red (`#FE0134`).
   - Compare **P50 LATENCY** (median response time) with **P95 LATENCY** (tail latency).
8. Scroll to the **Response Status & Traffic Distribution** bar chart to see the breakdown of successful calls versus chaos-induced failures.

---

## Expected Result

- **In the KPI Cards**:
  - **TOTAL REQUESTS**: Displays `20` (or your total session count).
  - **ERROR RATE**: Shows approximately `30.0%` (for a 20-request sample with a 30% rule, typically 5 to 7 requests fail, resulting in an error rate between `25.0%` and `35.0%`).
  - **AVG DURATION**: Displays the average round-trip time across all 20 calls.
  - **P50 LATENCY**: Displays the median duration (e.g. `240 ms`).
  - **P95 LATENCY**: Displays the 95th percentile duration, highlighting the slowest requests in the batch.
- **In the Traffic Distribution Bar Chart**:
  - The **`2xx Success`** bar shows approximately 14 requests.
  - The **`CHAOS_STATUS`** bar shows approximately 6 requests.
  - All other bars (`4xx Client Error`, `5xx Server Error`, `CHAOS_RESET`, `MOCKED`) remain at `0` unless those conditions were triggered.
- **Validation Takeaway**: You have verified in under 5 seconds that your 30% chaos rule is firing within expected statistical bounds without inspecting a single individual log entry.

---

## Variations

- **Before-and-After Chaos Comparison**:
  Before enabling any chaos rules, open the **Analytics** tab to establish your baseline health (0.0% error rate, uniform latency). Run your clean regression test suite. Next, enable your chaos rule, execute the same suite again, and review the Analytics dashboard to definitively prove that induced errors and latency spikes were captured and isolated.
- **Spotting Chaos Leakage via p95**:
  Suppose you configured a Sub-path Filter intending to inject `3000 ms` latency ONLY into `/checkout`. After executing a mixed test run across all your app's endpoints, check the **P95 LATENCY** card:
  - If `p95` is high only when checkout is called, your filter is strictly scoped.
  - If `p95` is elevated across general browsing endpoints where no latency was intended, your sub-path filter pattern may be too broad (e.g., matching `/check` instead of `/checkout`).
- **Verifying Mock Offloading Performance**:
  If you configure static mocks for heavy endpoints, review the **MOCKED** category on the chart alongside the **P50 LATENCY** card. Mocks are served from local JSON files in under 15 ms; observing a surge in the `MOCKED` bar accompanied by a drop in `p50` confirms that your application is effectively shielded from upstream network overhead.

---

## Related Reading

- [Analytics Dashboard Reference](../03-inspector/03-analytics-dashboard.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
- [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)
- [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [How-To: Simulate a Flaky API](./02-simulate-a-flaky-api.md)
- [How-To: Combine Latency and Failure](./04-combine-latency-and-failure.md)
