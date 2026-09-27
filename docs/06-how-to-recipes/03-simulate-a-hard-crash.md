# How-To: Simulate a Hard Crash
*For: developers testing low-level socket exception handling, process crash resistance, and catastrophic network recovery.*

## Goal

Configure EntropyLab to abruptly sever the underlying TCP network socket on 100% of requests, testing whether your client application survives a catastrophic server outage without crashing the host process.

## Prerequisites

Before following this recipe, ensure you understand:
- How to create and manage routes (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- How connection resets operate differently from HTTP status errors (see [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy status is green: `Running on port 8080`.
2. Click the **Routes** tab and ensure you have an active route (e.g., **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Click the **Chaos Rules** tab in the top navigation bar.
4. Select the row for your route (e.g., `/github`) and click **Edit Chaos Rule** (or double-click the row).
5. In the dialog, check the **Enable Connection Reset** checkbox.
6. In the **Reset %** field, enter:
   ```text
   100
   ```
7. Click **OK** to save the rule.
8. Verify that the **Chaos Rules** table displays:
   ```text
   Latency: OFF | Status: OFF | Reset: 100% | Mutation: OFF
   ```
9. Open your web browser, terminal, or API testing tool, and make a request to:
    ```text
    http://localhost:8080/github/users/octocat
    ```

---

## Expected Result

- **In Your Client / Browser**:
  - The request fails immediately with a raw socket termination error—**not an HTTP error page**.
  - **Web Browsers**: Display an error screen reading `This site can't be reached` or `ERR_CONNECTION_RESET`.
  - **Node.js / Fetch**: Throws `FetchError: socket hang up` or `ECONNRESET`.
  - **cURL**: Prints `curl: (52) Empty reply from server`.
- **In EntropyLab's Inspector**:
  - Switch to the **Inspector** tab. The newest entry displays:
    - **Method**: `GET`
    - **Path**: `/github/users/octocat`
    - **Status**: **`N/A (Reset)`** (confirming no HTTP status code was ever sent).
    - **Duration (ms)**: Under `5 ms`.
    - **Type**: **`CHAOS_RESET`**.
    - **Response Body**: Completely empty.

---

## Variations

- **Want to simulate occasional, intermittent crashes?**
  Open **Edit Chaos Rule** and lower **Reset %** from `100` to `25`. Now, roughly 1 out of 4 requests will suffer an abrupt socket severance, while the rest succeed with normal 200 OK responses. This tests whether intermittent socket drops cause lingering memory leaks or unhandled promise rejections.
- **Compare against HTTP status errors (Reset vs. 503 Flakiness):**
  Disable Connection Reset and enable **Status Code Override** (`503` at `100%`) as described in [How-To: Simulate a Flaky API](./02-simulate-a-flaky-api.md). Compare how your frontend reacts:
  - Does your app show a polite error toast for the 503, but crash with an unhandled exception screen on the Connection Reset?
  - If so, wrap your network calls in `try/catch` blocks that explicitly catch socket-level disconnect errors.
- **Want to simulate a server that hangs and then crashes?**
  In the same **Edit Chaos Rule** dialog, check **Enable Latency** and set **Latency (ms)** to `3000`. When a request arrives, EntropyLab will hold the connection open for 3 seconds before severing the socket, accurately simulating a remote server worker that timed out and crashed.

---

## Related Reading

- [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md)
- [How-To: Simulate a Flaky API](./02-simulate-a-flaky-api.md)
- [How-To: Combine Latency and Failure](./04-combine-latency-and-failure.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
