# Installation and First Launch
*For: everyone installing and opening EntropyLab for the first time.*

## Overview

EntropyLab is distributed as a self-contained desktop application. Setting it up takes less than two minutes and requires no database configuration, background service setup, or external dependencies.

> **Note:** EntropyLab is currently supported exclusively on **Windows** (Windows 10 and Windows 11, 64-bit). macOS and Linux builds are planned for future releases.

## Step-by-Step: Installing EntropyLab

Follow these steps to install EntropyLab on your Windows machine:

1. **Download the Installer**: Download the latest installer package (`EntropyLab-1.0.0.msi` or `.exe`) from the official release repository.
2. **Launch the Installer**: Double-click the downloaded setup file (`EntropyLab-1.0.0.msi`). If Windows SmartScreen displays an unrecognized app prompt on new builds, click **More info** and select **Run anyway**.
3. **Follow the Setup Wizard**: 
   - **Welcome**: The installer greets you with the EntropyLab version information. Click **Next**.
   - **Destination Folder**: By default, EntropyLab installs to `C:\Program Files\EntropyLab\`. Click **Change...** (or **Browse...**) if you want to select a custom installation directory or drive. Click **Next**.
   - **Shortcut Options**: Choose whether to install a desktop shortcut and a Start Menu program group. Click **Next**.
   - **Ready to Install**: Click **Install** to begin the installation (accept the standard Windows UAC administrator prompt).
4. **Finish and Launch**: Click **Finish** to close the setup wizard. Launch **EntropyLab** via the desktop icon or search for **EntropyLab** in your Windows Start Menu.

### Installation Steps at a Glance

| Step | Action | Expected Result |
|---|---|---|
| **1. Download** | Save `EntropyLab-1.0.0.msi` locally | Windows Installer package downloaded |
| **2. Run** | Double-click `EntropyLab-1.0.0.msi` | Setup wizard opens with EntropyLab branding |
| **3. Choose Destination** | Accept default or click **Change...** to select custom path | Target installation path confirmed |
| **4. Shortcuts** | Select Desktop / Start Menu shortcut options | Shortcut preferences saved |
| **5. Install & Finish** | Click **Install** then **Finish** | App and bundled JRE deployed to destination |
| **6. Launch** | Open via Start Menu or Desktop shortcut | EntropyLab main window appears |

## The Application at First Launch

When you open EntropyLab for the very first time, the main application window appears.

![Screenshot: EntropyLab main window immediately after first launch, showing the default Light theme, the top navigation bar with the dark mode toggle, the brand logo, the seven navigation tabs (Proxy Control, Routes, Chaos Rules, Inspector, Mocks, Analytics, and About), and the Proxy Control tab active displaying port 8080 with a red 'Stopped' status label](./images/first-launch-main-window.png)

### What Happens Behind the Scenes

On first launch, EntropyLab automatically initializes an internal data directory at:

```text
%APPDATA%\EntropyLab\
```

For beginners, you can think of `%APPDATA%\EntropyLab\` as a private folder just for this app's settings and data—you don't need to touch it. EntropyLab uses this folder to store your configuration file (`config.json`), your local traffic history database (`entropylab.db`), and any saved mock responses (`mocks\`). If the folder does not exist, the app creates it cleanly on startup.

### First-Launch Appearance & Dark Mode

By default, EntropyLab launches in **Light Mode**. 

> **Tip:** If you prefer working in a dark interface, you can toggle **Dark Mode** at any time by clicking the theme toggle icon located in the upper-right corner of the top bar. Your theme choice is saved automatically and persists every time you launch the app.

### What to Expect on First Launch: Why the Tabs Are Empty

When you click through the tabs on first launch, you will notice that almost everything is blank:
- The **Proxy Control** tab displays port `8080` with the status indicator showing **Stopped** in red.
- The **Routes** tab contains an empty table with no active route mappings.
- The **Chaos Rules** tab shows no configured failure rules.
- The **Inspector** tab contains an empty traffic log.
- The **Mocks** tab contains no mock mappings.

This empty state is completely normal! EntropyLab does not ship with pre-configured routes or mock endpoints because it is waiting for you to tell it which target API you want to test. 

You do not need to configure anything manually right now—the next page ([Quick Start Tutorial](./04-quick-start-tutorial.md)) will guide you step-by-step through starting the proxy, adding your very first route, and testing your first network anomaly.

## Common Mistakes & Troubleshooting

- **Windows SmartScreen Alert**: When installing on Windows 10/11, you may see a "Windows protected your PC" pop-up. Click **More info**, then click **Run anyway**.
- **Port 8080 Already in Use**: If another program (such as a local web server, Tomcat, or Docker container) is already using port `8080`, EntropyLab will display a port conflict error when you attempt to start the proxy. You can simply change the port number in the **Proxy Control** tab to any open port, such as `8085` or `9090`.
- **Accidental Manual Edits to AppData**: Do not edit or delete files inside `%APPDATA%\EntropyLab\` while EntropyLab is running, as this can corrupt your active session history or configuration.

## Related Reading

- [Welcome to EntropyLab](./01-welcome.md)
- [Core Concepts: How Reverse Proxies & Chaos Work](./02-core-concepts.md)
- [Quick Start Tutorial: Your First Chaos Test](./04-quick-start-tutorial.md)
- [Settings & Data Storage: Managing AppData and Themes](../05-settings-and-data/01-theme-and-data-storage.md)
- [Common Errors and Fixes](../07-troubleshooting/01-common-errors-and-fixes.md)
