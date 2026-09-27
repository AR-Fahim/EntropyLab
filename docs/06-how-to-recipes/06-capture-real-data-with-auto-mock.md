# How-To: Capture Real Data with Auto-Mock
*For: developers building offline-capable test environments and conserving third-party API rate limits.*

## Goal

Capture an authentic API response from a live remote service with a single click, allowing you to continue developing and testing offline without consuming external API quotas or rate limits.

### Why Auto-Mock Is Faster Than Hand-Crafting JSON

In [How-To: Unblock Frontend with Manual Mocks](./05-unblock-frontend-with-manual-mocks.md), you learned how to hand-write a `.json` file from scratch. While hand-crafting is essential when an API doesn't exist yet, it is tedious and error-prone when testing against existing APIs with massive, nested payloads. 

**Auto-Mock Snapshot** is the fastest way to create a mock: you call the real endpoint once, click **Save as Mock**, and EntropyLab captures every authentic field, nested array, and data type instantly.

## Prerequisites

Before following this recipe, ensure you understand:
- How to route traffic through EntropyLab (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- How the Traffic Inspector logs payloads (see [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy status is green: `Running on port 8080`.
2. In the **Routes** tab, ensure you have an active route mapping (for example, **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Open your browser or API client and make a single live request to the endpoint you want to snapshot:
   ```text
   http://localhost:8080/github/users/octocat
   ```
4. Verify that your browser loads the live data from GitHub.
5. Switch to EntropyLab and click the **Inspector** tab in the top navigation bar.
6. Click the top row in the table (the request you just made, showing Type `FORWARDED`) to highlight it.
7. Click the **Save as Mock** button located in the toolbar directly above the table.
8. An information dialog appears confirming:
   ```text
   Mock saved and activated for /github/users/octocat
   ```
9. Click **OK**.

---

## Expected Result

The mock is now active immediately. To prove that EntropyLab is serving your saved snapshot without contacting the internet:

1. Return to your web browser and reload the page:
   ```text
   http://localhost:8080/github/users/octocat
   ```
2. **Instant Delivery**: The response loads in under `10 ms` (compared to the 250+ ms required for live internet transit).
3. **Inspector Confirmation**: Switch to the **Inspector** tab:
   - A new row appears at the top.
   - The **Type** column now displays **`MOCKED`**.
   - The **Duration (ms)** column displays single-digit milliseconds.
4. **The Offline Test**: Disconnect your computer from Wi-Fi or unplug your network cable. Refresh the browser page again. The user profile data renders perfectly, proving your development environment is 100% functional without an internet connection.

---

## Variations

- **Want to refresh the mock with newer live data?**
  Over time, remote API data changes. To update your snapshot:
  1. Open the **Mocks** tab and uncheck the **Enabled** box for `/github/users/octocat` (allowing traffic to reach GitHub again).
  2. Send a new request from your browser to fetch fresh live data.
  3. In the **Inspector** tab, select the new `FORWARDED` row and click **Save as Mock**.
  4. EntropyLab automatically overwrites the existing mock file in `%APPDATA%\EntropyLab\mocks\` in place—no duplicate files or orphaned mappings are created.
- **Want to tweak the captured data?**
  Auto-mocked files are standard JSON files stored on your hard drive at:
  ```text
  %APPDATA%\EntropyLab\mocks\-github-users-octocat.json
  ```
  Open this file in VS Code or Notepad, modify a field (e.g., change `"name": "The Octocat"` to `"name": "Super Octocat"`), and save. Refresh your browser to see your modifications immediately.
- **Verify in the Mocks Tab:**
  Click the **Mocks** tab. You will see your snapshot listed with **Auto-Generated: Yes**, pointing to the file in your AppData directory.
- Use Edit Mock to manually point an existing auto-generated mock at a different saved file, or use Delete Mock to remove one you no longer need — no need to re-capture from scratch.

---

## Related Reading

- [Inspecting Details and Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
- [How-To: Unblock Frontend with Manual Mocks](./05-unblock-frontend-with-manual-mocks.md)
- [Theme and Data Storage: Where Mocks Live on Disk](../05-settings-and-data/01-theme-and-data-storage.md)
