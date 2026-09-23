# How-To: Scope Chaos to One Endpoint
*For: developers and QA engineers targeting specific operations without disrupting broad API suites.*

## Goal

Apply chaos injection (such as latency delays or status code overrides) to only ONE specific endpoint under a broad route, keeping all other sister endpoints running normally at full speed.

## Prerequisites

Before following this recipe, ensure you understand:
- How to define and manage routes (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- How sub-path filters constrain chaos rules (see [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy is green: `Running on port 8080`.
2. Click the **Routes** tab and ensure you have an active route representing a multi-endpoint service (for example, **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Click the **Chaos Rules** tab in the top navigation bar.
4. Select the row for your route (e.g., `/github`) and click **Edit Chaos Rule** (or double-click the row).
5. In the dialog, check **Enable Latency** and enter:
   ```text
   3000
   ```
6. Locate the **Sub-path filter (optional)** field near the bottom of the dialog and enter:
   ```text
   /users
   ```
7. Click **OK** to save the rule.
8. Open your web browser or API client and send a request to the **scoped endpoint**:
   ```text
   http://localhost:8080/github/users/octocat
   ```
9. Now send a second request to an **unscoped endpoint** under that exact same route:
   ```text
   http://localhost:8080/github/zen
   ```

---

## Expected Result

By testing two distinct sub-paths under the same parent route, you can verify that the filter is working with pinpoint accuracy:

| Test Call | Endpoint Path | Matches Filter? | Observable Result | Inspector Metrics |
|---|---|---|---|---|
| **Test Call 1** | `http://localhost:8080/github/users/octocat` | **Yes** (`/users`) | **Delayed by 3.0s**. Loading spinner persists for 3 seconds before rendering. | Duration: ~`3150 ms`, Status: `200`, Type: `FORWARDED` |
| **Test Call 2** | `http://localhost:8080/github/zen` | **No** | **Instant delivery**. Renders immediately with zero delay. | Duration: ~`120 ms`, Status: `200`, Type: `FORWARDED` |

EntropyLab successfully isolated the failure mode to `/users` while leaving the rest of the GitHub API fully operational.

---

## Variations

- **Target Risky Mutations Without Breaking App Navigation:**
  In a full-stack e-commerce application mapped to `/stripe`, set the **Sub-path filter** to `/checkout` and enable a 500 error override. You can now freely click through product catalogs, user accounts, and shopping carts at normal speed, triggering chaos only when clicking the final **Submit Payment** button.
- **Deep Hierarchical Sub-Paths:**
  Sub-path filters can be as specific as needed. Entering `/users/octocat/repos` targets only repository listing calls for `octocat`, while calls to `/users/octocat` or other user profiles bypass chaos entirely.
- **Combining Scoped Delays with Scoped Outages:**
  In the same **Edit Chaos Rule** dialog, check both **Enable Latency** (`2500 ms`) and **Enable Status Override** (`503` at `40%`), with the **Sub-path filter** set to `/checkout`. Now, only checkout requests suffer the 2.5-second delay and 40% failure probability, while navigation and user profile endpoints remain completely unaffected.

---

## Related Reading

- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [How-To: Simulate a Slow API](./01-simulate-a-slow-api.md)
- [How-To: Combine Latency and Failure](./04-combine-latency-and-failure.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
