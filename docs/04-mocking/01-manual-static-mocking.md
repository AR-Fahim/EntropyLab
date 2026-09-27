# Manual Static Mocking
*For: all developers, especially frontend engineers building against unready or unreleased backend endpoints.*

## Overview

Modern software development often requires frontend developers to build and test user interfaces before the corresponding backend API endpoints are written. In other cases, testing requires fixed, 100% predictable data that never changes due to third-party database updates.

**Manual Static Mocking** solves both challenges. By linking a local URL path directly to a `.json` file stored on your computer, EntropyLab immediately intercepts requests to that path and serves your file's contents with an HTTP `200 OK` status and `Content-Type: application/json`—without needing a running backend server or an active internet connection.

> **Note:** For auto-generating mocks from live API responses, see [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md) and [Managing Mocks and Auto-Mock Details](./02-managing-mocks-and-auto-mock-details.md).

## The Mocks Tab at a Glance

Mock endpoints are configured and maintained in the **Mocks** tab:

![Screenshot: Add Mock dialog showing the Local Path input set to /api/users, with the Choose File button displaying the path to users-mock.json](./images/mocks-add-mock-dialog.png)

The interface includes:
- **Toolbar**: Houses the **Add Mock** button.
- **Mocks Table**: A four-column grid showing **Local Path**, **File Path**, **Auto-Generated** (`Yes` or `No`), and **Enabled** (live checkbox).
- **Add Mock Dialog**: Contains a path input, a file chooser for `.json` files, and an **Add** button.

## Step-by-Step: Creating a Manual Static Mock

### Step 1: Create a Local JSON Payload File

Using any text editor (such as VS Code or Notepad), create a new file named `users-mock.json` anywhere on your computer (for example, on your Desktop or in your project folder). 

Paste the following mock JSON payload into the file and save it:

```json
{
  "status": "success",
  "users": [
    {
      "id": 101,
      "name": "Sarah Connor",
      "role": "Lead Architect",
      "active": true
    },
    {
      "id": 102,
      "name": "Miles Dyson",
      "role": "Cybernetics Specialist",
      "active": false
    }
  ]
}
```

### Step 2: Register the Mock in EntropyLab

1. Open EntropyLab and click the **Mocks** tab in the top navigation bar.
2. Click the **Add Mock** button in the toolbar.
3. In the **Local Path** field, type the endpoint you want your frontend code to call:
   ```text
   /api/users
   ```
4. Click the **Choose File...** button.
5. In the file picker, select the `users-mock.json` file you created in Step 1.
6. Verify that the file path appears next to the button.
7. Click **Add**.

A new row appears in your **Mocks** table with:
- **Local Path**: `/api/users`
- **File Path**: Full absolute path to your file
- **Auto-Generated**: `No`
- **Enabled**: Checked (`true`)

### Step 3: Test and Verify the Endpoint

1. Verify that your proxy is running in the **Proxy Control** tab (`Running on port 8080`).
2. Open your web browser or API client and visit:
   ```text
   http://localhost:8080/api/users
   ```
3. You will immediately receive your exact JSON payload.
4. Switch to the **Inspector** tab:
   - The request appears with Status **`200`**.
   - The **Type** column displays **`MOCKED`**.
   - The **Duration (ms)** column displays single-digit milliseconds (e.g., `4 ms`).

### Editing a Mock

You can also edit an existing mock's path or file at any time — see the full Edit Mock walkthrough in [Managing Mocks and Auto-Mock Details](./02-managing-mocks-and-auto-mock-details.md).

### Deleting a Mock

You can also delete an existing mock at any time — see the full Delete Mock walkthrough in [Managing Mocks and Auto-Mock Details](./02-managing-mocks-and-auto-mock-details.md).

---

## Routes vs. Mocks: Matching Behavior Comparison

The single most critical concept to understand when configuring mocks is that **Mocks use exact path matching, whereas Routes use prefix matching**:

| Feature / Behavior | Routes (Reverse Proxy) | Mocks (Static Mocking) |
|---|---|---|
| **Matching Algorithm** | **Longest-Prefix Match** | **Exact Path Match Only** |
| **Trailing Sub-Paths** | Forwards remainder path to destination (e.g., `/github/users` forwards to upstream `/users`). | **Rejected**. `/api/users` will NOT match `/api/users/101`. |
| **External Network** | Connects to the real upstream internet server. | Zero external network calls; reads from local disk. |
| **Execution Precedence** | Evaluated **second** (after Mocks). | Evaluated **first** (bypasses all routes and chaos). |

> **For Experts:** In EntropyLab's request pipeline, the mock evaluation engine executes before route matching. If an incoming request path matches an enabled mock's `localPath` exactly, EntropyLab delivers the local file payload immediately. No route matching occurs, no upstream connection is established, and no chaos rules are evaluated.

---

## Field-by-Field Reference

| Field / Control | What it does | Valid values / format | Default |
|---|---|---|---|
| **Local Path** | The exact local URL path on `localhost:<port>` that triggers this mock. | Must be non-empty and start with `/` (automatically normalized). Must be unique across all mocks. | None *(required)* |
| **Mock File (File Path)** | The absolute path on your local filesystem pointing to the `.json` file to serve. Selected via the **Choose File...** button. | Valid, accessible `.json` file on disk. | "No file selected" *(required)* |
| **Enabled** | Checkbox controlling whether the mock is actively intercepting traffic. If unchecked, the path falls through to route evaluation. | Checked (`true`) or Unchecked (`false`). | Checked (`true`) |
| **Auto-Generated** | Read-only indicator in the table displaying whether this mock was created manually or captured via Auto-Mock Snapshot. | `Yes` (from Inspector snapshot) or `No` (manually created). | `No` |

---

## Common Mistakes & Troubleshooting

- **"Validation Error: Local Path is required"**:
  Appears if the **Local Path** field is left blank when clicking **Add**.
- **"Validation Error: Please choose a .json file"**:
  Appears if you have not selected a valid `.json` file using the file chooser button.
- **"Duplicate Mock: A mock for this path already exists"**:
  EntropyLab requires unique local paths for mocks. If a mock already exists for `/api/users`, you cannot add a second mock for that exact path. Edit or delete the existing mock instead.
- **Calling a Sub-Path and Getting a 404**:
  Remember that mocks match **exact paths only**. If your mock is defined as `/api/users`, calling `/api/users/` (with a trailing slash) or `/api/users/1` will not match the mock and will fall through to route matching.
- **"Internal Server Error: Failed to read mock file" (Status 500)**:
  If a request returns a 500 error with this message, EntropyLab could not open the file specified in the **File Path** column. Check whether you moved, renamed, or deleted the `.json` file on your filesystem.
- **Trying to edit a mock's path to one that's already used by another mock**:
  Same duplicate-path protection as adding a new one applies here too.

## Related Reading

- [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)
- [Managing Mocks and Auto-Mock Details](./02-managing-mocks-and-auto-mock-details.md)
- [Understanding and Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
- [How-To Recipe: Unblock Frontend with Manual Mocks](../06-how-to-recipes/05-unblock-frontend-with-manual-mocks.md)
