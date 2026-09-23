# EntropyLab User Manual & Master Index

Welcome to the complete official documentation for **EntropyLab**, a local reverse proxy and chaos engineering studio designed for Windows developers.

---

## Where Should I Start?

Choose your path based on your experience and current objective:

- **New to API testing?**  
  👉 **Start at [Orientation](./00-orientation/01-welcome.md)**. Read the core concepts and follow the step-by-step Quick Start Tutorial to send and inspect your very first proxied request.
- **Know APIs, but new to chaos engineering?**  
  👉 **Jump to [How-To Recipes](./06-how-to-recipes/01-simulate-a-slow-api.md)**. Pick a hands-on recipe to simulate slow networks, intermittent outages, or abrupt socket crashes in under two minutes.
- **Need a quick fact, port limit, or field range?**  
  👉 **Go straight to [Complete UI Field Reference](./08-reference/02-complete-ui-field-reference.md)** or check the [Glossary of Terms](./08-reference/01-glossary.md) for instant lookups without narrative prose.

---

## Module A: Orientation
*Foundational mental models, setup procedures, and a frictionless first-run walkthrough.*

- [01. Welcome to EntropyLab](./00-orientation/01-welcome.md) — What EntropyLab is, who it is for, core capabilities, and why local chaos testing matters.
- [02. Core Concepts: How Reverse Proxies & Chaos Work](./00-orientation/02-core-concepts.md) — Plain-language proxy analogies, the reverse proxy request flow, and architectural safeguards.
- [03. Installation and First Launch](./00-orientation/03-installation-and-first-launch.md) — Installing the Windows package, AppData initialization, theme defaults, and understanding the empty first-launch state.
- [04. Quick Start Tutorial: Your First Route & Inspected Request](./00-orientation/04-quick-start-tutorial.md) — A step-by-step, zero-assumption tutorial creating a route, starting the proxy, and inspecting live GitHub traffic.

---

## Module B: Proxy & Routes
*Configuring, matching, and maintaining reverse proxy forwarding endpoints.*

- [01. Understanding and Managing Routes](./01-proxy-and-routes/01-understanding-and-managing-routes.md) — Longest-prefix path matching, remainder path calculation, query string preservation, and route lifecycle actions.
- [02. Proxy Control: Starting, Stopping, and Ports](./01-proxy-and-routes/02-proxy-control.md) — Binding to custom TCP ports, starting/stopping the server, and resolving port conflict errors.

---

## Module C: Chaos Engine
*Simulating real-world network degradations, outages, and catastrophic socket drops.*

- [01. What Is Chaos Engineering?](./02-chaos-engine/01-what-is-chaos-engineering.md) — Conceptual introduction to controlled failure injection using the building fire drill analogy.
- [02. Latency Injection: Simulating Slow Networks](./02-chaos-engine/02-latency-injection.md) — Delaying responses by 0–60,000 ms to test UI loading states and client timeout limits.
- [03. Status Code Override: Simulating HTTP Outages & Flakiness](./02-chaos-engine/03-status-code-override.md) — Faking local HTTP 500, 503, and 504 errors using an independent per-request probability model.
- [04. Connection Reset: Simulating Hard Crashes & Dropped Sockets](./02-chaos-engine/04-connection-reset.md) — Abruptly severing raw TCP sockets with no HTTP response to test catastrophic failure handling.
- [05. Sub-Path Filtering & Combining Chaos Rules](./02-chaos-engine/05-sub-path-filtering-and-combining-rules.md) — Scoping chaos to specific sub-paths and mastering the deterministic execution pipeline.

---

## Module D: Traffic Inspector
*Auditing, inspecting, and snapshotting network exchanges in real time.*

- [01. Reading & Using Traffic History](./03-inspector/01-reading-and-using-traffic-history.md) — Live-updating traffic log columns, understanding the 4 request types, and permanent SQLite audit storage.
- [02. Inspecting Details and Auto-Mock](./03-inspector/02-inspecting-details-and-auto-mock.md) — Deep 4-panel inspection, pretty-printed JSON formatting, and single-click Auto-Mock Snapshots.

---

## Module E: Mocking System
*Building and serving static JSON endpoints for offline and prototype testing.*

- [01. Manual Static Mocking](./04-mocking/01-manual-static-mocking.md) — Authoring custom JSON mock payloads and linking them to local paths using exact-path matching.
- [02. Managing Mocks and Auto-Mock Details](./04-mocking/02-managing-mocks-and-auto-mock-details.md) — Mock lifecycle management, in-place snapshot updates, and the absolute priority rule (Mocks vs. Routes).

---

## Module F: Settings & Data Storage
*Configuration persistence, file locations, and backup routines.*

- [01. Theme & Data Storage](./05-settings-and-data/01-theme-and-data-storage.md) — Dark mode toggle behavior, `%APPDATA%\EntropyLab\` layout (`config.json`, `entropylab.db`, `mocks\`), and migration steps.

---

## Module G: How-To Recipes
*Actionable, step-by-step recipes for validating specific application resilience goals.*

- [01. How-To: Simulate a Slow API](./06-how-to-recipes/01-simulate-a-slow-api.md) — Inject a 3,000 ms delay to test loading spinners, skeleton screens, and timeout thresholds.
- [02. How-To: Simulate a Flaky API](./06-how-to-recipes/02-simulate-a-flaky-api.md) — Configure an intermittent 30% failure rate with HTTP 503 to verify automated retry policies.
- [03. How-To: Simulate a Hard Crash](./06-how-to-recipes/03-simulate-a-hard-crash.md) — Sever TCP connections on 100% of requests to test socket disconnect resilience.
- [04. How-To: Combine Latency and Failure](./06-how-to-recipes/04-combine-latency-and-failure.md) — Stack a 2,000 ms delay with a 25% failure rate to simulate an overloaded, degraded service.
- [05. How-To: Unblock Frontend with Manual Mocks](./06-how-to-recipes/05-unblock-frontend-with-manual-mocks.md) — Create and serve a mock endpoint so frontend development continues before backend APIs exist.
- [06. How-To: Capture Real Data with Auto-Mock](./06-how-to-recipes/06-capture-real-data-with-auto-mock.md) — Capture a live API response in one click and develop completely offline without rate limits.
- [07. How-To: Scope Chaos to One Endpoint](./06-how-to-recipes/07-scope-chaos-to-one-endpoint.md) — Isolate chaos injection to a single risky sub-path while leaving sibling endpoints fast and stable.
- [08. Capstone: Full End-to-End Resilience Testing & Mocking Workflow](./06-how-to-recipes/08-capstone-full-resilience-test.md) — A comprehensive end-to-end resilience test uniting all EntropyLab features in a single checkout scenario.

---

## Module H: Troubleshooting & FAQ
*Error recovery, platform boundaries, and architectural clarifications.*

- [01. Common Errors and Fixes](./07-troubleshooting/01-common-errors-and-fixes.md) — A complete Error \| Cause \| Fix directory resolving 404s, 502s, port conflicts, and unserved mocks.
- [02. Limitations & Frequently Asked Questions (FAQ)](./07-troubleshooting/02-limitations-and-faq.md) — Why EntropyLab is a reverse proxy (not MITM), Windows platform scoping, and answers to common questions.

---

## Module I: Reference & Lexicon
*High-density reference indices for quick technical lookup.*

- [01. Glossary of Terms](./08-reference/01-glossary.md) — An alphabetized index defining every technical term and architectural concept across the manual.
- [02. Complete UI Field Reference](./08-reference/02-complete-ui-field-reference.md) — Comprehensive tab-by-tab tables listing every button, field, valid range, and default value.

---

## Module J: Appendix
*Project background, core technologies, and release history.*

- [01. About & Credits](./09-appendix/01-about-and-credits.md) — Release notes for v1.0.0, runtime architecture (Java 21, JavaFX, SQLite WAL, Jackson), and developer acknowledgments.
