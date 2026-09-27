# How-To: Unblock Frontend with Manual Mocks
*For: frontend and mobile developers building UI components before backend endpoints exist.*

## Goal

Create and serve a hand-crafted JSON mock endpoint in EntropyLab so you can develop, style, and test your frontend components immediately—without waiting for the backend team to finish building the real API.

## Prerequisites

Before following this recipe, ensure you understand:
- How to start and verify the proxy engine (see [Proxy Control Reference](../01-proxy-and-routes/02-proxy-control.md)).
- The fundamentals of static mock files (see [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md)).

---

## Steps

### Step 1: Author Your Expected Future API Payload

In any text editor (such as VS Code or Notepad), create a new file named `checkout-v2.json` and save it to your local workspace or Desktop.

Paste the JSON structure you expect the backend to eventually return:

```json
{
  "orderId": "ord_9843102",
  "status": "APPROVED",
  "currency": "USD",
  "totalAmount": 149.99,
  "items": [
    {
      "sku": "ITEM-PRO-4K",
      "name": "Ultra HD Monitor Stand",
      "quantity": 1,
      "price": 149.99
    }
  ],
  "estimatedDelivery": "2026-10-02"
}
```

### Step 2: Register the Mock in EntropyLab

1. Open EntropyLab and click the **Proxy Control** tab. Verify the status reads: `Running on port 8080`.
2. Click the **Mocks** tab in the top navigation bar.
3. Click the **Add Mock** button in the toolbar.
4. In the **Local Path** field, type the future endpoint path your frontend application will call:
   ```text
   /api/v2/checkout
   ```
5. Click the **Choose File...** button.
6. Browse to the `checkout-v2.json` file you created in Step 1, select it, and click **Open**.
7. Click the **Add** button in the dialog.

> **Tip:** **You do NOT need to create a Route for this mock to work.** Mocks operate independently and evaluate before the routing engine. If an incoming request matches an enabled mock's exact path, EntropyLab serves the file immediately without ever looking at the **Routes** tab.

### Step 3: Call the Endpoint From Your App

Configure your frontend application's API base URL to `http://localhost:8080`, or test it directly in your browser:
```text
http://localhost:8080/api/v2/checkout
```

---

## Expected Result

- **In Your Browser / Frontend Application**:
  - The request returns instantly with HTTP `200 OK` and `Content-Type: application/json`.
  - Your application renders the order summary, prices, and items immediately using the mock data.
- **In EntropyLab's Inspector**:
  - Open the **Inspector** tab. The top entry displays:
    - **Method**: `GET` (or `POST`)
    - **Path**: `/api/v2/checkout`
    - **Status**: `200`
    - **Duration (ms)**: `< 10 ms` (typically 3–5 ms)
    - **Type**: **`MOCKED`**

---

## Variations

- **Live In-Place Editing (No Re-Mapping Needed):**
  Need to test what happens if an item is out of stock or if the price is zero? Open `checkout-v2.json` in your editor, edit the values, and save the file. Refresh your frontend. EntropyLab reads the file dynamically from disk on each request, so your edits appear immediately without touching EntropyLab.
- **Switching Between Edge Cases:**
  Create multiple JSON files for different testing states (e.g., `checkout-success.json`, `checkout-card-declined.json`, `checkout-empty.json`). Use **Add Mock** to switch paths, or simply point your existing mock to the new file.
- **Seamlessly Transitioning to the Real Backend Later:**
  When your backend team finally deploys the real endpoint:
  1. Add a route in the **Routes** tab mapping `/api` to your real backend URL.
  2. In the **Mocks** tab, simply uncheck the **Enabled** box for `/api/v2/checkout`.
  3. Traffic immediately falls through to the real backend without changing a single line of code in your frontend application.
- Once the real backend is ready, use Delete Mock to remove the mapping entirely, or use Edit Mock to repoint the same Local Path at a different file without losing your existing configuration.

---

## Related Reading

- [Manual Static Mocking](../04-mocking/01-manual-static-mocking.md)
- [Managing Mocks and Auto-Mock Details](../04-mocking/02-managing-mocks-and-auto-mock-details.md)
- [How-To: Capture Real Data with Auto-Mock](./06-capture-real-data-with-auto-mock.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
