# Analytics Dashboard: Aggregated Metrics & Fault Distribution
*For: all developers and QA engineers monitoring test session health, analyzing response percentiles, and auditing chaos error distributions.*

## Overview

The **Analytics** tab provides an instant, high-level health check of your testing sessions without requiring you to manually scroll through or count hundreds of individual rows in the **Inspector**.

While the Inspector tab acts as a detailed transaction log (showing granular, request-by-request details), the Analytics tab aggregates your traffic into actionable key performance indicators (KPIs) and visual distributions. At a single glance, you can evaluate overall system stability, verify how frequently chaos rules are firing, and uncover hidden latency outliers that simple averages hide.

> [!NOTE]
> **Full Database Calculation:**
> Unlike the Inspector tab—which caps its live table display to the most recent 500 rows to ensure UI responsiveness—the Analytics dashboard calculates all statistics from the **entire traffic history** stored in your local SQLite database (`%APPDATA%\EntropyLab\entropylab.db`). Whether your session contains 50 requests or 50,000, the analytics represent your complete recorded history.

## The Analytics Tab at a Glance

The Analytics interface presents two primary functional areas: a top row of KPI metric summary cards and a comprehensive traffic distribution chart.

![Screenshot: Analytics tab displaying KPI summary cards (Total Requests, Error Rate, Avg Duration, p50 Latency, p95 Latency) and the Response Status & Traffic Distribution bar chart](./images/analytics-dashboard.png)

1. **Header & Action Bar**: Contains the page title, a live subtitle, a manual **Refresh** button, and a "Last updated: HH:mm:ss" timestamp.
2. **KPI Summary Cards**: Five real-time cards highlighting **TOTAL REQUESTS**, **ERROR RATE**, **AVG DURATION**, **P50 LATENCY**, and **P95 LATENCY**.
3. **Response Status & Traffic Distribution Bar Chart**: A vertical bar chart categorizing all historical traffic across 6 distinct status and chaos buckets.

## Step-by-Step: Analyzing Traffic & Fault Distributions

Follow these steps to inspect and analyze proxy traffic:

1. Click the **Analytics** tab in the top navigation bar.
2. The view loads automatically, querying SQLite asynchronously on background threads without lagging the UI.
3. Check the **TOTAL REQUESTS** card to verify the total volume of requests processed during your test session.
4. Review the **ERROR RATE** card:
   - If no errors have occurred, the metric renders in standard neutral styling (e.g. `0.0%`).
   - If errors or chaos failures are present, the percentage renders in high-visibility brand red (`#FE0134`) to immediately draw your attention.
5. Compare the **P50 LATENCY** and **P95 LATENCY** cards to check for performance consistency across your endpoints.
6. Scroll down to the **Response Status & Traffic Distribution** bar chart to see exactly where your failures originated (e.g., distinguishing real upstream `5xx Server Error` codes from simulated `CHAOS_STATUS` overrides or `CHAOS_RESET` drops).
7. If new traffic has arrived, click the **Refresh** button in the top-right corner to manually recompute stats, or simply observe the view as it auto-refreshes whenever new traffic completes.

## Field-by-Field Reference: KPI Summary Cards

| Metric Card | What it measures | Calculation details | Default / Empty State |
|---|---|---|---|
| **TOTAL REQUESTS** | The total number of HTTP requests recorded by EntropyLab across the entire local database. | Total row count (`SELECT COUNT(*) FROM request_log`). | `0` |
| **ERROR RATE** | The percentage of all requests that resulted in an HTTP error or a simulated chaos disruption. | Formula: `(Error Count * 100.0) / Total Requests`. Highlighted in red (`#FE0134`) when greater than `0.0%`. | `0.0%` |
| **AVG DURATION** | The mathematical mean round-trip duration in milliseconds across all requests. | Sum of all `durationMs` divided by total requests. | `0.0 ms` |
| **P50 LATENCY** | The median response time (50th percentile). Half of all requests were faster than this value. | Middle value of all sorted request durations. | `0 ms` |
| **P95 LATENCY** | The 95th percentile response time. 95% of all requests were faster than this value. | 95th percentile value of sorted request durations. | `0 ms` |

> [!IMPORTANT]
> **What Counts as an "Error"?**
> The **ERROR RATE** metric treats both **real upstream HTTP errors** (`statusCode >= 400`) and **simulated chaos events** (`CHAOS_STATUS` and `CHAOS_RESET`) as errors. When running chaos experiments, an elevated error rate is expected and desirable—it confirms that your chaos rules are actively intercepting calls.

## Response Status & Traffic Distribution Chart

Beneath the KPI cards, the **Response Status & Traffic Distribution** bar chart categorizes every recorded request into one of six mutually exclusive buckets along the X-axis:

| Category Bucket | What it represents |
|---|---|
| **`2xx Success`** | Real upstream HTTP success responses with status codes `200` through `299` (including `200 OK`, `201 Created`, `204 No Content`). |
| **`4xx Client Error`** | Real upstream client errors with status codes `400` through `499` (such as `400 Bad Request`, `401 Unauthorized`, `404 Not Found`). |
| **`5xx Server Error`** | Real upstream server errors with status codes `500` through `599` returned directly by the target API (excluding simulated chaos). |
| **`CHAOS_STATUS`** | Requests intercepted by an active **Status Code Override** chaos rule where EntropyLab locally generated an error (`500`, `503`, or `504`). |
| **`CHAOS_RESET`** | Requests terminated by an active **Connection Reset** chaos rule where the TCP socket was forcefully dropped. |
| **`MOCKED`** | Requests intercepted by the mocking engine and served from local JSON files. |

This breakdown allows you to immediately answer questions such as: *"Did my application fail because GitHub was genuinely down (5xx), or because EntropyLab injected a simulated failure (CHAOS_STATUS)?"*

## Understanding Percentiles: p50 vs. p95 Explained

When measuring web and API performance, looking only at the **Average Duration** can be dangerously misleading. A single extreme outlier (or a small percentage of stalled requests) gets diluted by thousands of fast requests, hiding critical performance issues. Percentiles reveal the true distribution of user experience.

### Plain-Language Concept

Imagine you took every single API request your application made, wrote its duration on an index card, and lined the cards up on a table in order from fastest to slowest:

- **p50 (Median — The Typical Case)**: Walk halfway down the line and pick up the card right in the middle. Half of all your requests were faster than this card, and half were slower. This represents your **typical, day-to-day user experience**.
- **p95 (95th Percentile — The Worst-Case Experience)**: Walk almost all the way to the end of the line (95% of the way). Only the slowest 5% of your requests were worse than this card. This represents the **frustrated user experience**—the people stuck on slow cellular connections, hitting cold-start serverless functions, or suffering backend database locks.

### A Worked Example: Spotting the Unstable Dependency

Consider two testing sessions, both showing an **Average Duration of ~300 ms**:

- **Service A**:
  - `p50`: `280 ms`
  - `p95`: `320 ms`
  - *Analysis*: Rock-solid consistency. Almost every request completes within a tight 40 ms window. Your backend is predictable and stable.

- **Service B**:
  - `p50`: `120 ms`
  - `p95`: `4000 ms`
  - *Analysis*: **Dangerous instability.** The typical user has a blistering fast experience (120 ms). But 1 out of every 20 requests (5%) takes 4 full seconds! If you only looked at the 300 ms average, you would never suspect that 5% of your users are experiencing severe lag.

In chaos testing, this metric is indispensable: when you inject **Latency (e.g. 3,000 ms)** with a **Failure % of 10%**, your `p50` will reflect normal response speeds while your `p95` immediately leaps up to reflect the injected delay, demonstrating that your application is experiencing tail-latency spikes.

> [!NOTE]
> **🎯 For Experts: The Mathematical Percentile Algorithm**
> EntropyLab computes percentiles in memory using the nearest-rank index method:
> 1. All historical duration values `durationMs` from `RequestLogDAO.getAll()` are collected into a flat list `List<Long> durations`.
> 2. The list is sorted in ascending natural order (`durations.sort(Long::compare)`).
> 3. The p50 index is computed as `p50Idx = (int) (durations.size() * 0.50)`, clamped to `durations.size() - 1`.
> 4. The p95 index is computed as `p95Idx = (int) (durations.size() * 0.95)`, clamped to `durations.size() - 1`.
> 5. The values at those exact sorted indices are assigned to `stats.p50Duration` and `stats.p95Duration`.

## Common Mistakes & Troubleshooting

- **Assuming Analytics Only Shows Recent Traffic**: It is easy to think Analytics is limited because the Inspector only lists 500 rows. Remember: Analytics queries the entire SQLite database file. If you have run tests across several days without clearing history, all historical requests contribute to the cards and charts.
- **Misinterpreting High Error Rates During Chaos Testing**: If you run a test session specifically to test resilience against HTTP 503 errors or connection drops, your **ERROR RATE** card may climb to 40% or 50%. This is expected; the card accurately reflects that half of your session experienced injected chaos.
- **Seeing "0 ms" on Empty Installations**: When EntropyLab is launched for the very first time before any traffic has passed through the proxy, all cards display initial zero states (`0`, `0.0%`, `0.0 ms`, `0 ms`). As soon as your first request completes, the dashboard updates automatically.
- **UI Freezes When Processing Huge Logs**: All database retrieval and statistical computations run inside `CompletableFuture.supplyAsync()` worker threads. The JavaFX application thread only applies the final calculated numbers, ensuring the dashboard never freezes during intensive calculations.

## Related Reading

- [Reading & Using Traffic History](./01-reading-and-using-traffic-history.md)
- [Inspecting Details & Auto-Mock](./02-inspecting-details-and-auto-mock.md)
- [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md)
- [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)
- [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md)
- [How-To Recipe: Monitor Traffic with Analytics](../06-how-to-recipes/10-monitor-traffic-with-analytics.md)
