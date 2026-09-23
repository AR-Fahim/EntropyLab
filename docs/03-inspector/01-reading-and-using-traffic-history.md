# Reading & Using Traffic History
*For: all developers monitoring live traffic, inspecting request metrics, and debugging proxy behavior.*

## Overview

The **Inspector** tab is EntropyLab's built-in network traffic analyzer. It records a live, searchable, and permanent historical log of every single HTTP request and response that passes through the reverse proxy. 

Whether an exchange was forwarded across the internet, served instantly by a local mock, delayed by chaos latency, or severed by a connection drop, the Inspector gives you immediate, transparent visibility into the lifecycle of your traffic.

> **Tip:** The Inspector is the premier debugging tool for verifying chaos configurations. If you are ever wondering *"Why isn't my chaos rule firing?"* or *"Did my app actually send that header?"*, open the Inspector first and check the **Type** and **Status** columns.

## The Inspector Tab at a Glance

The Inspector interface provides an immediate high-level summary of network activity:

![Screenshot: Inspector tab showing a traffic history table with diverse request types: a 200 GET to /github/users (FORWARDED), a 200 GET to /api/users (MOCKED), a 500 POST to /stripe/checkout (CHAOS_STATUS), and a reset to /orders (CHAOS_RESET)](./images/inspector-traffic-table.png)

The view consists of:
1. **Action Toolbar**: Houses the **Save as Mock** button (active when a valid row with a response payload is selected).
2. **Live Traffic Table**: A six-column table streaming network events in real time.
3. **Detail Window**: Double-clicking any row (or pressing Enter) opens an in-depth inspection modal showing complete request and response headers and bodies.

## Step-by-Step: Reading and Interpreting Traffic

1. Open EntropyLab and click the **Inspector** tab in the top navigation bar.
2. Trigger an API call from your frontend application, test script, or browser (for example, navigating to `http://localhost:8080/github/users/octocat`).
3. Observe the top of the table: the newly completed request appears at the very top row automatically.
4. Scan the columns from left to right:
   - Check **Time** to verify when the request arrived.
   - Check **Method** and **Path** to ensure your application targeted the expected local endpoint.
   - Check **Status** to see what HTTP status code your app received.
   - Check **Duration (ms)** to evaluate latency or identify performance bottlenecks.
   - Check **Type** to see whether the request was forwarded to the real internet, intercepted by a mock, or disrupted by chaos rules.

## Field-by-Field Reference

### Column Reference

| Column | What it displays | Example Values |
|---|---|---|
| **Time** | The exact wall-clock time the request reached EntropyLab, formatted as `HH:mm:ss` in your local time zone. | `14:23:05`, `09:15:42` |
| **Method** | The HTTP request verb transmitted by the client. | `GET`, `POST`, `PUT`, `DELETE`, `PATCH` |
| **Path** | The complete local path and endpoint requested on `localhost:<port>`. | `/github/users/octocat`, `/stripe/checkout` |
| **Status** | The final HTTP status code returned to your client application. If a connection reset occurred, displays **`N/A (Reset)`**. | `200`, `201`, `404`, `500`, `503`, `N/A (Reset)` |
| **Duration (ms)** | Total round-trip time in milliseconds from socket reception to response completion. | `12 ms` (local mock), `2540 ms` (chaos latency) |
| **Type** | The classification category indicating how EntropyLab processed the request. | `FORWARDED`, `MOCKED`, `CHAOS_STATUS`, `CHAOS_RESET` |

### Request Types at a Glance

The **Type** column is your fastest way to understand what happened behind the scenes:

| Type Value | What Happened Behind the Scenes | Target Server Contacted? |
|---|---|---|
| **`FORWARDED`** | The request matched an enabled route and was relayed across the internet to the real upstream API. The response shown came directly from the external server. | **Yes** (real external server responded). |
| **`MOCKED`** | The request matched an active mock in the **Mocks** tab. EntropyLab served a saved JSON response file immediately from your local disk. | **No** (served locally in < 15 ms). |
| **`CHAOS_STATUS`** | The request was intercepted by a **Status Code Override** chaos rule. EntropyLab returned an HTTP error code (`500`, `503`, or `504`) locally. | **No** (intercepted before reaching internet). |
| **`CHAOS_RESET`** | The request was severed by a **Connection Reset** chaos rule. EntropyLab forcefully dropped the TCP socket with no headers or body. | **No** (connection severed immediately). |

## Live Updates & The 500-Row Display Limit

### Seamless Real-Time Streaming

The Inspector updates in real time using an internal event bus. As your client applications make network calls:
- New rows are instantly inserted at the top of the table.
- There is **no need to refresh** the page, click a reload button, or restart the app.
- Execution times and status codes appear as soon as the response concludes.

### The 500-Row Table Cap vs. Permanent Database Storage

To keep the desktop interface fluid, memory-efficient, and responsive even under rapid test suites, the visual table in the **Inspector** displays the **500 most recent requests**. When the 501st request arrives, the oldest visible row is removed from the bottom of the table.

> **Note:** **No data is ever lost.** The 500-row cap is strictly a UI rendering optimization. Every single request, along with its full headers, timings, and payloads, is saved permanently to an embedded SQLite database on your machine (`%APPDATA%\EntropyLab\entropylab.db`). Even if older entries scroll off the visual table, they remain safely stored in your local database across application restarts.

## Common Mistakes & Troubleshooting

- **No Requests Appearing in the Table**:
  1. Verify in the **Proxy Control** tab that the proxy status is green (`Running on port 8080`).
  2. Verify that your application is sending requests to `http://localhost:<port>/<path>`, not calling the remote API directly. Remember that EntropyLab does not perform transparent global interception.
- **Unexpected `404 Not Found` Responses**:
  If a row appears with Status `404` and Type `FORWARDED`, double-click the row to view the response body. If the body says `No route configured for this path`, your request path did not match any active route in the **Routes** tab.
- **Save as Mock Button Is Disabled**:
  The **Save as Mock** button requires a valid row selection and will remain disabled if:
  - No row is currently selected in the table.
  - The selected entry is of type `CHAOS_RESET` (which has no response payload).
  - The response body was empty.

## Related Reading

- [Inspecting Details and Auto-Mock](./02-inspecting-details-and-auto-mock.md)
- [Understanding and Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
- [What Is Chaos Engineering?](../02-chaos-engine/01-what-is-chaos-engineering.md)
- [Settings & Data Storage: The SQLite Database](../05-settings-and-data/01-theme-and-data-storage.md)
