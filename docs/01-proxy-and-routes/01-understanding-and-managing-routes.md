# Understanding and Managing Routes
*For: all developers configuring and debugging route mappings.*

## Overview

A **route** (or route mapping) is the fundamental rule connecting a local URL path on your computer to an external destination server. When your application sends an HTTP request to EntropyLab, the proxy examines the path, identifies the corresponding route, and forwards the request to the upstream target URL on your behalf.

> **Note:** For a conceptual overview of why EntropyLab uses a local reverse proxy architecture rather than global traffic interception, review [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md).

## The Routes Tab at a Glance

The **Routes** tab provides a complete management dashboard for defining, updating, and toggling your proxy endpoints.

![Screenshot: Routes tab showing a configured table with three active routes: /github mapped to https://api.github.com, /stripe mapped to https://api.stripe.com, and /weather mapped to https://api.weather.com, with toolbar buttons Add Route, Edit Route, and Delete Route](./images/routes-tab-overview.png)

The interface consists of:
1. **Toolbar**: Quick-action buttons (**Add Route**, **Edit Route**, **Delete Route**) positioned above the table.
2. **Routes Table**: A three-column grid listing your configured routes (**Local Path**, **Target Base URL**, **Enabled**).
3. **Context Menu**: Right-clicking any row exposes quick shortcuts for **Edit** and **Delete**.

## How Route Matching & Forwarding Works

### Longest-Prefix Matching in Plain English

When a request arrives at EntropyLab, the proxy checks the beginning of the URL path to decide which route should receive it. If multiple routes match the beginning of your path, EntropyLab always selects the most specific one—the route with the longest matching local path prefix.

Once matched, EntropyLab removes the matched local prefix, preserves the rest of the URL (the *remainder path*), preserves any URL query parameters, and appends them to the destination target URL.

### The Request Translation Lifecycle

Consider a configured route:
- **Local Path**: `/github`
- **Target Base URL**: `https://api.github.com`

When your application makes the following request:
```http
GET http://localhost:8080/github/users/octocat?tab=repos
```

EntropyLab performs the following translation:
1. **Match Local Prefix**: Identifies `/github` as the matching route prefix.
2. **Compute Remainder Path**: Strips `/github` from `/github/users/octocat`, leaving `/users/octocat`.
3. **Preserve Query Parameters**: Captures `?tab=repos`.
4. **Construct Upstream URI**: Combines `https://api.github.com` + `/users/octocat` + `?tab=repos`.
5. **Dispatch Upstream**:
   ```http
   GET https://api.github.com/users/octocat?tab=repos
   ```

> **For Experts:** Matching is evaluated against all enabled routes using boundary-safe prefix comparisons: `incomingPath.equals(normalizedLocal) || incomingPath.startsWith(normalizedLocal + "/")`. If multiple routes qualify, the route with the maximum character length (`normalizedLocal.length()`) wins. The remainder path is normalized to always begin with a leading `/`, and raw query strings (`exchange.getRequestURI().getRawQuery()`) are passed through unaltered without URL decoding/re-encoding distortions.

### Overlapping Routes: A Worked Example

Suppose you have two overlapping routes configured in your **Routes** table:

| Route | Local Path | Target Base URL |
|---|---|---|
| **Route A** | `/github` | `https://api.github.com` |
| **Route B** | `/github/api` | `https://mock-service.internal/v2` |

Here is how EntropyLab handles incoming requests:

- **Scenario 1: Calling a generic user endpoint**
  - Incoming request: `http://localhost:8080/github/users`
  - Candidates: Only **Route A** matches (`/github`).
  - Result: Forwarded to `https://api.github.com/users`.

- **Scenario 2: Calling a nested API endpoint**
  - Incoming request: `http://localhost:8080/github/api/search?q=chaos`
  - Candidates: Both **Route A** (`/github`, length 7) and **Route B** (`/github/api`, length 11) match the start of the path.
  - Winner: **Route B** wins because it is the longest-prefix match.
  - Result: The remainder path is `/search`, and the request is forwarded to `https://mock-service.internal/v2/search?q=chaos`.

## Step-by-Step: Managing Routes

### 1. Adding a Route

1. Click the **Routes** tab in the top navigation bar.
2. Click the **Add Route** button in the toolbar.
3. In the **Local Path** field, enter the path prefix you want to intercept locally (e.g., `/api/v1`).
4. In the **Target Base URL** field, enter the destination URL where traffic should go (e.g., `https://api.example.com`).
5. Click **Add**. The new route appears in the table with its **Enabled** checkbox selected by default.

### 2. Editing an Existing Route

1. In the **Routes** table, select the route you want to modify (or double-click the row).
2. Click the **Edit Route** button in the toolbar (or right-click the row and choose **Edit**).
3. Modify the **Local Path** or **Target Base URL** in the **Edit Route** dialog.
4. Click **Save**. Your changes take effect immediately without requiring a proxy restart.

### 3. Deleting a Route

1. In the **Routes** table, select the route you wish to delete.
2. Click the **Delete Route** button in the toolbar (or right-click and choose **Delete**).
3. A confirmation dialog appears asking: `Delete route '<path>'?`.
4. Click **OK** to permanently remove the route. Any active chaos rules associated with this route are automatically cleaned up.

### 4. Toggling Route Status (Enable / Disable)

You do not need to delete a route to stop proxying its traffic temporarily:
1. Locate the route in the **Routes** table.
2. Click the checkbox in the **Enabled** column.
3. When unchecked, EntropyLab immediately stops forwarding traffic for that route. Any request arriving on that path will return an HTTP `404 Not Found` response with the body `No route configured for this path`.
4. Check the box again at any time to re-enable forwarding instantly.

## Field-by-Field Reference

| Field | What it does | Valid values/range | Default |
|---|---|---|---|
| **Local Path** | The URL path prefix on `localhost:<port>` that triggers this route. EntropyLab intercepts all incoming paths matching this prefix. | Must be non-empty and start with `/`. Automatically normalizes leading slashes and strips trailing slashes. Must be unique. | None *(required)* |
| **Target Base URL** | The destination protocol, host, and optional base path where matching traffic is forwarded over the network. | Must be a valid URL starting with `http://` or `https://` (e.g., `https://api.github.com`). | None *(required)* |
| **Enabled** | Determines whether the route is currently active. If disabled, incoming traffic is rejected with a 404 response. | Checked (`true`) or Unchecked (`false`). | Checked (`true`) |

## Common Mistakes & Validation Messages

- **"Validation Error: Both fields are required"**: Appears if either the **Local Path** or **Target Base URL** field is left blank or contains only whitespace when clicking **Add** or **Save**.
- **"Duplicate Route: A route for this path already exists"**: EntropyLab enforces unique local paths. If you attempt to add or edit a route with a local path that matches an existing route (e.g., adding another `/github`), this warning prevents overwriting.
- **Forgetting to Enable a Route**: If your client receives an HTTP `404 Not Found` with the body `No route configured for this path`, check the **Enabled** column in the Routes tab. If the box is unchecked, the route is inactive.
- **Forgetting to Start the Proxy**: If your browser or HTTP client reports `ERR_CONNECTION_REFUSED` or `Failed to connect to localhost`, navigate to the **Proxy Control** tab and confirm that the status reads **Running on port 8080** in green. Routes cannot forward traffic if the proxy engine is stopped.

## Related Reading

- [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)
- [Proxy Control Reference](./02-proxy-control.md)
- [What Is Chaos Engineering?](../02-chaos-engine/01-what-is-chaos-engineering.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
- [Common Errors & Fixes](../07-troubleshooting/01-common-errors-and-fixes.md)
