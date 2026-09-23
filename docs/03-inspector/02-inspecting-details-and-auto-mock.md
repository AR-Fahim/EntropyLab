# Inspecting Details and Auto-Mock
*For: all developers inspecting HTTP payloads and creating instant local mocks.*

## Overview

While the main **Inspector** table gives an excellent high-level overview of traffic, diagnosing network bugs and evaluating payload structures requires deeper visibility. EntropyLab provides an in-depth **Request Details** modal for examining exact HTTP headers and bodies. 

Additionally, the Inspector features **Auto-Mock Snapshot**—a single-click workflow that captures any live server response and turns it into a permanent, reusable local mock endpoint.

> **Note:** For table column definitions and log retention details, review [Reading & Using Traffic History](./01-reading-and-using-traffic-history.md). For complete mock configuration and file management, see [Managing Mocks & Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md).

## The Request Details View at a Glance

Double-clicking any row in the Inspector opens the **Request Details** dialog:

![Screenshot: Request Details modal dialog showing the 4-panel grid with Request Headers, Response Headers, Request Body, and a pretty-printed JSON Response Body for /github/users/octocat](./images/inspector-request-details.png)

The dialog displays a balanced four-panel layout:
1. **Request Headers**: Metadata headers sent by your client application (e.g., `User-Agent`, `Accept`, custom auth tokens).
2. **Response Headers**: Metadata headers returned by the destination server (e.g., `Content-Type`, `Date`, caching policies).
3. **Request Body**: The raw payload submitted with the request (e.g., JSON sent with a `POST` or `PUT`).
4. **Response Body**: The response data returned to your client application.

### Automatic JSON Pretty-Printing & Plain Text Handling

Reading minified, single-line JSON responses is difficult. EntropyLab automatically parses JSON bodies in both request and response panes and formats them with clean syntax indentation in an easy-to-read monospace font (`Consolas`).

If the payload is not JSON (such as HTML error pages, plain text error messages, or XML), EntropyLab displays the content cleanly as raw plain text without throwing parser warnings or corrupting formatting.

## Step-by-Step: Inspecting Headers and Payloads

1. In the **Inspector** tab, locate the request you wish to examine.
2. Double-click the row (or select the row and press **Enter**).
3. The **Request Details** modal window will open.
4. Browse the four panels to verify headers, query parameters, or payload fields.
5. Click **Close** in the bottom-right corner to return to the traffic log.

---

## The Auto-Mock Snapshot: Turning Live Responses into Permanent Mocks

One of the most tedious tasks in software testing is manually creating mock data files. With **Auto-Mock Snapshot**, you can execute a real API request once, capture the server's authentic response, and instantly freeze it as a permanent local mock.

### When Is the "Save as Mock" Button Available?

The **Save as Mock** button is located in the toolbar directly above the Inspector table. To prevent invalid mock states, the button dynamically enables and disables:

- **Enabled**: When a row is selected that contains a valid response payload (such as `FORWARDED` or `CHAOS_STATUS` responses with body text).
- **Disabled (Grayed Out)**:
  - When no row is selected.
  - When the selected row has Type **`CHAOS_RESET`** (because connection resets sever the socket without generating a response body).
  - When the response body was completely empty (there is no meaningful payload to capture).

### Step-by-Step: Capturing an Auto-Mock Snapshot

1. In the **Inspector** tab, select a request row that received a successful response from a real API (Type = `FORWARDED`).
2. Click the **Save as Mock** button in the toolbar.
3. Behind the scenes, EntropyLab automatically:
   - Sanitizes the URL path into a safe filename (e.g., `/github/users/octocat` becomes `-github-users-octocat.json`).
   - Saves the full response body into your local mocks directory:
     ```text
     %APPDATA%\EntropyLab\mocks\<sanitized-path>.json
     ```
   - Creates a new entry in the **Mocks** tab mapped to that exact path, with **Auto-Generated** set to **Yes** and **Enabled** checked.
4. An information dialog appears confirming:
   ```text
   Mock saved and activated for <path>
   ```
5. Click **OK**. The mock is now active immediately.

### The Manual Mock Safety Check (Confirm Overwrite)

What happens if you have already hand-crafted a custom mock file for this path?

EntropyLab protects your work. If you click **Save as Mock** for a path that already has an active **manual mock** (a mock created via a custom file rather than auto-generation), EntropyLab will **not** silently overwrite it. Instead, a confirmation modal appears:

```text
Confirm Overwrite
This path already has a manual mock. Overwrite with the captured response?
[Yes] [No]
```

- Clicking **No** cancels the operation and keeps your hand-crafted file completely safe.
- Clicking **Yes** updates the mock mapping to point to the newly captured snapshot and flags it as auto-generated.

---

## Worked Example: Capture & Prove an Auto-Mock

Let's walk through an end-to-end scenario demonstrating how to capture and prove a mock in under 30 seconds:

### 1. Make the Initial Live Request
Ensure your route for `/github` is active, open your browser, and visit:
```text
http://localhost:8080/github/users/octocat
```
Your browser loads GitHub's live profile data. In the Inspector, this appears as a `FORWARDED` row with a duration of ~250 ms.

### 2. Take the Snapshot
1. Switch to EntropyLab and select that `FORWARDED` row in the **Inspector** tab.
2. Click **Save as Mock**.
3. Click **OK** on the confirmation prompt (`Mock saved and activated for /github/users/octocat`).

### 3. Prove It Worked
To prove that EntropyLab is now serving your saved snapshot without contacting the internet:
1. Switch back to your browser and refresh the page (`http://localhost:8080/github/users/octocat`).
2. Notice how fast it loads: the response appears **instantaneously** (typically in under `10 ms`).
3. Switch back to the **Inspector** tab in EntropyLab:
   - A new row has appeared at the top.
   - The **Type** column now displays **`MOCKED`**!
   - The **Duration (ms)** column displays single-digit milliseconds.
4. *(Optional Ultimate Test)*: Disconnect your computer from Wi-Fi or unplug your Ethernet cable. Refresh the browser page again. The page still loads the complete `octocat` user data perfectly, proving your frontend is fully isolated from network dependencies.

## Field-by-Field Reference

| Control / Dialog Element | Purpose | Valid States / Behavior |
|---|---|---|
| **Save as Mock Button** | Captures the selected row's response payload and registers it as an active mock. | Enabled when a valid response body exists; disabled for `CHAOS_RESET` or empty payloads. |
| **Request Headers Panel** | Displays metadata sent by client. | Read-only text area with monospace font. |
| **Response Headers Panel** | Displays metadata returned by server. | Read-only text area with monospace font. |
| **Request Body Panel** | Displays payload sent by client. | Auto-formats JSON; plain text fallback. |
| **Response Body Panel** | Displays payload received by client. | Auto-formats JSON; plain text fallback. |

## Common Mistakes & Troubleshooting

- **Why is the "Save as Mock" Button Disabled?**:
  Ensure you have clicked a table row to select it. If the row represents a `CHAOS_RESET`, the button remains disabled because no response payload exists to save.
- **Why Does My Sub-Path Still Contact the Internet?**:
  Remember that **Mocks use exact path matching**, unlike Routes which use prefix matching. If you saved a mock for `/github/users/octocat`, visiting `/github/users/octocat/repos` will not match the mock and will continue forwarding to GitHub.

## Related Reading

- [Reading & Using Traffic History](./01-reading-and-using-traffic-history.md)
- [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
- [How-To Recipe: Capture Real Data with Auto-Mock](../06-how-to-recipes/06-capture-real-data-with-auto-mock.md)
