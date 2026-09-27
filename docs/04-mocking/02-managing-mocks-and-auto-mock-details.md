# Managing Mocks and Auto-Mock Details
*For: all developers organizing mock datasets, managing mock lifecycles, and resolving routing precedence.*

## Overview

The **Mocks** tab provides a centralized inventory of all mock responses registered in EntropyLab. Mocks can originate from two sources: hand-crafted JSON files you created yourself (Manual Mocks) or live snapshots captured with a single click from the Traffic Inspector (Auto-Mocks).

Understanding how mocks interact with the rest of EntropyLab—specifically how they take precedence over routes and chaos rules—ensures that your test environments behave deterministically.

> **Note:** For instructions on writing and loading your first manual `.json` mock, see [Manual Static Mocking](./01-manual-static-mocking.md). For capturing live snapshots, see [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md).

## The Mocks Tab at a Glance

The **Mocks** table summarizes all local response stubs currently configured in the application:

![Screenshot: Mocks tab showing two entries: a manually added mock for /api/users with Auto-Generated No, and an auto-generated mock for /github/users/octocat with Auto-Generated Yes, with Enabled checkboxes checked](./images/mocks-management-table.png)

### The Columns Explained

- **Local Path**: The exact URL path that EntropyLab intercepts.
- **File Path**: The absolute path to the `.json` file on disk whose contents are served.
- **Auto-Generated**: A transparency indicator displaying **Yes** or **No**:
  - **`Yes`**: Created automatically via the Inspector's **Save as Mock** button. The file is stored in `%APPDATA%\EntropyLab\mocks\`.
  - **`No`**: Created manually by you via the **Add Mock** dialog pointing to a file on your local filesystem.
- **Enabled**: An interactive checkbox enabling or disabling the mock in real time.

## Step-by-Step: Managing Mock States

### 1. Toggling Mock Activation (Enable / Disable)

You can activate or deactivate any mock instantly without restarting the proxy:

1. Click the **Mocks** tab in the top navigation bar.
2. Locate the mock row you wish to modify.
3. Click the checkbox in the **Enabled** column.
4. **What happens to traffic when disabled?**
   When a mock is unchecked, EntropyLab ignores it completely. Requests to that path **fall through** to your normal reverse proxy routing and chaos rules:
   - If a matching route exists in the **Routes** tab, the request is evaluated against any active chaos rules and forwarded to the real upstream API.
   - If no matching route exists, the request returns HTTP `404 Not Found` (`No route configured for this path`).
5. Check the box again at any time to resume serving the local mock immediately.

### 2. Editing a Mock

You can modify an existing mock's intercepted path or source JSON file at any time without recreating it:

1. Click the **Mocks** tab in the top navigation bar.
2. Click the table row of the mock you wish to edit to select it (or double-click the row directly, or right-click and select **Edit**).
3. Click the **Edit Mock** button in the toolbar.
4. The **Edit Mock** dialog opens pre-filled with the mock's current **Local Path** and **File Path**.
5. Update either or both fields:
   - **Local Path**: Edit the URL path string (must begin with `/`).
   - **Mock File**: Click **Choose File...** to browse your filesystem and select a new `.json` file.
6. Click **Save** to commit the changes immediately.

**Duplicate-Path Validation During Edits:**  
EntropyLab's path validator checks for uniqueness across all configured mocks, but **explicitly excludes the mock currently being edited**. This means keeping the same Local Path while changing only the target File Path is fully valid and will not trigger a duplicate-path error.

> 📝 **Note on Auto-Generated Status:** Editing the Local Path or File Path of an Auto-Generated mock preserves its origin. The **Auto-Generated** column remains **`Yes`**, tracking that the mock originally came from an Inspector traffic snapshot.

### 3. Deleting a Mock

To permanently remove a mock configuration from EntropyLab:

1. Click the **Mocks** tab in the top navigation bar.
2. Click the table row of the mock you wish to remove (or right-click the row and choose **Delete**).
3. Click the red **Delete Mock** button in the toolbar.
4. A themed confirmation dialog appears:
   ```text
   Confirm Deletion
   Delete mock for '<localPath>'?
   ```
5. Click **Yes** to confirm. The mapping is removed from the table and configuration immediately.

> 📝 **Note on Physical Files:** Deleting a mock in EntropyLab removes only the routing mapping from the application's configuration. The physical `.json` file on disk (whether located in `%APPDATA%\EntropyLab\mocks\` or a custom workspace directory) is **not** deleted. This ensures you never lose captured response data by accident. If you wish to delete the file permanently, delete it manually via Windows File Explorer.

---

## Auto-Mock Snapshot Mechanics: Overwrite Behavior

When you take an Auto-Mock Snapshot in the **Inspector** tab, EntropyLab saves the response payload to a file named after the sanitized path (for example, `/github/users/octocat` is saved to `%APPDATA%\EntropyLab\mocks\-github-users-octocat.json`).

### What Happens if You Auto-Mock the Same Path Twice?

If you capture a snapshot for a path that already has an auto-generated mock, **the existing file is overwritten in place**. 

- The existing `.json` file in `%APPDATA%\EntropyLab\mocks\` is truncated and updated with the newest response data.
- The existing entry in the **Mocks** table is updated to ensure it is marked **Enabled: Yes** and points to that file.
- **No duplicate files pile up on disk**, and no duplicate rows appear in the Mocks table.

> **Tip:** In-place overwriting is an intentional design choice. It allows you to quickly refresh stale test snapshots simply by making a fresh live API call and clicking **Save as Mock** again.

---

## Mock Priority & Precedence: Who Wins?

One of the most important rules in EntropyLab's request lifecycle is:

> **CRITICAL RULE:** An **ENABLED mock ALWAYS wins** over any Route or Chaos Rule for that same exact path.

Because mocks are evaluated at Step 1 of the request pipeline, matching mocks short-circuit all subsequent reverse proxy operations.

```text
Incoming Request
       │
       ▼
[ Mock Check ] ──────(Enabled Mock Found?)──────► Return Local JSON (INSTANT 200 OK)
       │                                          (Route & Chaos NEVER run)
       │ No match or Mock is Disabled
       ▼
[ Route Match ] ────────────────────────────────► Apply Chaos Rules & Forward to Real API
```

### Worked Example: Mock vs. Route + Chaos

Suppose you are developing an e-commerce checkout integration and have configured:
- **Route**: Local Path `/stripe`, Target Base URL `https://api.stripe.com`.
- **Chaos Rule on `/stripe`**: Latency = `3000 ms`, Status Override = `500` (100% failure).
- **Mock**: Local Path `/stripe/plans`, File = `plans.json`, **Enabled: Checked**.

Here is how incoming requests behave:

| Request Path | Mock Check | Route & Chaos Check | Final Outcome |
|---|---|---|---|
| `http://localhost:8080/stripe/plans` | **Exact match found!** | Bypassed completely. | **Instant 200 OK** in 4 ms with `plans.json` data. Zero latency, zero 500 errors. |
| `http://localhost:8080/stripe/checkout` | No match. | Matched route `/stripe`. Chaos triggered! | **3-second delay, then 500 error**. |
| `http://localhost:8080/stripe/plans` *(with Mock Disabled)* | Mock is disabled; skipped. | Matched route `/stripe`. Chaos triggered! | **3-second delay, then 500 error**. Traffic falls through! |

*Key Takeaway:* You can safely apply aggressive chaos to an entire service route (like `/stripe`) while keeping individual critical endpoints (like `/stripe/plans`) pinned to stable mocks.

---

## Field-by-Field Reference

| Column / Field | What it represents | Valid States / Values |
|---|---|---|
| **Local Path** | The exact path that triggers this mock. | Unique URL path starting with `/`. |
| **File Path** | The full disk path to the source JSON file. | Absolute path on local filesystem. |
| **Auto-Generated** | Shows whether the mock was produced by Auto-Mock Snapshot or manual import. | **`Yes`** (created via Inspector) or **`No`** (created via Add Mock dialog). |
| **Enabled** | Live toggle controlling whether the mock intercepts requests. | Checked (`true`) or Unchecked (`false`). |

## Common Mistakes & Troubleshooting

- **Why Did Chaos Not Trigger on My Mock?**:
  Chaos rules (latency, status overrides, and connection resets) only apply to traffic that flows through **Routes**. Because enabled mocks are served before routes are evaluated, chaos rules never execute for active mocks. To test chaos on that path, temporarily uncheck the mock's **Enabled** checkbox.
- **Forgetting Exact Path Boundaries**:
  Remember that mocks match exact paths. A mock for `/stripe/plans` will not match `/stripe/plans/` (trailing slash) or `/stripe/plans?currency=usd` (if query parameters alter the local path matching).
- **Manually Editing Auto-Generated Files**:
  You are free to open any file in `%APPDATA%\EntropyLab\mocks\` in your code editor and edit the JSON by hand. EntropyLab reads the file freshly from disk on each request, so your edits take effect immediately without restarting the app.

## Related Reading

- [Manual Static Mocking](./01-manual-static-mocking.md)
- [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)
- [Understanding and Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
- [Sub-Path Filtering & Combining Chaos Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
