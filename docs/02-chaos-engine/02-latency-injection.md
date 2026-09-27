# Latency Injection: Simulating Slow Networks
*For: all developers testing timeouts, slow connections, and UI loading states.*

## Overview

**Latency injection** is the practice of artificially delaying an HTTP response before returning it to the caller. Rather than letting requests complete as fast as possible, EntropyLab pauses execution for a configured duration in milliseconds, accurately mimicking congested mobile towers, high-latency satellite connections, or overloaded database servers.

> **Note:** For the conceptual background of how chaos engineering helps build resilient systems, see [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md).

## The Chaos Rules Tab & Latency Controls at a Glance

In EntropyLab, chaos parameters are managed per-route within the **Chaos Rules** tab.

![Screenshot: Edit Chaos Rule dialog for route /github, focusing on the Latency section at the top with Enable Latency checked and the Latency (ms) spinner populated with 2500, with remaining chaos sections disabled](../images/chaos-latency-dialog.png)

When configuring latency for a route:
1. Open the **Chaos Rules** tab to view your configured routes and their active chaos summaries.
2. Select a route and click **Edit Chaos Rule** to open the multi-feature chaos configuration modal.
3. The top section of the dialog contains the dedicated **Latency** controls (**Enable Latency** checkbox and the **Latency (ms)** spinner).

> **Tip:** Latency delays run entirely on dedicated background worker threads and will never freeze or lag the EntropyLab user interface. You can freely switch tabs, inspect ongoing traffic, or update other routes while a delayed request is in flight.

## Step-by-Step: Configuring Latency Injection

Follow these steps to apply an artificial delay to any existing route:

1. Click the **Chaos Rules** tab in the top navigation bar.
2. In the table, click to select the route you want to delay (for example, `/github`).
3. Click the **Edit Chaos Rule** button in the toolbar (or double-click the table row). The **Edit Chaos Rule** dialog will open.
4. Click the **Enable Latency** checkbox to activate delay injection.
5. In the **Latency (ms)** field, set your target delay in milliseconds (for example, `2500` for a 2.5-second delay). You can type the number directly or use the up/down spinner arrows (which increment by 100 ms).
6. Click **OK** to save the rule.
7. Verify that the **Chaos Rules** table updates its **Chaos Summary** column for that route to display:
   ```text
   Latency: 2500ms | Status: OFF | Reset: OFF | Mutation: OFF
   ```

The rule is now active immediately. You do not need to restart the proxy.

## Verifying Injected Latency with the Inspector

To confirm that the delay is functioning properly:

1. Open your web browser or API testing tool (such as Postman or curl).
2. Send a request to your proxied endpoint (for example, `http://localhost:8080/github/users/octocat`).
3. Observe your client: notice that the page takes approximately 2.5 seconds before rendering the response.
4. Switch back to EntropyLab and click the **Inspector** tab.
5. Look at the most recent entry at the top of the table:
   - The **Path** column displays `/github/users/octocat`.
   - The **Duration (ms)** column displays a number slightly higher than your injected value (for example, `2680`), representing your 2,500 ms injected delay plus the real network round-trip time to GitHub.

## Field-by-Field Reference

| Field / Control | What it does | Valid values / range | Default |
|---|---|---|---|
| **Enable Latency** | Master toggle that enables or disables artificial delay injection for the selected route. | Checked (`true`) or Unchecked (`false`). | Unchecked (`false`) |
| **Latency (ms)** | The exact duration in milliseconds that EntropyLab holds the request before delivering the response. | Whole integer between `0` and `60000` (up to 60 seconds). Step size: `100 ms`. | `0` |

### Interpreting Real-World Millisecond Values

When deciding what value to set in the **Latency (ms)** field, use these real-world benchmarks:

- **100 ms – 300 ms (Fast Cellular / High-Speed Transit)**: Simulates normal 4G/LTE mobile devices or overseas data centers. Useful for testing UI responsiveness and making sure button animations do not stutter.
- **1,000 ms – 2,500 ms (Degraded 3G / Congested Wi-Fi)**: Simulates slow mobile connections or congested servers. Ideal for testing whether loading spinners, skeleton screens, and disabled submit buttons render properly.
- **5,000 ms – 15,000 ms (Near-Timeout Conditions)**: Simulates near-failure states, satellite connections, or severe server backlog. Crucial for verifying that your client application triggers its own timeout handlers instead of hanging indefinitely.
- **30,000 ms – 60,000 ms (Maximum Delay)**: The maximum allowable delay (60 seconds). Simulates catastrophic network stalls or deadlocked backend microservices.

## Common Mistakes & Troubleshooting

- **Entering Values Above 60,000 ms**: The spinner caps values at `60000` (1 minute). If you type a value higher than 60,000, it automatically clamps to 60,000 when focus leaves the field.
- **Forgetting to Check "Enable Latency"**: If you configure `3000` in the spinner but leave the **Enable Latency** checkbox unchecked, no latency will be injected. The **Chaos Summary** column will display `Latency: OFF`.
- **Applying Latency to the Wrong Route**: Because each route has its own independent chaos rule, ensure you selected the specific route your client is calling.
- **Combining Latency with Sub-Path Filters**: If a sub-path filter is configured on the rule, only requests matching that specific sub-path will experience the delay. Requests to other paths under the same route will complete at normal speed.

## Related Reading

- [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md)
- [Status Code Override Reference](./03-status-code-override.md)
- [Connection Reset Reference](./04-connection-reset.md)
- [Sub-Path Filtering & Combining Rules](./05-sub-path-filtering-and-combining-rules.md)
- [How-To Recipe: Simulate a Slow API](../06-how-to-recipes/01-simulate-a-slow-api.md)
