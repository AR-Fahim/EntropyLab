# EntropyLab 🌀

> **Local Reverse Proxy & Chaos Engineering Studio for Developers**

![Version](https://img.shields.io/badge/version-1.0.0-red.svg)
![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

---

## 📌 Overview

**EntropyLab** is a high-performance desktop developer tool engineered for local reverse proxying, traffic inspection, network fault injection, and mock response serving. 

Built with **Java 17** and **JavaFX 21**, EntropyLab provides developers with a dedicated control center to test backend resilience against real-world network failures—including simulated latency spikes, connection resets, and HTTP status code overrides—without modifying code or relying on external cloud proxies.

---

## ✨ Features

- 🔀 **Prefix-Based Reverse Proxying**: Route incoming requests on local paths (e.g. `/stripe`, `/api/v1`) directly to target backends.
- ⚡ **Chaos Engineering Engine**:
  - **Latency Injection**: Introduce configurable delays (e.g., 200ms–5000ms) with customizable failure percentages.
  - **Connection Reset Simulation**: Abruptly terminate client connections to simulate network drops and socket reset errors.
  - **HTTP Status Code Override**: Force custom HTTP failure codes (e.g. `429`, `500`, `503`, `504`) with deterministic JSON error bodies.
  - **Sub-Path Filtering**: Restrict chaos rules to specific sub-paths (e.g. apply chaos only to `/stripe/checkout` while leaving `/stripe/status` untouched).
- 🔍 **Live Traffic Inspector**:
  - Zero-overhead, asynchronous request logging powered by SQLite in WAL mode.
  - Inspect HTTP method, target URL, status code, duration, timestamps, and full request/response headers & payloads.
- 🎭 **Flexible Mock Serving**:
  - **Manual Mocks**: Serve static JSON files directly from disk for specified paths.
  - **One-Click Auto-Mock Snapshotting**: Snapshot real HTTP responses directly from the Inspector table into static mocks for offline development.
- 🎨 **Adaptive Design & Dual Themes**:
  - Polished Light and Dark modes with instant toggling.
  - Consistent styling across all controls, dialogs, alerts, and tables.

---

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 17 (LTS) |
| **Desktop GUI** | JavaFX 21 (OpenJFX) |
| **Embedded Proxy** | `com.sun.net.httpserver.HttpServer` |
| **HTTP Client** | `java.net.http.HttpClient` (HTTP/1.1 & HTTP/2 support) |
| **Persistence (Database)** | SQLite JDBC (Xerial 3.46.1.0) with WAL (Write-Ahead Logging) |
| **JSON & Config** | FasterXML Jackson Databind 2.17.2 |
| **Build & Tooling** | Apache Maven 3.8+ |

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Apache Maven**: Version 3.8 or higher

### Build & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/EntropyLab.git
   cd EntropyLab
   ```

2. **Compile the project**:
   ```bash
   mvn clean compile
   ```

3. **Launch the application**:
   ```bash
   mvn javafx:run
   ```

*(Alternatively, on Windows PowerShell you can run `./dev-run.ps1`)*

### Native Packaging (Windows Installer & Standalone Bundle)

EntropyLab uses JDK 21 `jpackage` to bundle a custom runtime JRE and package a standalone native Windows installer (`.exe` / `.msi`) or portable application image (`app-image`).

#### Requirements for Building Installer
- **JDK 21+** (`jpackage` tool)
- **WiX Toolset 3.11+** (required for `.exe` / `.msi` installers; downloaded automatically or installed to `%USERPROFILE%\.wix311` or on `PATH`).

#### Building the Installer

You can build the native installer with either Maven or the provided PowerShell packaging script:

- **Via Maven Profile**:
  ```bash
  mvn package -Pinstaller
  ```
- **Via PowerShell Script**:
  ```powershell
  powershell -ExecutionPolicy Bypass -File package.ps1
  ```

#### Packaging Output

- **Installer Location**: `target/installer/EntropyLab-1.0.0.exe`
- **Output Type**: Self-extracting Windows installer with custom Start Menu shortcut, Desktop icon, and installation directory chooser.
- **Standalone Runtime**: Bundles a tailored Java runtime (`javafx.controls`, `jdk.httpserver`, `java.net.http`, `java.sql`, `java.desktop`, etc.). End users do **not** need Java installed to run EntropyLab.
- **Data Persistence**: Resolves runtime configuration and databases automatically to `%APPDATA%\EntropyLab\` (with fallback to `~/.entropylab`).

---

## 📖 How to Use

1. **Add a Route Mapping**:
   Navigate to the **Routes** tab, click **Add Route**, and specify a local path prefix (e.g. `/api`) and your target backend base URL (e.g. `https://httpbin.org`).
2. **Optionally Configure Chaos Rules**:
   Switch to **Chaos Rules** to attach simulated latency, connection resets, or HTTP status overrides to any route. You can also specify an optional sub-path filter (e.g. `/checkout`).
3. **Start the Proxy**:
   In the **Proxy Control** tab, verify the proxy port (default: `8080`) and click **Start Proxy**.
4. **Point Your Application**:
   Direct your frontend, API client, or microservice to:
   ```
   http://localhost:8080/<localPath>
   ```
5. **Inspect Live Traffic**:
   Open the **Inspector** tab to examine requests in real time. Double-click or select any row to view complete headers and payloads.
6. **Save Responses as Mocks**:
   Select any recorded response in the Inspector and click **Save as Mock**. EntropyLab automatically snapshots the response and registers it in the **Mocks** tab.
7. **Toggle Light/Dark Mode Anytime**:
   Click the **day-night icon** in the top title bar at any time to switch themes.

---

## ⚙️ Configuration & Data Storage

EntropyLab stores configuration and runtime data in the system user directory:

- **Windows**: `%APPDATA%\EntropyLab`
  - `config.json`: Route mappings, chaos rules, and proxy settings.
  - `entropylab.db`: High-speed SQLite traffic log database.
  - `mocks/`: Directory containing auto-generated and manual mock JSON files.
- **Environment Configuration (`.env`)**:
  - Optional `.env` file in the root directory to dynamically configure developer social links:
    ```properties
    DEVELOPER_NAME=Abdur Rahman
    DEVELOPER_GITHUB=https://github.com/your-github
    DEVELOPER_LINKEDIN=https://linkedin.com/in/your-linkedin
    DEVELOPER_TWITTER=https://x.com/your-handle
    ```

---

## 👨‍💻 About the Developer

Developed by **Abdur Rahman** — a Computer Science & Engineering student building software for fun, learning, experimenting, and as a hobby.

- **GitHub**: [github.com/](https://github.com/) *(configurable via `.env`)*
- **LinkedIn**: [linkedin.com/in/](https://linkedin.com/in/) *(configurable via `.env`)*

---

## 📜 Credits & Third-Party Libraries

We gratefully acknowledge the following open-source projects:

- **[OpenJFX](https://openjfx.io/)**: Modern JavaFX controls and UI platform.
- **[FasterXML Jackson](https://github.com/FasterXML/jackson)**: JSON parsing and object serialization.
- **[Xerial SQLite JDBC](https://github.com/xerial/sqlite-jdbc)**: Embedded SQLite driver.
- **JDK HTTP Stack**: Embedded HTTP server and asynchronous client.
- **Open Source Community**: Concepts inspired by developer tools like Postman, WireMock, and Apache Commons.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
