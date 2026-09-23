# Status Code Override: Simulating HTTP Outages & Flakiness
*For: all developers testing error alerts, retry mechanisms, and graceful UI degradation.*

## Overview

A **status code override** intercepts incoming requests and immediately returns an HTTP failure status code (such as `500`, `503`, or `504`) directly from EntropyLab.

### Faking Failures Without Touching the Real Server

It is crucial to understand how this differs from an actual upstream API failure:
- In an actual API failure, your request travels across the internet to the remote server, and the remote server responds with an error code.
- In a **status code override**, **EntropyLab fakes the failure locally**. The request never leaves your machine, and the real external API is never contacted. 

This enables you to test how your application responds to third-party outages without having to coordinate with external providers, corrupt remote databases, or risk account rate limits.

> **Note:** For a high-level conceptual comparison of all three chaos tools, see [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md).

## The Status Code Override Controls at a Glance

Status code overrides are configured within the same **Edit Chaos Rule** dialog as Latency Injection, located in the **Chaos Rules** tab.

![Screenshot: Edit Chaos Rule dialog for route /github, highlighting the Status Code Override section with Enable Status Override checked, Status Code dropdown set to 500, and Failure % spinner set to 30](./images/chaos-status-override-dialog.png)

The section contains three interdependent controls:
1. **Enable Status Override**: Master checkbox to activate failure injection.
2. **Status Code**: Dropdown selector choosing which HTTP error code to return (`500`, `503`, or `504`).
3. **Failure %**: Percentage spinner (`0`–`100%`) controlling the probability that any given request triggers the failure.

## Step-by-Step: Configuring Status Code Override

Follow these steps to configure status code overrides on any route:

1. Click the **Chaos Rules** tab in the top navigation bar.
2. Select the route you want to disrupt (e.g., `/github`).
3. Click the **Edit Chaos Rule** button in the toolbar (or double-click the row).
4. In the dialog, check the **Enable Status Override** checkbox.
5. In the **Status Code** dropdown, select your desired error code (`500`, `503`, or `504`).
6. In the **Failure %** field, enter your desired failure probability between `0` and `100` (e.g., `50` for a 50% chance). You can use the spinner arrows (which increment by 5%) or type directly.
7. Click **OK** to save the rule.
8. Verify that the **Chaos Summary** column in the **Chaos Rules** table updates to reflect your rule, for example:
   ```text
   Latency: OFF | Status: 500 (50%) | Reset: OFF
   ```

The rule takes effect immediately—no proxy restart is needed.

## Worked Examples: Deterministic Failures vs. Flakiness

The **Failure %** setting allows you to simulate two distinct real-world testing scenarios:

### Example 1: 100% Failure (Deterministic Outage Testing)
- **Configuration**: Status Code = `500`, Failure % = `100%`.
- **Behavior**: Every single request arriving on this route immediately returns `500 Internal Server Error (Chaos Override)`.
- **When to Use**: When you are building a new error banner, modal, or fallback UI component, and you need guaranteed failures on every reload so you can inspect and style the user interface without intermittent successes.

### Example 2: 20% Failure (Probabilistic Flakiness & Retry Testing)
- **Configuration**: Status Code = `503`, Failure % = `20%`.
- **Behavior**: On each independent request, there is a 20% statistical chance of receiving a `503 Service Unavailable (Chaos Override)` and an 80% chance of successfully reaching the real API.
- **When to Use**: When testing automated retry mechanisms, exponential backoff policies, or verifying that intermittent transient network hiccups do not completely crash user workflows.

> **Warning:** **Failure % is probabilistic, NOT sequential.** A 20% setting does *not* mean "the 5th request will fail." It means every incoming request rolls an independent 100-sided die: if the roll is less than 20, it fails. If you send 10 requests, you can expect roughly 2 failures, but due to random probability, you might see 1, 3, or even 0 failures in a small sample.

## Field-by-Field Reference

| Field / Control | What it does | Valid values / options | Default |
|---|---|---|---|
| **Enable Status Override** | Master toggle that enables or disables HTTP error injection for this route. | Checked (`true`) or Unchecked (`false`). | Unchecked (`false`) |
| **Status Code** | The specific HTTP error status code returned to the client when a failure triggers. | Choice of `500`, `503`, or `504`. | `500` |
| **Failure %** | The percentage chance (probability) that an individual incoming request triggers the override. | Integer between `0` and `100`. Step size: `5%`. | `0` |

### Supported Status Codes & Their Real-World Meanings

| Code | HTTP Description | Real-World Scenario Simulated |
|---|---|---|
| **500** | **Internal Server Error** | Simulates an unhandled backend crash, database query error, or uncaught exception inside the remote application. Response body: `Internal Server Error (Chaos Override)`. |
| **503** | **Service Unavailable** | Simulates a remote service that is temporarily offline due to scheduled maintenance, server overload, or rate limits. Response body: `Service Unavailable (Chaos Override)`. |
| **504** | **Gateway Timeout** | Simulates an edge proxy, load balancer, or reverse proxy that timed out waiting for a deeper upstream microservice. Response body: `Gateway Timeout (Chaos Override)`. |

## Recognizing Overridden Requests in the Inspector

When a status code override triggers, EntropyLab logs the event with distinctive attributes in the **Inspector** tab:

1. **Status Column**: Shows the injected error code (e.g., `500`, `503`, or `504`).
2. **Type Column**: Displays **`CHAOS_STATUS`** instead of `FORWARDED`, immediately highlighting that the request was intercepted by the chaos engine.
3. **Execution Duration**: Extremely low (typically under `5 ms`, unless combined with Latency Injection) because no network transit occurred.
4. **Target URL**: Blank/empty in database logs, verifying that no outbound connection was ever made to the real API.
5. **Response Body**: Displays plain text: `<Status Description> (Chaos Override)`.

## Common Mistakes & Troubleshooting

- **Expecting Sequential Failures**: Remember that a 50% failure rate does not alternate strictly between success and failure (`Success, Fail, Success, Fail`). Because rolls are random, clusters of failures or successes are normal.
- **Forgetting to Check "Enable Status Override"**: If you configure the status code and failure percentage but leave the checkbox unchecked, all requests will forward normally. Check that the table summary does not say `Status: OFF`.
- **Precedence Over Connection Reset**: If both Connection Reset and Status Code Override are enabled and both trigger on the same request, **Connection Reset takes precedence** (the socket is severed, and no status code is sent).

## Related Reading

- [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md)
- [Latency Injection Reference](./02-latency-injection.md)
- [Connection Reset Reference](./04-connection-reset.md)
- [Sub-Path Filtering & Combining Rules](./05-sub-path-filtering-and-combining-rules.md)
- [How-To Recipe: Simulate a Flaky API](../06-how-to-recipes/02-simulate-a-flaky-api.md)
