# Proxy Control: Starting, Stopping, and Ports
*For: all developers managing the local proxy lifecycle and network port configurations.*

## Overview

The **Proxy Control** tab is the operational command center for EntropyLab's embedded reverse proxy engine. From this tab, you configure which local TCP port EntropyLab binds to, monitor its running state, and start or stop the server.

> **Tip:** Modifying routes, changing chaos injection rules, or adding mock responses takes effect immediately in real time—**no proxy restart is required**. The only time you need to stop and restart the proxy is when changing the listening port number.

## The Proxy Control Tab at a Glance

The **Proxy Control** interface is streamlined and focused on server lifecycle management:

![Screenshot: Proxy Control tab showing the Port field disabled and set to 8080, the primary action button displaying Stop Proxy, and the status label showing Running on port 8080 in bold green text](./images/proxy-control-running.png)

The tab provides three essential controls:
1. **Port Field**: A text input specifying the local TCP port where EntropyLab listens for incoming traffic.
2. **Start Proxy / Stop Proxy Button**: A single toggle button that starts the server or shuts it down cleanly.
3. **Status Label**: A live status indicator reporting whether the proxy is currently **Stopped** (in bold red `#FE0134`) or **Running on port <X>** (in bold green `#2e7d32`).

## Step-by-Step: Managing Proxy Execution & Ports

### 1. Starting the Proxy

1. Open EntropyLab and click the **Proxy Control** tab in the top navigation bar.
2. Verify the number in the **Port** field (the default is `8080`).
3. Click the **Start Proxy** button.
4. Verify the UI updates:
   - The button label changes to **Stop Proxy**.
   - The **Port** field becomes disabled (grayed out) to prevent modification while the socket is active.
   - The status text updates to bold green: `Running on port 8080`.

### 2. Stopping the Proxy

1. Navigate to the **Proxy Control** tab.
2. Click the **Stop Proxy** button.
3. Verify the UI updates:
   - The button label changes back to **Start Proxy**.
   - The **Port** field is re-enabled for editing.
   - The status text updates to bold red: `Stopped`.

### 3. Changing to a Custom Port

Because the **Port** field is locked while the server is active, you must follow this exact sequence to change ports:

1. If the proxy is currently running, click **Stop Proxy**.
2. Click into the now-active **Port** field.
3. Delete the existing port number and enter your desired custom port (for example, `8085` or `9000`).
4. Click **Start Proxy**.
5. EntropyLab validates the number, saves the new port to your persistent configuration (`config.json`), and starts listening on the new port. The status label will reflect your new port (e.g., `Running on port 8085`).

## Field-by-Field Reference

| Control / Field | What it does | Valid values/range | Default |
|---|---|---|---|
| **Port** | Specifies the local TCP port number where EntropyLab's reverse proxy accepts HTTP connections (`http://localhost:<port>`). Locked and disabled while the proxy is running. | Integer between `1` and `65535`. Ports above `1024` are recommended to avoid system permission restrictions. | `8080` |
| **Start Proxy / Stop Proxy** | Toggles the internal reverse proxy server on or off. Validates the port input and initiates network socket binding. | Clickable action button. | "Start Proxy" (when stopped) |
| **Status** | Real-time visual feedback indicating the current socket state. | Displays either **Stopped** (red text `#FE0134`) or **Running on port <port>** (green text `#2e7d32`). | "Stopped" |

## Common Mistakes & Troubleshooting

### Invalid Port Number

- **Symptoms**: Clicking **Start Proxy** opens an error dialog titled **Invalid Port** with the message:
  ```text
  Invalid port number
  ```
- **Causes**: 
  - The **Port** field is blank.
  - The value contains letters, symbols, or negative numbers (e.g., `8080a` or `-1`).
  - The value is outside the valid TCP range (`1`–`65535`), such as `70000`.
- **How to Fix**: Enter a whole number between `1` and `65535` (standard unreserved developer ports like `8080`, `8085`, `8888`, or `9090` work best) and click **Start Proxy** again.

### Port Already in Use (`Address already in use: bind`)

- **Symptoms**: Clicking **Start Proxy** opens an error dialog titled **Proxy Error** with the message:
  ```text
  Address already in use: bind
  ```
  The status indicator remains red (**Stopped**).
- **Causes**:
  1. Another developer application running on your computer is already listening on that port (port `8080` is commonly used by Tomcat, Spring Boot, Jenkins, Docker, or Node.js dev servers).
  2. A previous or second instance of EntropyLab is already running in the background.
- **How to Fix (Option A — Recommended)**:
  Pick a different port. Change the value in the **Port** field from `8080` to another port—such as `8085`, `8090`, or `9000`—and click **Start Proxy**. Update the base URL in your API client or frontend code to match your new port (e.g., `http://localhost:8085`).
- **How to Fix (Option B — Free the Port)**:
  If you must use port `8080`, identify which application is occupying it:
  1. Open Windows PowerShell and run:
     ```powershell
     Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess
     ```
  2. Close the conflicting application or stop the corresponding process in Task Manager, then click **Start Proxy** in EntropyLab.

## Related Reading

- [Understanding and Managing Routes](./01-understanding-and-managing-routes.md)
- [Quick Start Tutorial: Your First Route & Inspected Request](../00-orientation/04-quick-start-tutorial.md)
- [Settings & Data Storage: Where Config Is Saved](../05-settings-and-data/01-theme-and-data-storage.md)
- [Common Errors and Fixes](../07-troubleshooting/01-common-errors-and-fixes.md)
