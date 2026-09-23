# About & Credits
*For: all readers interested in project background, architecture technologies, and release history.*

## EntropyLab v1.0.0
**Local Reverse Proxy & Chaos Engineering Studio for Windows**

### Overview & Mission

EntropyLab is a lightweight, high-performance developer desktop studio built for modern API workflows. It enables developers and QA engineers to intercept HTTP traffic locally, inject real-world network anomalies (simulated latency delays, connection resets, and status code overrides), inspect live requests and responses with zero overhead, and capture mock responses directly from live endpoints for offline resilience testing.

EntropyLab is 100% private, offline-first, and runs entirely on your local machine with zero external cloud accounts, telemetry, or system-level certificate tampering.

---

## Technical Architecture & Core Technologies

EntropyLab is engineered as a self-contained, native Windows desktop executable without requiring external runtime dependencies or background daemons.

| Technology / Component | Role in EntropyLab | Why It Was Chosen |
|---|---|---|
| **Java 21 (LTS)** | Core Runtime | Modern Java LTS runtime delivering high-performance asynchronous networking, virtual threads, and cross-platform reliability. |
| **OpenJFX (JavaFX 21)** | Desktop GUI Toolkit | Hardware-accelerated UI engine providing native Windows desktop integration, CSS-based styling, smooth data binding, and instantaneous Light/Dark theme transitions. |
| **JDK Sun HTTP Server & HttpClient** | Embedded Reverse Proxy | Native, high-concurrency HTTP server and asynchronous `java.net.http.HttpClient` pipeline powering low-overhead reverse proxying, remainder-path forwarding, and non-blocking worker threads. |
| **FasterXML Jackson** | JSON Processing Engine | Industry-standard JSON parsing, schema serialization, pretty-printed inspection formatting, and configuration object mapping. |
| **Xerial SQLite JDBC** | Traffic History Database | Zero-configuration embedded SQL database running in **WAL (Write-Ahead Logging)** mode, guaranteeing durable, thread-safe request logging with zero impact on UI responsiveness. |
| **WiX Toolset & jpackage** | Native Windows Packaging | Builds native Windows installer packages (`.exe`/`.msi`) and self-contained application images with Start Menu and desktop shortcuts. |

---

## Version History

| Version | Release Date | Summary of Changes |
|---|---|---|
| **v1.0.0** | September 2026 | **Initial Public Release:**<br>• Embedded reverse proxy engine on customizable TCP ports (default 8080).<br>• Longest-prefix route mapping with dynamic query string passthrough.<br>• Chaos Engine with Latency Injection (0–60,000 ms), Status Code Overrides (500/503/504 with 0–100% failure probability), and Connection Resets.<br>• Sub-path filtering for targeted, endpoint-specific chaos isolation.<br>• Live Traffic Inspector with real-time streaming, 500-row UI cache, and permanent SQLite persistence.<br>• Request Details modal with 4-panel header and pretty-printed JSON body inspection.<br>• Manual Static Mocking (local `.json` file mapping) and single-click Auto-Mock Snapshots.<br>• System-wide Light and Dark mode toggle with persistent configuration. |

---

## Credits & Community Acknowledgments

EntropyLab was built by **Abdur Rahman** and was shaped by developer experience standards established across the open-source engineering community:

- **WireMock & Toxiproxy**: For inspiring declarative chaos injection and mock mapping patterns.
- **Charles Proxy & Fiddler**: For defining the gold standard in desktop network traffic inspection.
- **Postman & Insomnia**: For demonstrating the value of clean, developer-first HTTP interaction workflows.

---

## Related Reading

- [Welcome to EntropyLab](../00-orientation/01-welcome.md)
- [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)
- [Complete UI Field Reference](../08-reference/02-complete-ui-field-reference.md)
- [Limitations & FAQ](../07-troubleshooting/02-limitations-and-faq.md)
