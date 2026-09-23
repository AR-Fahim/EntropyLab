# Limitations & Frequently Asked Questions (FAQ)
*For: all users, especially developers accustomed to Charles Proxy, Fiddler, or Postman.*

## What Is It? (Reverse Proxy vs. Transparent MITM Proxy)

If you have previously used network debugging tools like **Charles Proxy**, **Fiddler**, or **mitmproxy**, you may expect EntropyLab to transparently capture all outbound internet traffic automatically the moment you launch the application.

**EntropyLab does not work that way.**

EntropyLab is an **application-level Reverse Proxy**, not a transparent "Man-in-the-Middle" (MITM) proxy. Understanding this architectural distinction is the single most important concept for successfully using EntropyLab:

| Feature Dimension | Transparent MITM Proxy (Charles / Fiddler) | EntropyLab (Reverse Proxy) |
|---|---|---|
| **Traffic Interception** | Intercepts *all* outbound computer traffic globally by altering OS network settings. | Listens *only* on a dedicated local port (`localhost:<port>`). |
| **Client Configuration** | None; intercepts silently (or requires OS proxy settings). | You must explicitly point your app's API base URL to EntropyLab. |
| **HTTPS Decryption** | Requires installing and trusting a custom Root CA certificate. | **No certificates required.** Traffic between your app and EntropyLab is plain HTTP; EntropyLab initiates clean outbound HTTPS calls. |
| **System Footprint** | Invasive; can break VPNs, corporate firewalls, and certificate pinning. | **Zero system intrusion.** Leaves OS proxy and certificate stores completely untouched. |

### How You Connect Your Application

With EntropyLab, traffic is never captured automatically. You must configure your application or HTTP client to target EntropyLab's local address instead of calling the remote service directly:

- **Instead of calling:** `https://api.stripe.com/v1/charges`
- **You configure your app to call:** `http://localhost:8080/stripe/v1/charges`

EntropyLab receives your local HTTP request, applies your configured mocks and chaos rules, connects to the upstream HTTPS API over the internet on your behalf, and relays the response back to your application.

## Why It Matters: The Tradeoffs of Reverse Proxy Architecture

Why did we design EntropyLab as a reverse proxy rather than a transparent MITM proxy?

This was a deliberate design decision tailored for local development convenience and system safety:
1. **Zero Certificate Management**: Transparently decrypting live HTTPS traffic requires generating custom SSL certificates and installing them into Windows' Trusted Root Certification Authorities store. This frequently triggers security warnings, violates corporate security policies, and breaks applications that use SSL pinning. EntropyLab requires zero certificates.
2. **Deterministic & Surgical**: Because you explicitly point your application at EntropyLab, only the specific microservices or routes you want to test pass through the proxy. Your email client, web browser sessions, IDE updates, and Slack calls are never touched or monitored.
3. **Clean Teardown**: Closing EntropyLab leaves zero lingering proxy settings on your operating system. If Charles Proxy crashes or closes abnormally, your computer often loses internet access until you manually uncheck the Windows proxy settings. With EntropyLab, this never happens.

---

## Platform Support & Explicit Scope Boundaries

- **Windows Only**: This release of EntropyLab is compiled and packaged exclusively for **64-bit Windows 10 and Windows 11**. macOS and Linux distributions are planned for future versions.
- **No Request Modification/Replay**: EntropyLab does not currently support replaying requests or mutating request bodies in flight.
- **No Global Cloud Sync**: All configuration, logs, and mocks are stored 100% locally on your machine. There is no cloud dashboard, remote team sync, or user account system.

---

## Frequently Asked Questions (FAQ)

### Can I use EntropyLab with HTTPS-only external APIs?
**Yes, absolutely.** 
EntropyLab routinely proxies traffic to secure external APIs like GitHub (`https://api.github.com`) and Stripe (`https://api.stripe.com`). 
The key detail to remember:
- The **incoming connection** from your local application to EntropyLab is plain HTTP (`http://localhost:8080/...`).
- The **outgoing connection** from EntropyLab to the real target is full, encrypted HTTPS (`https://...`). 
EntropyLab handles all upstream TLS handshakes and encryption seamlessly in the background.

### Does EntropyLab work when I am completely offline?
**Yes, for Mocks; No, for Forwarded Routes.**
- If a path matches an enabled mock in the **Mocks** tab, EntropyLab reads the response payload directly from your local hard drive. It works 100% offline—you can disconnect from Wi-Fi entirely.
- If a path forwards to a real external API (Type = `FORWARDED`), EntropyLab must have an active internet connection to contact the destination server. If your internet is down, forwarded calls will return `502 Bad Gateway`.

### Is any of my data, traffic, or telemetry sent to external servers?
**No. Never.**
EntropyLab is 100% private, self-contained, and offline-first. It contains zero analytics libraries, zero telemetry trackers, and zero external cloud connections. The only network packets EntropyLab ever sends are the direct HTTP requests to the target base URLs you explicitly configure in the **Routes** tab.

### Can I edit `config.json` by hand?
**Yes, but only while EntropyLab is completely closed.**
Your configuration is stored at `%APPDATA%\EntropyLab\config.json`. Because it is standard, human-readable JSON, you can inspect it or edit it in VS Code. However, you must exit the app before saving changes, and you must ensure your JSON syntax is valid. If you make a syntax error, EntropyLab will reset to defaults on launch to protect itself from crashing. See [Theme & Data Storage](../05-settings-and-data/01-theme-and-data-storage.md) for details.

### What happens to my data if I uninstall EntropyLab?
**Your configuration, mocks, and logs are preserved.**
When you uninstall EntropyLab through Windows *Settings > Apps > Installed apps* (or *Add or Remove Programs*), the Windows uninstaller removes the executable binary and application files from `C:\Program Files\EntropyLab\`. 

Following standard Windows desktop conventions, the uninstaller **does not touch your user data directory** at `%APPDATA%\EntropyLab\`. Your `config.json`, traffic database (`entropylab.db`), and saved mocks (`mocks\`) remain completely intact on your hard drive. If you reinstall EntropyLab later, your exact configuration will be waiting for you. If you wish to permanently erase all data, manually delete the `%APPDATA%\EntropyLab\` folder after uninstalling.

### Can I run multiple instances of EntropyLab on the same machine?
**Not on the same port.**
EntropyLab binds exclusively to the TCP port specified in the **Proxy Control** tab (default `8080`). If you attempt to launch a second instance on the same port, the second instance will report `Address already in use: bind`. You can, however, run multiple instances if you assign each instance a unique port (e.g., `8080` and `8085`).

---

## Key Terms Introduced Here

- **Reverse Proxy**: A local server that receives your requests and forwards them to a real API on your behalf, so you can intercept and modify traffic in between.
- **Man-in-the-Middle (MITM) Proxy**: A proxy tool (like Charles or Fiddler) that transparently captures all system traffic by installing custom root certificates and altering OS network adapters.
- **Upstream Connection**: The outbound network call made from EntropyLab to the real destination API over the internet.
- **Downstream Connection**: The local network call made from your client application to EntropyLab on `localhost:<port>`.

---

## Related Reading

- [Core Concepts: How Reverse Proxies & Chaos Work](../00-orientation/02-core-concepts.md)
- [Common Errors and Fixes](./01-common-errors-and-fixes.md)
- [Theme & Data Storage: Managing AppData](../05-settings-and-data/01-theme-and-data-storage.md)
- [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)
