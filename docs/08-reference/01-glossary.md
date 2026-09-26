# Glossary of Terms
*For: all developers seeking clear, concise, and canonical definitions of EntropyLab terminology.*

This glossary defines every core concept, UI label, and architectural term used across the EntropyLab documentation. Entries are alphabetized and linked directly to their comprehensive reference pages.

---

### AppData Directory (`%APPDATA%\EntropyLab\`)
The private local directory on Windows where EntropyLab stores all user-specific data, including `config.json` (settings, routes, rules), `entropylab.db` (traffic history database), and the `mocks\` directory. This folder is completely self-contained, offline, and preserved across application updates.  
*See full reference:* [Theme & Data Storage: Where Your Data Lives](../05-settings-and-data/01-theme-and-data-storage.md)

### Auto-Mock Snapshot
The single-click capability in the Traffic Inspector that captures a live HTTP response payload from a real API and automatically saves it as a permanent local mock. Auto-mock files are saved to `%APPDATA%\EntropyLab\mocks\` and automatically registered in the Mocks tab.  
*See full reference:* [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)

### Chaos Engineering
The discipline of deliberately introducing controlled failures into a software system to verify that it survives degraded or hostile network conditions gracefully. In EntropyLab, chaos engineering is implemented via Latency Injection, Status Code Overrides, and Connection Resets attached to specific routes.  
*See full reference:* [What Is Chaos Engineering?](../02-chaos-engine/01-what-is-chaos-engineering.md)

### Connection Reset
A chaos engine failure mode that abruptly severs the underlying TCP network socket while processing a request, returning zero HTTP headers, zero status codes, and zero body bytes. This simulates hard server panics, power losses, and network drops, testing low-level socket exception handling in client applications.  
*See full reference:* [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md)

### Enabled (Checkbox)
A toggle switch present in Routes, Mocks, and Chaos configurations that activates or deactivates that specific feature live in real time. Disabling a route causes it to return a 404 response; disabling a mock allows matching traffic to fall through to standard route forwarding.  
*See full reference:* [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md) | [Managing Mocks](../04-mocking/02-managing-mocks-and-auto-mock-details.md)

### Error Rate
The percentage of all recorded HTTP requests in EntropyLab's database that resulted in an HTTP error status code (`400`+) or an intentional, simulated chaos failure (`CHAOS_STATUS` or `CHAOS_RESET`). Displayed in the Analytics tab and highlighted in red whenever errors are present.  
*See full reference:* [Analytics Dashboard: Aggregated Metrics & Fault Distribution](../03-inspector/03-analytics-dashboard.md)

### Failure %
The probabilistic percentage chance (`0`% to `100`%) that an individual incoming request will trigger an active Status Code Override. It operates as an independent random roll on each request rather than a sequential counter ("fail every Nth request").  
*See full reference:* [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)

### Inspector
The primary monitoring tab in EntropyLab that maintains a live, real-time audit log of every HTTP request and response passing through the reverse proxy. It displays timestamps, HTTP verbs, paths, durations, status codes, and execution types, with an in-depth modal for inspecting raw headers and formatted JSON payloads.  
*See full reference:* [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)

### Latency Injection
The deliberate insertion of an artificial time delay (measured in milliseconds from `0` to `60000`) before returning an HTTP response. Used to simulate slow mobile connections, saturated Wi-Fi, or high server load to test UI loading states and client timeout thresholds.  
*See full reference:* [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md)

### Local Path
The URL path prefix on `http://localhost:<port>` that EntropyLab intercepts. For routes, it defines the prefix matched and stripped before forwarding; for mocks, it defines the exact path that triggers the local mock file.  
*See full reference:* [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)

### Longest-Prefix Matching
The deterministic path-resolution algorithm EntropyLab uses to match incoming requests to configured routes. If multiple routes match the beginning of a URL, the route with the longest character length always wins.  
*See full reference:* [How Route Matching Works](../01-proxy-and-routes/01-understanding-and-managing-routes.md#how-route-matching--forwarding-works)

### Manual Mock vs. Auto-Generated Mock
The two origin categories of mocks displayed in the Mocks tab's **Auto-Generated** column:
- **Manual Mock (`Auto-Generated: No`)**: Hand-crafted by the user using the **Add Mock** dialog, pointing to a custom `.json` file anywhere on the local filesystem.
- **Auto-Generated Mock (`Auto-Generated: Yes`)**: Created automatically by clicking **Save as Mock** in the Inspector, saving captured live data into `%APPDATA%\EntropyLab\mocks\`.  
*See full reference:* [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)

### Man-in-the-Middle (MITM) Proxy
A proxy architecture (used by tools like Charles Proxy and mitmproxy) that transparently intercepts all computer network traffic by altering OS proxy settings and installing custom Root CA certificates. EntropyLab is **not** a MITM proxy; it is a reverse proxy requiring explicit application targeting.  
*See full reference:* [Limitations & FAQ: Reverse Proxy vs. MITM](../07-troubleshooting/02-limitations-and-faq.md)

### Mock / Mock Mapping
A configuration rule that intercepts an incoming HTTP request and immediately returns a saved `.json` file from local disk with an HTTP 200 status, without contacting any external network. Mocks evaluate before routes and require an exact path match.  
*See full reference:* [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md)

### Mutation Intensity
The configuration setting controlling how many characters are randomly corrupted per response when Payload Mutation is enabled. Specifies the exact number of characters randomly replaced with printable ASCII characters in a successfully forwarded response body.  
*See full reference:* [Payload Mutation: Testing Malformed JSON & Corrupted Data](../02-chaos-engine/06-payload-mutation.md)

### Payload Mutation
A chaos engine failure mode that randomly corrupts characters in a real upstream HTTP response body after it returns successfully from the target API, testing client application resilience against malformed JSON, deserialization crashes, and data corruption.  
*See full reference:* [Payload Mutation: Testing Malformed JSON & Corrupted Data](../02-chaos-engine/06-payload-mutation.md)

### Percentile Response Time (p50/p95)
Statistical latency distribution metrics that represent user experience more accurately than averages. The **p50 (median)** response time indicates that 50% of requests completed faster than this duration, while the **p95** response time indicates that 95% of requests completed faster (capturing tail latency and worst-case performance).  
*See full reference:* [Analytics Dashboard: Aggregated Metrics & Fault Distribution](../03-inspector/03-analytics-dashboard.md)

### Port / Port Binding
The local TCP network port (default `8080`, valid range `1`–`65535`) where EntropyLab binds its internal HTTP server. Applications send their requests to `http://localhost:<port>/<path>`. Changing ports requires stopping and restarting the proxy engine.  
*See full reference:* [Proxy Control: Ports and Lifecycle](../01-proxy-and-routes/02-proxy-control.md)

### Remainder Path
The trailing portion of an incoming URL path that remains after the matched **Local Path** prefix is stripped away. EntropyLab appends the remainder path (along with any query parameters) directly onto the **Target Base URL** when forwarding traffic.  
*See full reference:* [Understanding & Managing Routes: Path Translation](../01-proxy-and-routes/01-understanding-and-managing-routes.md#the-request-translation-lifecycle)

### Request Types (`FORWARDED`, `MOCKED`, `CHAOS_STATUS`, `CHAOS_RESET`)
The four canonical classifications recorded in the Inspector's **Type** column:
- **`FORWARDED`**: The request was successfully relayed across the internet to the real upstream API, and a live response was received.
- **`MOCKED`**: The request was intercepted by an active mock mapping and served locally from disk without external network traffic.
- **`CHAOS_STATUS`**: The request was intercepted by a Status Code Override rule, returning a simulated 500, 503, or 504 error locally.
- **`CHAOS_RESET`**: The request was severed abruptly by a Connection Reset rule, closing the TCP socket with no HTTP response.  
*See full reference:* [Inspector: Request Types at a Glance](../03-inspector/01-reading-and-using-traffic-history.md#request-types-at-a-glance)

### Reset %
The probability (`0`% to `100`%) that an incoming request on a route with Connection Reset enabled will have its underlying TCP socket severed immediately. Evaluated before Status Code Overrides.  
*See full reference:* [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md)

### Reverse Proxy
A local server that receives your application's HTTP requests on `localhost` and forwards them to a real external API on your behalf. This architecture enables local inspection, mocking, and chaos injection without modifying remote backends or installing system certificates.  
*See full reference:* [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)

### Route / Route Mapping
A configuration rule connecting a local URL path prefix (such as `/github`) to a real destination API's base URL (such as `https://api.github.com`). Routes define the forwarding targets for the reverse proxy engine.  
*See full reference:* [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)

### Status Code Override
A chaos engine failure mode that forces a request to fail with a specific HTTP error code (`500`, `503`, or `504`) directly from EntropyLab without contacting the upstream API. Used to test application error banners, fallback screens, and retry logic.  
*See full reference:* [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md)

### Sub-Path Filter
An optional path constraint applied to a route's chaos rule that restricts Latency Injection, Status Code Overrides, and Connection Resets to only match sub-paths underneath that route (e.g., scoping chaos only to `/checkout`). All other sub-paths under that route forward normally.  
*See full reference:* [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)

### Target Base URL
The upstream destination protocol and hostname (such as `https://api.github.com` or `https://api.stripe.com`) where EntropyLab forwards traffic matching a route's Local Path. Must start with `http://` or `https://`.  
*See full reference:* [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)

### WAL Mode (Write-Ahead Logging)
The high-concurrency journaling mode utilized by EntropyLab's embedded SQLite database (`entropylab.db`). WAL mode allows background worker threads to write traffic logs concurrently at high speeds without locking the database or stalling user interface read operations.  
*See full reference:* [Theme & Data Storage: The SQLite Database](../05-settings-and-data/01-theme-and-data-storage.md#2-entropylabdb-traffic-history-database)

---

## Related Reading

- [Welcome to EntropyLab](../00-orientation/01-welcome.md)
- [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)
- [Complete UI Field Reference](./02-complete-ui-field-reference.md)
- [Limitations & FAQ](../07-troubleshooting/02-limitations-and-faq.md)
