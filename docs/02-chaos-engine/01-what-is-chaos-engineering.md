# What Is Chaos Engineering?
*For: developers and testers learning how to build resilient systems by simulating realistic network failures.*

## What Is It?

To understand chaos engineering, consider a familiar real-world practice: the **fire drill**.

In an office building, school, or hospital, safety teams do not wait for an actual fire to break out to discover whether emergency exit doors are jammed, whether the alarm sirens are audible, or whether occupants know where to assemble. Instead, they deliberately schedule a fire drill. They sound the alarm under controlled, safe conditions to observe how the facility and its people respond, exposing hidden flaws before an actual emergency strikes.

In software development, **chaos engineering** is the digital equivalent of a fire drill. It is the discipline of deliberately introducing controlled failures into a system to test how well it survives them. 

Rather than hoping that your application will behave sensibly when an external service fails, you intentionally inject realistic network disruptions—such as severe latency, server outages, or severed connections—to verify that your error handling, fallback interfaces, and recovery mechanisms perform as designed.

## Why It Matters / The Problem It Solves

When developing an application locally on your computer, conditions are almost always unrealistically perfect. Your local machine is fast, database queries return in single-digit milliseconds, and API calls complete cleanly with successful `200 OK` status codes.

The production internet, however, is hostile, noisy, and unpredictable:
- Upstream third-party servers become overloaded and take 20 seconds to reply.
- Payment processors and identity providers suffer transient database outages.
- Mobile networks fluctuate, dropping TCP connections halfway through a payload transfer.

Most software applications are never tested against these edge cases until an outage hits in production. When an unexpected failure occurs, untested apps often freeze indefinitely, show blank white screens, crash abruptly without error logging, or trap users in unclickable states.

Chaos engineering eliminates this blind spot. By deliberately testing under hostile conditions before releasing to users, you can confirm that:
- Loading skeletons, spinners, and progress indicators inform users when an API is slow.
- Informative, polite error messages replace cryptic crash screens.
- Automatic retries, timeouts, and circuit breakers protect backend services from cascading failures.
- Offline and fallback states allow users to continue working even when a dependency is unreachable.

## How It Works: The Chaos Workflow & The 3 Chaos Tools

### The Scoped Chaos Workflow

In EntropyLab, chaos is never an indiscriminate, all-or-nothing switch that breaks your entire machine's internet connection. Instead, **chaos rules are attached directly to specific routes** that you define in the **Routes** tab.

Because chaos rules are bound to individual routes:
1. You can test a failing payment gateway (`/stripe`) without disrupting your authentication provider (`/auth`) or your local database.
2. Your regular web browsing and development tooling remain completely unaffected.
3. You can enable or disable chaos injection instantly with zero application restarts.

### The 3 Chaos Tools in EntropyLab

EntropyLab equips you with three specialized tools to simulate the most common failure modes observed in modern distributed systems:

#### 1. Latency Injection (Simulating Slow Networks & Congested Servers)
In production, systems rarely fail by crashing instantly. More often, they degrade under load, responding sluggishly over 5, 10, or 30 seconds. **Latency injection** artificially delays a response to simulate a slow network or overloaded server. This allows you to verify whether your user interface displays smooth loading states, whether your HTTP clients enforce appropriate timeout thresholds, and whether slow responses block other parallel operations.

#### 2. Status Code Override (Simulating Service Outages & Gateway Errors)
When an upstream service experiences internal database errors, deploys broken code, or undergoes maintenance, it typically returns HTTP error status codes. A **status code override** forces a request to fail with a specific HTTP error code (such as `500 Internal Server Error`, `503 Service Unavailable`, or `504 Gateway Timeout`) instead of reaching the real API. This lets you confirm that your application accurately distinguishes between success and failure, presents helpful user notifications, and triggers retry policies when appropriate.

#### 3. Connection Reset (Simulating Hard Crashes & Broken Sockets)
A connection reset is fundamentally harsher than an HTTP error status code. When a remote server suffers an abrupt power outage, a fatal process crash, or a severed physical connection, it cannot send a polite HTTP error response. Instead, the network socket is abruptly terminated. A **connection reset** abruptly closes the connection with no response at all, simulating a hard crash (worse than a graceful error). This simulates a total network disruption and tests whether your low-level networking libraries handle sudden socket drops without crashing the host process.

## Key Terms Introduced Here

- **Chaos Engineering**: Deliberately introducing failures into a system to test how well it survives them.
- **Latency Injection**: Artificially delaying a response to simulate a slow network or overloaded server.
- **Status Code Override**: Forcing a request to fail with a specific HTTP error code (500/503/504) instead of reaching the real API.
- **Connection Reset**: Abruptly closing the connection with no response at all, simulating a hard crash (worse than a graceful error).
- **Route / Route Mapping**: A rule connecting a local path (e.g. `/github`) to a real API's base URL (e.g. `https://api.github.com`).

## Related Reading

- [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)
- [Latency Injection Reference](./02-latency-injection.md)
- [Status Code Override Reference](./03-status-code-override.md)
- [Connection Reset Reference](./04-connection-reset.md)
- [Sub-Path Filtering & Combining Rules](./05-sub-path-filtering-and-combining-rules.md)
