# Complete UI Field Reference
*For: expert engineers requiring an immediate, non-narrative lookup of every UI control, field, valid range, and default value.*

This reference provides exhaustive tabular specifications for every interactive control, input field, and display element across all EntropyLab tabs and dialogs.

---

## 1. Global Shell & Navigation Bar

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Theme Toggle** | Button / Icon | `LIGHT`, `DARK` | `LIGHT` | Toggles interface theme between Light and Dark modes; persists across restarts in `config.json`. | [Theme & Data Storage](../05-settings-and-data/01-theme-and-data-storage.md#the-interface-theme--dark-mode) |
| **Navigation Tabs** | Tab Selector | `Proxy Control`, `Routes`, `Chaos Rules`, `Inspector`, `Mocks`, `Analytics`, `About` | `Proxy Control` | Switches the primary active operational view. | [Orientation Overview](../00-orientation/01-welcome.md#core-capabilities-at-a-glance) |

---

## 2. Proxy Control Tab

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Port** | Text Field | Integer: `1` to `65535` | `8080` | Local TCP port where reverse proxy listens; locked and disabled while proxy is running. | [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md#field-by-field-reference) |
| **Start Proxy / Stop Proxy** | Button | Action trigger | `"Start Proxy"` | Starts or stops the embedded reverse proxy server socket. | [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md#field-by-field-reference) |
| **Status Label** | Display Label | `"Stopped"` (red `#FE0134`), `"Running on port <port>"` (green `#2e7d32`) | `"Stopped"` | Live visual indicator of proxy server socket state. | [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md#field-by-field-reference) |

---

## 3. Routes Tab

### Toolbar & Table Controls

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Add Route** | Button | Action trigger | Active | Opens the Add Route modal dialog to register a new route mapping. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#1-adding-a-route) |
| **Edit Route** | Button | Action trigger | Disabled (requires row selection) | Opens the Edit Route dialog for the currently selected table row. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#2-editing-an-existing-route) |
| **Delete Route** | Button | Action trigger | Disabled (requires row selection) | Prompts for confirmation and permanently removes the selected route mapping. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#3-deleting-a-route) |
| **Local Path** | Table Column | Valid URL path string starting with `/` | None *(mandatory)* | The local URL prefix matched using longest-prefix comparison. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#field-by-field-reference) |
| **Target Base URL** | Table Column | Valid URL string starting with `http://` or `https://` | None *(mandatory)* | Upstream destination URL where matching requests are relayed. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#field-by-field-reference) |
| **Enabled** | Checkbox Table Column | `true` (checked), `false` (unchecked) | `true` | Real-time toggle; if disabled, incoming requests return HTTP 404. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#4-toggling-route-status-enable--disable) |

### Add / Edit Route Dialog

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Local Path** | Text Field | Non-empty string starting with `/` (must be unique across all routes) | Blank (Add) / Current (Edit) | Defines the local prefix EntropyLab listens for on `localhost:<port>`. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#field-by-field-reference) |
| **Target Base URL** | Text Field | Valid URL beginning with `http://` or `https://` | Blank (Add) / Current (Edit) | Defines the external destination URL where traffic is forwarded. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#field-by-field-reference) |
| **Add / Save** | Button | Action trigger | Active | Validates input, saves route to `config.json`, and refreshes table. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#1-adding-a-route) |
| **Cancel** | Button | Action trigger | Active | Closes modal without saving changes. | [Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md#1-adding-a-route) |

---

## 4. Chaos Rules Tab

### Toolbar & Table Controls

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Edit Chaos Rule** | Button | Action trigger | Disabled (requires row selection) | Opens the multi-section chaos configuration dialog for the selected route. | [What Is Chaos Engineering?](../02-chaos-engine/01-what-is-chaos-engineering.md) |
| **Local Path** | Table Column | String | None | Read-only display of the route's local path. | [What Is Chaos Engineering?](../02-chaos-engine/01-what-is-chaos-engineering.md) |
| **Chaos Summary** | Table Column | Text summary string | `"Latency: OFF \| Status: OFF \| Reset: OFF"` | Live human-readable summary of active chaos parameters on that route. | [What Is Chaos Engineering?](../02-chaos-engine/01-what-is-chaos-engineering.md) |

### Edit Chaos Rule Dialog

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Enable Latency** | Checkbox | `true` (checked), `false` (unchecked) | `false` | Activates artificial delay injection for matching requests. | [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md#field-by-field-reference) |
| **Latency (ms)** | Spinner / Number | Integer: `0` to `60000` (step: `100`) | `0` | Delay duration in milliseconds applied before returning or forwarding. | [Latency Injection Reference](../02-chaos-engine/02-latency-injection.md#field-by-field-reference) |
| **Enable Status Override** | Checkbox | `true` (checked), `false` (unchecked) | `false` | Activates forced HTTP status error injection. | [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md#field-by-field-reference) |
| **Status Code** | Dropdown Selector | `500`, `503`, `504` | `500` | The simulated HTTP error code returned when override triggers. | [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md#field-by-field-reference) |
| **Failure %** | Spinner / Number | Integer: `0` to `100` (step: `5`) | `0` | Independent probability per request that Status Override triggers. | [Status Code Override Reference](../02-chaos-engine/03-status-code-override.md#field-by-field-reference) |
| **Enable Connection Reset** | Checkbox | `true` (checked), `false` (unchecked) | `false` | Activates abrupt TCP connection severance. | [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md#field-by-field-reference) |
| **Reset %** | Spinner / Number | Integer: `0` to `100` (step: `5`) | `0` | Independent probability per request that TCP socket is severed. | [Connection Reset Reference](../02-chaos-engine/04-connection-reset.md#field-by-field-reference) |
| **Enable Mutation** | Checkbox | `true` (checked), `false` (unchecked) | `false` | Activates random ASCII character corruption on successfully forwarded response bodies. | [Payload Mutation Reference](../02-chaos-engine/06-payload-mutation.md#field-by-field-reference) |
| **Mutation Intensity** | Spinner / Number | Integer: `1` to `1000` (step: `1`) | `5` | Exact number of random character substitutions applied per response when mutation is enabled. | [Payload Mutation Reference](../02-chaos-engine/06-payload-mutation.md#field-by-field-reference) |
| **Sub-path filter (optional)** | Text Field | Path string (e.g., `/checkout`, `/users`) | Blank | Scopes all active chaos rules on this route strictly to this sub-path. | [Sub-Path Filtering Reference](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md#field-by-field-reference) |
| **OK** | Button | Action trigger | Active | Validates and commits all chaos settings for the route to `config.json`. | [Sub-Path Filtering Reference](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md) |
| **Cancel** | Button | Action trigger | Active | Discards changes and closes dialog. | [Sub-Path Filtering Reference](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md) |

---

## 5. Inspector Tab

### Toolbar & Traffic Table Controls

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Save as Mock** | Button | Action trigger | Disabled (enabled on valid response body) | Captures selected row's response and creates an active mock in Mocks tab. | [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md#the-auto-mock-snapshot-turning-live-responses-into-permanent-mocks) |
| **Time** | Table Column | Time string (`HH:mm:ss`) | Live system time | Wall-clock timestamp when the request arrived at EntropyLab. | [Reading Traffic History](../03-inspector/01-reading-and-using-traffic-history.md#column-reference) |
| **Method** | Table Column | HTTP verb: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`, `HEAD` | Dynamic | The HTTP method transmitted by the client. | [Reading Traffic History](../03-inspector/01-reading-and-using-traffic-history.md#column-reference) |
| **Path** | Table Column | URL path string | Dynamic | The incoming path requested by the client on `localhost:<port>`. | [Reading Traffic History](../03-inspector/01-reading-and-using-traffic-history.md#column-reference) |
| **Status** | Table Column | Numeric HTTP code (`200`, `404`, `500`, etc.) or `"N/A (Reset)"` | Dynamic | HTTP status code returned to client, or N/A if connection reset severed socket. | [Reading Traffic History](../03-inspector/01-reading-and-using-traffic-history.md#column-reference) |
| **Duration (ms)** | Table Column | Non-negative integer (milliseconds) | Dynamic | Total round-trip execution time from socket receipt to completion. | [Reading Traffic History](../03-inspector/01-reading-and-using-traffic-history.md#column-reference) |
| **Type** | Table Column | `FORWARDED`, `MOCKED`, `CHAOS_STATUS`, `CHAOS_RESET` | Dynamic | Processing classification describing how EntropyLab handled the request. | [Reading Traffic History](../03-inspector/01-reading-and-using-traffic-history.md#request-types-at-a-glance) |

### Request Details Modal (Double-Click Row)

| Name | Type | Valid Values / Content | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Request Headers** | Text Area | Read-only formatted text | Dynamic | Displays incoming client headers formatted line-by-line. | [Inspecting Details](../03-inspector/02-inspecting-details-and-auto-mock.md#the-request-details-view-at-a-glance) |
| **Response Headers** | Text Area | Read-only formatted text | Dynamic | Displays outgoing response headers returned to client. | [Inspecting Details](../03-inspector/02-inspecting-details-and-auto-mock.md#the-request-details-view-at-a-glance) |
| **Request Body** | Text Area | Read-only text / pretty-printed JSON | Dynamic | Displays payload sent by client (auto-formatted with Consolas monospace). | [Inspecting Details](../03-inspector/02-inspecting-details-and-auto-mock.md#the-request-details-view-at-a-glance) |
| **Response Body** | Text Area | Read-only text / pretty-printed JSON | Dynamic | Displays payload returned by server (auto-formatted with Consolas monospace). | [Inspecting Details](../03-inspector/02-inspecting-details-and-auto-mock.md#the-request-details-view-at-a-glance) |
| **Close** | Button | Action trigger | Active | Closes detail inspection dialog. | [Inspecting Details](../03-inspector/02-inspecting-details-and-auto-mock.md) |

---

## 6. Mocks Tab

### Toolbar & Table Controls

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Add Mock** | Button | Action trigger | Active | Opens the Add Mock modal dialog to register a manual mock. | [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md#step-2-register-the-mock-in-entropylab) |
| **Local Path** | Table Column | Valid URL path string starting with `/` | None *(mandatory)* | Exact path that triggers this mock (exact match only; no prefix matching). | [Managing Mocks](../04-mocking/02-managing-mocks-and-auto-mock-details.md#the-columns-explained) |
| **File Path** | Table Column | Absolute filesystem path string | None *(mandatory)* | Absolute disk path to the source `.json` payload file. | [Managing Mocks](../04-mocking/02-managing-mocks-and-auto-mock-details.md#the-columns-explained) |
| **Auto-Generated** | Table Column | `"Yes"`, `"No"` | `"No"` (Manual) / `"Yes"` (Snapshot) | Read-only indicator showing whether mock was created via Auto-Mock Snapshot. | [Managing Mocks](../04-mocking/02-managing-mocks-and-auto-mock-details.md#the-columns-explained) |
| **Enabled** | Checkbox Table Column | `true` (checked), `false` (unchecked) | `true` | Real-time toggle; if disabled, requests fall through to route matching. | [Managing Mocks](../04-mocking/02-managing-mocks-and-auto-mock-details.md#1-toggling-mock-activation-enable--disable) |

### Add Mock Dialog

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Local Path** | Text Field | Non-empty string starting with `/` (must be unique across all mocks) | Blank | Exact path on `localhost:<port>` that triggers this mock. | [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md#field-by-field-reference) |
| **Choose File...** | Button | Windows FileChooser | "No file selected" | File browser restricted to `.json` files to select source payload. | [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md#field-by-field-reference) |
| **Selected File Label**| Display Label | Absolute filesystem path | "No file selected" | Displays current file selection from FileChooser. | [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md#field-by-field-reference) |
| **Add** | Button | Action trigger | Active | Validates inputs, saves mock to `config.json`, and adds row to table. | [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md#field-by-field-reference) |
| **Cancel** | Button | Action trigger | Active | Closes modal without saving changes. | [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md) |

---

## 7. Analytics Tab

### Analytics Tab

| Name | Type | Valid Values / Range | Default Value | One-Line Description | Reference Link |
|---|---|---|---|---|---|
| **Refresh** | Button | Action trigger | Active | Manually re-queries SQLite database and recalculates all metrics and chart buckets. | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#analytics-overview) |
| **Total Requests** | Read-only Display Metric (KPI Card) | Non-negative integer (e.g., `1,248`) | `"0"` | Total volume of requests logged across the entire database history. | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#metric-by-metric-reference) |
| **Error Rate %** | Read-only Display Metric (KPI Card) | Percentage: `0.0%` to `100.0%` | `"0.0%"` | Ratio of failed requests (status >= 400 or chaos) to total requests; highlighted in red (`#FE0134`) if > 0. | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#metric-by-metric-reference) |
| **Average Duration** | Read-only Display Metric (KPI Card) | Millisecond decimal string (e.g., `14.2 ms`) | `"0 ms"` | Arithmetic mean latency computed across all logged requests in the database. | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#metric-by-metric-reference) |
| **p50 Duration** | Read-only Display Metric (KPI Card) | Integer milliseconds (e.g., `8 ms`) | `"0 ms"` | 50th percentile (median) duration; 50% of requests completed faster than this time. | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#metric-by-metric-reference) |
| **p95 Duration** | Read-only Display Metric (KPI Card) | Integer milliseconds (e.g., `45 ms`) | `"0 ms"` | 95th percentile duration; 95% of requests completed faster than this time (tail latency). | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#metric-by-metric-reference) |
| **Response Status & Traffic Distribution Chart** | BarChart (JavaFX) | X-axis: CategoryAxis (`2xx Success`, `4xx Client Error`, `5xx Server Error`, `CHAOS_STATUS`, `CHAOS_RESET`, `MOCKED`); Y-axis: NumberAxis (`Request Count`) | Empty series | Bar chart breaking down traffic volume by HTTP status classes and chaos failure modes. | [Analytics Dashboard](../03-inspector/03-analytics-dashboard.md#response-status--traffic-distribution-chart) |

---

## Related Reading

- [Glossary of Terms](./01-glossary.md)
- [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md)
- [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
