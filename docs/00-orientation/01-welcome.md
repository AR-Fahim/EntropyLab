# Welcome to EntropyLab
*For: everyone, especially beginners — read this before anything else.*

## What Is It?

EntropyLab is a desktop application that lets you safely test how your computer programs behave when internet connections get slow, break, or fail completely.

Under the hood, EntropyLab is a local **reverse proxy** and **chaos engineering** studio designed for Windows. It runs directly on your computer, sitting between your application (such as a browser frontend, mobile app, or backend service) and the external APIs it communicates with. By directing your network requests through EntropyLab, you can monitor traffic in real time, simulate disruptive network conditions like high latency or sudden server crashes, and return saved mock responses—all without modifying your application's source code or deploying test stubs to a remote server.

### Who It Is For

EntropyLab is built for frontend and backend developers, QA professionals, and software testers who need to guarantee that their software handles real-world network failures gracefully. If your application depends on remote HTTP APIs—such as authentication providers, payment processors, or third-party data feeds—EntropyLab helps you verify that your loading spinners, timeout handlers, retry policies, and fallback user interfaces operate properly when things go wrong.

### 100% Local and Private

EntropyLab is **100% local**:
- It requires no cloud account, signup, or login.
- It sends zero telemetry or usage metrics to external servers.
- It has no internet dependency whatsoever, except for reaching the actual target APIs you choose to proxy.
- All configuration files, captured traffic logs, and saved mock responses stay entirely on your local machine.

### Core Capabilities at a Glance

EntropyLab provides five primary capabilities:

- **Reverse Proxy**: Relays your application's HTTP requests to target APIs, allowing you to intercept, monitor, and manipulate traffic along the way.
- **Chaos Engine**: Deliberately injects controlled network failures—such as artificial latency delays, HTTP server errors (500, 503, 504), or sudden connection drops—into selected endpoints.
- **Traffic Inspector**: Displays a live, searchable history of every request and response passing through the proxy, complete with status codes, execution timings, and headers.
- **Manual Static Mocks**: Instantly serves pre-defined JSON response files for designated paths without contacting external servers.
- **Auto-Mock Snapshot**: Converts any live API response captured in the Inspector into a permanent local mock file with a single click.

## Why It Matters / The Problem It Solves

Have you ever wondered what happens to your application when a payment gateway hangs for 30 seconds, or when an inventory API crashes during peak traffic?

In typical development environments, verifying these edge cases is frustrating and time-consuming. Developers usually have three imperfect choices:
1. Write temporary, throwaway code to simulate exceptions and remember to remove it before pushing to production.
2. Manually disable their computer's network adapter, which severs all connections indiscriminately.
3. Hope that their error-handling logic works correctly when an outage inevitably happens in production.

EntropyLab eliminates this friction. By placing a configurable local proxy between your software and the outside world, you can simulate an unreliable API in seconds. You can force checkout calls to delay by 8,000 milliseconds, configure a third-party login service to return `503 Service Unavailable` on 25% of attempts, or drop the TCP socket entirely to simulate a sudden server crash. Your application's resilience can be thoroughly tested, debugged, and proven before your code ever reaches a user—keeping your production codebase clean and free of test-only hacks.

## How It Works

To understand how EntropyLab operates, imagine sending an urgent letter through a personal assistant rather than dropping it directly into a public mailbox. 

Before handing the letter to the postal carrier, your assistant can:
- Note the destination address and keep a record of when it was sent.
- Hold onto the letter for ten minutes to simulate a postal delay.
- Hand it back to you immediately with a note saying "Service Down" without ever sending it.
- Give you a pre-written standard reply stored in their desk drawer.
- Forward the letter normally, await the reply, log the result, and hand the response back to you.

Technically, instead of configuring your app to call an external service directly (for example, `https://api.stripe.com/v1/charges`), you configure your app to call your local EntropyLab proxy (such as `http://localhost:8080/stripe/v1/charges`). EntropyLab checks the requested path, applies any active chaos rules (like delay or failure rates), checks whether a mock file exists, and forwards permitted requests to the real API. The result is recorded in the **Inspector** tab and returned to your app.

> **Note:** A detailed walkthrough of reverse proxy mechanics and how they differ from traditional inspection tools is provided in [Core Concepts: How Reverse Proxies & Chaos Work](./02-core-concepts.md).

### How to Read This Manual

This documentation is designed to serve readers of all background levels. Choose the path that matches your current goal:

- **Nadia (The Beginner)**: If you are new to proxies, network testing, or APIs, read this guide in order. Start with [Core Concepts](./02-core-concepts.md), proceed to [Installation and First Launch](./03-installation-and-first-launch.md), and follow the hands-on [Quick Start Tutorial](./04-quick-start-tutorial.md). Every technical term is explained step-by-step.
- **Marcus (The Intermediate Developer)**: If you already understand REST APIs, HTTP status codes, and tools like Postman, skim [Core Concepts](./02-core-concepts.md) and jump directly into the practical recipes in [How-To Recipes](../06-how-to-recipes/01-simulate-a-slow-api.md). You will find concise instructions for simulating slow networks, flaky endpoints, and API outages.
- **Priya (The Expert Engineer)**: If you have extensive experience with tools like WireMock, Charles Proxy, or Toxiproxy, skip the conceptual primers and jump directly to the [Complete UI Field Reference](../08-reference/02-complete-ui-field-reference.md) or the [Proxy & Routes Reference](../01-proxy-and-routes/01-understanding-and-managing-routes.md). Look for `> **For Experts:**` callouts throughout the documentation for rapid summaries of execution order, parameter limits, and architectural specifics.

## Key Terms Introduced Here

- **Reverse Proxy**: A local server that receives your requests and forwards them to a real API on your behalf, so you can intercept and modify traffic in between.
- **Chaos Engineering**: Deliberately introducing failures into a system to test how well it survives them.
- **Latency Injection**: Artificially delaying a response to simulate a slow network or overloaded server.
- **Status Code Override**: Forcing a request to fail with a specific HTTP error code (500/503/504) instead of reaching the real API.
- **Connection Reset**: Abruptly closing the connection with no response at all, simulating a hard crash (worse than a graceful error).
- **Mock / Mock Mapping**: A saved, fake response served instantly instead of contacting a real API.
- **Auto-Mock Snapshot**: Capturing a REAL response you've already seen in the Inspector and turning it into a permanent Mock with one click.
- **Inspector**: The tab showing a live, searchable history of every request/response that passed through EntropyLab.

## Related Reading

- [Core Concepts: How Reverse Proxies & Chaos Work](./02-core-concepts.md)
- [Installation and First Launch](./03-installation-and-first-launch.md)
- [Quick Start Tutorial: Your First Chaos Test](./04-quick-start-tutorial.md)
- [Complete Glossary of Terms](../08-reference/01-glossary.md)
