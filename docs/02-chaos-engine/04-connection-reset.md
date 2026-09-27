# Connection Reset: Simulating Hard Crashes & Dropped Sockets
*For: all developers testing extreme resilience, low-level socket handling, and network recovery.*

## Overview

A **connection reset** forcefully severs the underlying TCP network socket while an incoming request is being processed, sending zero HTTP response headers, zero status codes, and zero body bytes back to the caller.

### Connection Reset vs. Status Code Override: Why It Is Fundamentally Worse

It is easy to confuse a Connection Reset with a Status Code Override, but they simulate fundamentally different classes of system failure:

| Failure Characteristic | Status Code Override (HTTP 500/503/504) | Connection Reset (`CHAOS_RESET`) |
|---|---|---|
| **What the Server Does** | Gracefully handles the request and sends a formatted HTTP error response. | Abruptly closes the raw TCP connection with no response at all. |
| **HTTP Protocol State** | Valid, well-formed HTTP exchange. | Broken TCP socket; violated HTTP protocol exchange. |
| **How Client Libraries React** | Handled smoothly by standard HTTP promises (e.g., `response.status === 500`). | Triggers low-level socket exceptions (e.g., `ECONNRESET`, `SocketException`, or unhandled promise rejections). |
| **Severity** | **Bad** (graceful application error). | **Catastrophic** (hard crash, kernel-level socket severance). |

In simple terms: **A Status Code Override tests how your application handles bad news; a Connection Reset tests how your application handles silence.** Many applications that cleanly handle 500 errors will crash or freeze when faced with an abrupt connection drop.

> **Note:** For a conceptual overview of where connection drops fit into chaos engineering, see [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md).

## The Connection Reset Controls at a Glance

Connection Reset is configured within the **Edit Chaos Rule** dialog in the **Chaos Rules** tab.

![Screenshot: Edit Chaos Rule dialog for route /github, highlighting the Connection Reset section with Enable Connection Reset checked and Reset % spinner set to 50, with remaining chaos sections disabled](../images/chaos-connection-reset-dialog.png)

The section contains two primary controls:
1. **Enable Connection Reset**: Checkbox that activates abrupt TCP socket drops.
2. **Reset %**: Percentage spinner (`0`–`100%`) defining the independent probability that any individual incoming request has its connection severed.

## Step-by-Step: Configuring Connection Reset

Follow these steps to simulate connection resets on a route:

1. Click the **Chaos Rules** tab in the top navigation bar.
2. Select the route you want to test (for example, `/github`).
3. Click the **Edit Chaos Rule** button in the toolbar (or double-click the row).
4. In the dialog, check the **Enable Connection Reset** checkbox.
5. In the **Reset %** field, set your target probability between `0` and `100` (e.g., `100` to guarantee a reset on every request, or `30` to simulate intermittent dropped connections).
6. Click **OK** to save the rule.
7. Verify that the **Chaos Summary** column for that route updates in the **Chaos Rules** table:
   ```text
   Latency: OFF | Status: OFF | Reset: 50% | Mutation: OFF
   ```

The rule takes effect immediately with no server restart required.

> **For Experts:** In EntropyLab's execution order, Connection Reset takes precedence over Status Code Override. If both features are enabled on the same route and both trigger on a request, the connection is immediately terminated without sending status headers.

## Field-by-Field Reference

| Field / Control | What it does | Valid values / range | Default |
|---|---|---|---|
| **Enable Connection Reset** | Master toggle that enables or disables abrupt TCP connection severance for this route. | Checked (`true`) or Unchecked (`false`). | Unchecked (`false`) |
| **Reset %** | The probability (percentage chance) that any individual incoming request will have its socket closed with no response. Operates as an independent roll per request. | Integer between `0` and `100`. Step size: `5%`. | `0` |

## What You Will Observe & Troubleshooting

Because a connection reset drops the TCP socket abruptly, your client application will not receive a standard HTTP status page. Setting clear expectations for what this looks like prevents confusion:

> **Warning:** When a connection reset triggers, your client will report a low-level network failure, not an HTTP error code. Do not worry—EntropyLab is not crashing; this is the intended chaos behavior! Depending on what tool you use to send the request, you will observe:
> - **Web Browsers (Chrome / Edge / Firefox)**: Displays an error screen reading `"This site can't be reached"`, `ERR_CONNECTION_RESET`, or `ERR_EMPTY_RESPONSE`.
> - **Node.js / Fetch / Axios**: Throws `FetchError: socket hang up`, `ECONNRESET`, or `Client network socket disconnected before secure TLS connection was established`.
> - **cURL / CLI Tools**: Prints `curl: (52) Empty reply from server` or `curl: (56) Recv failure: Connection was reset by peer`.
> - **Python (requests / urllib)**: Raises `requests.exceptions.ConnectionError: ('Connection aborted.', ConnectionResetError(10054, ...))`.

### Recognizing Connection Resets in the Inspector

When a request is terminated by a connection reset, EntropyLab captures the event in the **Inspector** tab:

1. **Status Column**: Displays **`N/A (Reset)`** in place of a numeric status code, explicitly confirming that no HTTP status code was ever transmitted.
2. **Type Column**: Displays **`CHAOS_RESET`** in the log table.
3. **Response Body**: Empty (since no payload was generated).
4. **Duration (ms)**: Near zero (typically `< 2 ms`), unless combined with Latency Injection.
5. **Save as Mock**: The **Save as Mock** button is automatically disabled for reset entries, as there is no response payload to save.

## Related Reading

- [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md)
- [Latency Injection Reference](./02-latency-injection.md)
- [Status Code Override Reference](./03-status-code-override.md)
- [Sub-Path Filtering & Combining Rules](./05-sub-path-filtering-and-combining-rules.md)
- [How-To Recipe: Simulate a Hard Crash](../06-how-to-recipes/03-simulate-a-hard-crash.md)
