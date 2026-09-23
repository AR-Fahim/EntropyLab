# Core Concepts: How Reverse Proxies & Chaos Work
*For: beginners seeking a clear mental model, with technical callouts for experienced engineers.*

## What Is It?

To understand EntropyLab, we first need to understand what a **proxy** is in everyday life.

Imagine you want to buy specialized goods from an overseas manufacturer, but they do not deliver directly to your private home address. Instead, you sign up with a local package-forwarding service in your city. When placing orders, you give the seller the address of the forwarding depot. When the package arrives at the depot, the forwarder inspects the parcel, logs its arrival, and sends it to your doorstep. 

In this analogy, the forwarding service is a **proxy**: an intermediary that sits between two parties, relaying messages or items so that neither party communicates directly without passing through the middle.

A **reverse proxy** is a specific type of proxy server that sits in front of one or more destination servers and handles incoming requests on their behalf. Instead of your computer reaching out across the internet directly to a remote service, your application talks directly to the reverse proxy running locally on your own machine. The reverse proxy receives the request, decides what to do with it, and forwards it to the real external destination.

EntropyLab is a specialized reverse proxy built specifically for local development and **chaos engineering**—deliberately introducing failures into a system to test how well it survives them.

> **Warning:** EntropyLab does **not** transparently intercept your computer's global web traffic or perform "Man-in-the-Middle" (MITM) HTTPS decryption like tools such as Charles Proxy, Fiddler, or mitmproxy. Running EntropyLab will not alter your operating system's global proxy settings. To route traffic through EntropyLab, you must explicitly point your application's API calls to EntropyLab's local address (for example, `http://localhost:8080`). For a comprehensive explanation of this architecture and why it keeps your system secure and clean, see [Limitations & FAQ](../07-troubleshooting/02-limitations-and-faq.md).

> **For Experts:** Unlike L7 forward proxies that require installing a trusted root CA certificate to decrypt outbound TLS traffic, EntropyLab operates strictly as an application-level reverse proxy. Your application sends plain HTTP requests to `localhost:<port>`, where EntropyLab handles route matching, mock evaluation, and chaos injection before dispatching an outbound HTTPS request to the real upstream server. This eliminates TLS certificate management while keeping network manipulation fully deterministic.

## Why It Matters / The Problem It Solves

Testing how an application behaves during network anomalies is notoriously difficult when connected directly to live external APIs. If an external API server is fast and healthy, you cannot easily test whether your user interface displays a loading skeleton or a progress bar. If an external payment service never returns an error in development, you cannot easily confirm whether your retry logic or error alert functions properly.

By routing your traffic through a local reverse proxy, you gain a powerful control point. Because all requests pass through EntropyLab, you can inspect the exact headers and bodies being exchanged, inject artificial delays, return simulated server outages, or swap in pre-recorded mock responses on demand—all without modifying the code of the remote services.

## How It Works

### The Request Flow: Direct vs. EntropyLab

Consider what happens when your application communicates with an external service like GitHub's API.

**Direct Communication (Standard Flow):**
```text
Your App ───────────────────────────────► https://api.github.com
Your App ◄─────────────────────────────── https://api.github.com
```
In standard direct communication, your application connects directly to the remote server over the internet. You have no local visibility into the raw transmission and no way to simulate poor network conditions.

**Through EntropyLab (Reverse Proxy Flow):**
```text
Your App ──► localhost:8080/github ──► EntropyLab ──► https://api.github.com
                                            │
                                            ▼
Your App ◄── localhost:8080/github ◄── EntropyLab ◄── https://api.github.com
```
When using EntropyLab:
1. You configure your application to send requests to a local address (such as `http://localhost:8080/github/users`).
2. EntropyLab intercepts the request locally.
3. EntropyLab inspects the URL, checks for mocks or chaos rules, and optionally relays the request to the upstream server (`https://api.github.com/users`).
4. When the upstream server responds, EntropyLab logs the response, applies any configured rules, and returns the response to your application.

### The 4 Building Blocks of EntropyLab

EntropyLab organizes its functionality into four core building blocks:

1. **Routes (Route Mapping)**: A **route** is a configuration rule that connects a local path on your machine (such as `/github`) to a real API's base URL (such as `https://api.github.com`). Routes define where incoming traffic should be forwarded when it reaches EntropyLab.
2. **Chaos Rules (Chaos Engine)**: A set of conditions applied to a route to simulate hostile or degraded network environments. Within a chaos rule, you can enable **latency injection** (artificially delaying a response to simulate a slow network or overloaded server), a **status code override** (forcing a request to fail with a specific HTTP error code such as 500, 503, or 504 instead of reaching the real API), or a **connection reset** (abruptly closing the connection with no response at all, simulating a hard crash).
3. **The Inspector**: The **Inspector** tab displays a live, searchable history of every request and response that passes through EntropyLab. It lets you examine HTTP methods, target paths, execution duration in milliseconds, status codes, and formatted request and response bodies.
4. **Mocks (Mock Mapping)**: A **mock** is a saved, fake response served instantly to your application instead of contacting the real API. You can create manual mocks by attaching pre-written JSON files, or use the **Auto-Mock Snapshot** feature to capture real live responses directly from the Inspector and save them as permanent local mocks with a single click.

### Complete Lifecycle of a Request

When your application issues a request to EntropyLab, the proxy executes the following sequence:

1. **Local Reception**: EntropyLab receives the request on its configured local port (e.g., `8080`).
2. **Mock Evaluation**: EntropyLab checks whether an exact path match exists in the **Mocks** tab. If an enabled mock exists, EntropyLab immediately returns the saved response content to your app—no external network traffic occurs.
3. **Route Matching**: If no mock exists, EntropyLab matches the URL path against your configured **Routes** to find the corresponding target base URL.
4. **Chaos Evaluation**: If the matched route has active chaos rules, EntropyLab applies them in a strict order:
   - **Latency**: If enabled, the request is paused for the configured number of milliseconds.
   - **Connection Reset**: If triggered, EntropyLab abruptly drops the TCP connection immediately, simulating a catastrophic server failure.
   - **Status Code Override**: If triggered, EntropyLab bypasses the real server and returns an HTTP error code (500, 503, or 504).
5. **Upstream Forwarding**: If the request was not aborted by a connection reset or status override, EntropyLab forwards the request to the upstream target URL over the internet.
6. **Logging & Return**: EntropyLab logs the transaction details in the **Inspector** tab and returns the response headers and payload back to your application.

## Key Terms Introduced Here

- **Proxy**: An intermediary program that sits between two computers to relay requests and responses.
- **Reverse Proxy**: A local server that receives your requests and forwards them to a real API on your behalf, so you can intercept and modify traffic in between.
- **Route / Route Mapping**: A rule connecting a local path (e.g. `/github`) to a real API's base URL (e.g. `https://api.github.com`).
- **Chaos Engineering**: Deliberately introducing failures into a system to test how well it survives them.
- **Latency Injection**: Artificially delaying a response to simulate a slow network or overloaded server.
- **Status Code Override**: Forcing a request to fail with a specific HTTP error code (500/503/504) instead of reaching the real API.
- **Connection Reset**: Abruptly closing the connection with no response at all, simulating a hard crash (worse than a graceful error).
- **Mock / Mock Mapping**: A saved, fake response served instantly instead of contacting a real API.
- **Auto-Mock Snapshot**: Capturing a REAL response you've already seen in the Inspector and turning it into a permanent Mock with one click.
- **Inspector**: The tab showing a live, searchable history of every request/response that passed through EntropyLab.

## Related Reading

- [Welcome to EntropyLab](./01-welcome.md)
- [Installation and First Launch](./03-installation-and-first-launch.md)
- [Quick Start Tutorial: Your First Chaos Test](./04-quick-start-tutorial.md)
- [Limitations & FAQ: Reverse Proxy vs. MITM Tools](../07-troubleshooting/02-limitations-and-faq.md)
- [Complete Glossary of Terms](../08-reference/01-glossary.md)
