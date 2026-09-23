# Common Errors and Fixes
*For: all developers diagnosing configuration mismatches, proxy errors, and unexpected network behaviors.*

## Overview

When testing applications through a local reverse proxy, issues usually stem from a few common configuration mismatches: calling the wrong port, slight typos in route paths, exact-path mock restrictions, or conflicting local services.

This guide provides an exhaustive troubleshooting directory of every error message and unexpected behavior you might encounter while using EntropyLab, along with immediate, actionable steps to resolve them.

> **Tip:** Whenever an unexpected error occurs, open the **Inspector** tab first. The Inspector records the exact path requested by your client, the returned HTTP status code, and the processing type (`FORWARDED`, `MOCKED`, `CHAOS_STATUS`, or `CHAOS_RESET`).

---

## Error, Cause & Fix Reference

The table below catalogs every standard error message and unexpected condition in EntropyLab:

| Error Message / Symptom | Where It Appears | Root Cause | Immediate Actionable Fix |
|---|---|---|---|
| **`No route configured for this path` (HTTP 404)** | Browser or client response body; Inspector Status `404` | 1. No route exists with a **Local Path** matching the incoming URL prefix.<br>2. The matching route exists, but its **Enabled** checkbox is unchecked. | 1. Check the **Routes** tab to verify a route exists for that path prefix.<br>2. Ensure the route's **Enabled** checkbox is checked.<br>3. Check for leading/trailing slash mismatches in your client code. |
| **`Bad Gateway: <message>` (HTTP 502)** | Browser or client response body; Inspector Status `502` | EntropyLab could not establish a connection to the upstream server. Causes include: network disconnection, upstream server outage, or invalid/misspelled **Target Base URL** (e.g., `https://api.github.con`). | 1. Test your internet connection.<br>2. Open the **Routes** tab, edit the route, and verify the **Target Base URL** starts with `http://` or `https://` and is spelled correctly.<br>3. Test the target URL directly in your browser. |
| **`Address already in use: bind`** | Error dialog titled **Proxy Error** on clicking **Start Proxy** | Another process on your machine (e.g., Tomcat, Docker, Jenkins, Node.js) or a second running instance of EntropyLab is already using the configured port. | 1. Change the **Port** field in the **Proxy Control** tab to another unreserved port (such as `8085`, `8090`, or `9000`) and click **Start Proxy**.<br>2. Alternatively, terminate the conflicting process using Windows PowerShell (`Get-NetTCPConnection -LocalPort 8080`). See [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md). |
| **`ERR_CONNECTION_REFUSED` / `Failed to connect`** | Browser or HTTP client error screen | The EntropyLab reverse proxy is not currently running. | 1. Switch to the **Proxy Control** tab.<br>2. Check the status indicator. If it reads red **Stopped**, click **Start Proxy**.<br>3. Confirm the status turns bold green: `Running on port <port>`. |
| **`Invalid port number`** | Error dialog titled **Invalid Port** on clicking **Start Proxy** | The value in the **Port** field is blank, non-numeric, or outside the valid TCP range (`1`–`65535`). | Enter a valid numeric integer between `1` and `65535` (e.g., `8080`) and click **Start Proxy** again. |
| **`Internal Server Error: Failed to read mock file` (HTTP 500)** | Browser or client response body; Inspector Status `500` | The `.json` mock file mapped to this path was moved, renamed, deleted from disk, or lacks read permissions. | 1. Open the **Mocks** tab and inspect the **File Path** column.<br>2. Verify that the file exists at that exact path on disk.<br>3. Re-select the file using the **Add Mock** dialog or restore the missing file. |
| **`Duplicate Route: A route for this path already exists`** | Warning dialog in **Routes** tab | An existing route already uses this exact **Local Path**. | Routes must have unique prefixes. Modify the existing route using **Edit Route**, or choose a distinct local path prefix. |
| **`Duplicate Mock: A mock for this path already exists`** | Warning dialog in **Mocks** tab | An existing mock already intercepts this exact local path. | Mocks must have unique paths. Uncheck or delete the existing mock before registering a new one for that path. |
| **`Validation Error: Both fields are required`** | Warning dialog in **Routes** tab | The **Local Path** or **Target Base URL** field was left blank or contains only whitespace. | Enter non-empty values for both fields before clicking **Add** or **Save**. |

---

## In-Depth Troubleshooting Scenarios

### 1. A Mock Is Not Being Served Even Though It Is Enabled

**Symptom**: You created a mock for `/api/users`, but when you call `http://localhost:8080/api/users/123`, the request either returns a 404 error or forwards to the real backend instead of serving the mock.

**Cause**: Unlike Routes (which use longest-prefix matching), **Mocks use exact path matching only**. The path requested by your client must match the mock's **Local Path** character-for-character.
- A mock for `/api/users` will **not** match `/api/users/` (trailing slash).
- A mock for `/api/users` will **not** match `/api/users/123` (nested sub-path).

**Fix**:
1. Check the exact URL requested in the **Inspector** tab's **Path** column.
2. In the **Mocks** tab, ensure the mock's **Local Path** exactly matches that string.
3. If you need mocks for multiple sub-paths (e.g., `/api/users` and `/api/users/123`), create a dedicated mock for each exact path.

---

### 2. A Chaos Rule Does Not Seem to Apply

**Symptom**: You enabled Latency Injection (3000 ms) or Status Code Override (500 at 100%), but requests complete in single-digit milliseconds and return 200 OK.

**Checklist to Resolve**:
1. **Check the Sub-Path Filter**: Open **Edit Chaos Rule** for that route. Is the **Sub-path filter (optional)** populated? If the filter is set to `/checkout`, calling `/status` or `/users` will bypass chaos completely. Clear the field if you want chaos to apply to the entire route.
2. **Check the Route Enabled Status**: In the **Routes** tab, verify that the route's **Enabled** checkbox is checked.
3. **Check for Active Mocks**: Mocks are evaluated *before* the chaos engine. If an enabled mock exists for that exact path, EntropyLab serves the mock immediately; the route and its chaos rules are never touched. Temporarily disable the mock in the **Mocks** tab if you want to test chaos.
4. **Check the Inspector Type**: If the Inspector shows Type **`FORWARDED`**, the request bypassed chaos. If it shows **`CHAOS_STATUS`** or **`CHAOS_RESET`**, chaos did execute.

---

### 3. Does the App Freeze During Slow Requests?

**Important Guarantee**: **EntropyLab's user interface should NEVER freeze or become unresponsive during a slow request.**

- All proxy networking, latency pauses, socket resets, and database logging execute on dedicated background worker threads.
- When an injected delay of 30,000 ms is in flight, the JavaFX desktop UI remains completely fluid. You can switch tabs, add new routes, view historical inspector logs, or toggle Dark Mode without lag.

> **Warning:** If the EntropyLab window freezes, displays a "Not Responding" title bar, or locks up when processing a delayed request, **this is unexpected behavior and should be treated as a bug**. Please report it with your Windows OS version and steps to reproduce.

---

## Related Reading

- [Proxy Control: Resolving Port Conflicts](../01-proxy-and-routes/02-proxy-control.md)
- [Understanding and Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
- [Limitations & FAQ](./02-limitations-and-faq.md)
