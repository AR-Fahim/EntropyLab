# Theme & Data Storage
*For: intermediate and advanced developers managing app settings, local persistence, and backup workflows.*

## Overview

EntropyLab is an offline-first, zero-cloud desktop application. All user preferences, routing rules, chaos injection settings, historical traffic logs, and mock files are stored locally on your Windows computer in human-readable and standard database formats.

Understanding where this data lives makes it simple to inspect raw logs, backup your environment, or replicate your configurations across team machines.

> **Tip:** The entire `%APPDATA%\EntropyLab\` folder is completely self-contained. Copying this single folder to a new Windows machine (or preserving it in a backup script) restores your exact routes, chaos rules, mocks, and historical logs with zero setup.

## The Interface Theme & Dark Mode

EntropyLab features native support for both **Light Mode** and **Dark Mode**.

![Screenshot: EntropyLab running in Dark Mode, showing the top bar theme toggle icon, dark background styling, and contrasting brand red highlights](./images/settings-dark-mode.png)

### The Theme Toggle

- **Location**: Located in the upper-right corner of the application's permanent top navigation bar.
- **How It Works**: Clicking the theme icon instantly toggles the UI between clean Light Mode and eye-friendly Dark Mode.
- **Automatic Persistence**: There is no separate "Save Settings" button. When clicked, EntropyLab immediately records your theme preference to `config.json`. The next time you open EntropyLab, it launches in your preferred theme automatically.

---

## Where Your Data Lives: `%APPDATA%\EntropyLab\`

On first launch, EntropyLab automatically provisions a private storage directory at:

```text
%APPDATA%\EntropyLab\
```

*(On a standard Windows installation, this resolves to `C:\Users\<YourUsername>\AppData\Roaming\EntropyLab\`.)*

Inside this directory are the three pillars of EntropyLab's local state:

```text
%APPDATA%\EntropyLab\
├── config.json
├── entropylab.db
└── mocks\
     ├── -github-users-octocat.json
     └── ...
```

---

### 1. `config.json` (Configuration & Rules)

`config.json` is a single, human-readable JSON file that contains all non-traffic configuration:
- The proxy listening port (e.g., `8080`).
- The active theme mode (`"LIGHT"` or `"DARK"`).
- All configured route mappings (local paths, target URLs, enabled flags).
- All configured chaos rules (latency ms, failure percentages, status codes, sub-path filters).
- All configured mock mappings (paths, target file locations, auto-generated flags).

> **For Experts:** Because `config.json` is formatted as standard JSON, advanced users can inspect or hand-edit configuration settings directly in their favorite code editor. However, you **must close EntropyLab before editing `config.json`**; otherwise, active in-memory states may overwrite your changes on shutdown.

> **Warning:** What happens if you introduce a syntax error while editing `config.json`? EntropyLab parses this file safely using Jackson. If the JSON is malformed or invalid upon launch, **EntropyLab will not crash**. Instead, it logs the parse error, re-initializes a clean default configuration, and saves it. While this guarantees the application always opens, **you will lose your custom routes and rules**. Always make a backup copy of `config.json` before hand-editing!

---

### 2. `entropylab.db` (Traffic History Database)

`entropylab.db` is an embedded **SQLite 3** database file that holds your permanent traffic audit log.

Every HTTP request that passes through the reverse proxy is asynchronously appended to the `request_log` table in this database:
- Exact millisecond timestamps.
- HTTP method, local path, and target upstream URL.
- Numeric status codes and execution duration in milliseconds.
- Processing classification (`FORWARDED`, `MOCKED`, `CHAOS_STATUS`, `CHAOS_RESET`).
- Full request headers, request bodies, response headers, and response bodies.

> **For Experts:** While the Inspector visual table limits its display to the 500 most recent rows for UI performance, `entropylab.db` stores your complete, unclipped history. You can open `entropylab.db` directly using any standard SQLite GUI (such as *DB Browser for SQLite*, DBeaver, or the `sqlite3` CLI) to run custom SQL queries, export test runs to CSV, or extract complex historical payloads for regression testing:
> ```sql
> SELECT time(timestamp/1000, 'unixepoch'), method, path, statusCode, durationMs 
> FROM request_log 
> WHERE type = 'CHAOS_STATUS' 
> ORDER BY timestamp DESC;
> ```

---

### 3. `mocks\` (Saved Mock Files)

The `mocks\` sub-directory holds the physical JSON response payloads used by the mocking system.

- When you click **Save as Mock** in the Inspector, the response body is sanitized and written here as a `.json` file (e.g., `-api-v1-users.json`).
- When you use **Add Mock** to create a manual mock, you can also place your custom JSON files directly into this folder to keep your mock assets organized in one place.
- Files inside `mocks\` can be opened and edited in any text editor at any time. Because EntropyLab reads the file from disk dynamically on every matching request, your file modifications are served immediately.

---

## Step-by-Step: Backing Up and Migrating Your Setup

To backup or move your EntropyLab setup to a coworker's PC or a secondary workstation:

1. Close EntropyLab to ensure all in-memory buffers are flushed to disk.
2. Press **Win + R**, type:
   ```text
   %APPDATA%\EntropyLab
   ```
   and press **Enter**. Windows Explorer will open directly to your data directory.
3. Copy the entire `EntropyLab` folder (or specific files like `config.json` and the `mocks\` directory) to a flash drive, shared network drive, or repository.
4. On the destination machine, install EntropyLab and paste the `EntropyLab` folder into the target machine's `%APPDATA%\` directory.
5. Launch EntropyLab. Your routes, chaos rules, and mock endpoints will appear exactly as configured.

---

## Field-by-Field Reference

| File / Component | Type | Purpose | Format | Hand-Editable? |
|---|---|---|---|---|
| **Theme Toggle** | UI Control | Toggles Light and Dark theme modes. | Saved as `"LIGHT"` / `"DARK"` in config. | Via UI icon or config.json. |
| **`config.json`** | File | Stores routes, chaos rules, and proxy port. | Pretty-printed JSON. | Yes *(while app is closed)*. |
| **`entropylab.db`** | File | Permanent audit log of all proxy traffic. | SQLite 3 database. | Via SQLite query tools. |
| **`mocks\`** | Directory | Storage folder for mock response bodies. | Individual `.json` files. | Yes *(updates take effect live)*. |

## Common Mistakes & Troubleshooting

- **Editing `config.json` While EntropyLab Is Running**:
  If you edit `config.json` while the app is running, your edits may be overwritten when EntropyLab updates its state or closes. Always quit the app before hand-editing.
- **Accidental JSON Syntax Errors in `config.json`**:
  If you edit `config.json` and forget a closing brace or trailing comma, EntropyLab will reset the file to blank defaults on launch. Keep a `.bak` copy of your configuration before editing.
- **Deleting `entropylab.db`**:
  If you wish to clear your historical traffic logs completely, closing EntropyLab and deleting `entropylab.db` is safe. EntropyLab will automatically create a fresh, empty database on its next launch.

## Related Reading

- [Installation and First Launch](../00-orientation/03-installation-and-first-launch.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
- [Common Errors and Fixes](../07-troubleshooting/01-common-errors-and-fixes.md)
