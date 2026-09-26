# How-To: Test Malformed JSON Resilience
*For: intermediate and advanced developers (Marcus & Priya) verifying JSON parser error boundaries, deserialization error handling, and data corruption resilience.*

## Goal

Test whether your application crashes or handles errors gracefully when a real upstream API returns corrupted, malformed, or invalid JSON payload data.

## Prerequisites

Before following this recipe, ensure you understand:
- How to create and enable a proxy route (see [Understanding & Managing Routes](../01-proxy-and-routes/01-understanding-and-managing-routes.md)).
- How payload corruption operates on real responses (see [Payload Mutation Reference](../02-chaos-engine/06-payload-mutation.md)).
- How to inspect mutated traffic logs (see [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)).

---

## Steps

1. In EntropyLab, click the **Proxy Control** tab and confirm the proxy status is green: `Running on port 8080` (if stopped, click **Start Proxy**).
2. Click the **Routes** tab and ensure you have an active route mapping (for example, **Local Path** `/github` mapped to **Target Base URL** `https://api.github.com`).
3. Click the **Chaos Rules** tab in the top navigation bar.
4. Click to select the row for your route (e.g., `/github`).
5. Click **Edit Chaos Rule** in the toolbar (or double-click the selected row).
6. In the dialog, scroll down to the **Payload Mutation** section and check the **Enable Payload Mutation** checkbox.
7. In the **Mutation Intensity** field, enter:
   ```text
   10
   ```
8. Click **OK** to save the rule.
9. Verify that the **Chaos Rules** table updates its summary column to display:
   ```text
   Latency: OFF | Status: OFF | Reset: OFF | Mutation: 10 chars
   ```
10. Trigger a request through your application, or make a test call directly from terminal:
    ```bash
    curl http://localhost:8080/github/users/octocat
    ```

---

## Expected Result

- **In Your Client Application**:
  - The HTTP request returns a `200 OK` status from GitHub, but the response body contains 10 randomly scrambled characters scattered throughout the text.
  - Because characters like quotes (`"`), colons (`:`), commas (`,`), or curly braces (`{`, `}`) were likely corrupted, your client's JSON parser (such as `JSON.parse()` in JavaScript, `Jackson`/`Gson` in Java, or `json.loads()` in Python) will encounter a syntax error.
  - **Pass Criteria**: Your application's `try/catch` error boundary intercepts the parsing error, logs a diagnostic warning, and displays a graceful fallback interface or user-friendly message.
  - **Fail Criteria**: Your application crashes, produces an unhandled promise rejection, throws a fatal white-screen panic, or silently hangs waiting for corrupted data fields.
- **In EntropyLab's Inspector**:
  - Switch to the **Inspector** tab. The top row shows:
    - **Method**: `GET`
    - **Path**: `/github/users/octocat`
    - **Status**: `200`
    - **Type**: **`FORWARDED_MUTATED`**
  - Click the row to open the detail drawer. In the **Response Body** tab, compare the scrambled payload against the expected format: notice the random printable ASCII characters interspersed within the payload, breaking JSON syntax.

---

## Variations

- **Want to test catastrophic corruption (worst-case destruction)?**
  Increase the **Mutation Intensity** field to `100` or `250`. With hundreds of characters scrambled, large sections of the payload are destroyed. This tests whether your data sanitizers, buffer parsers, and stream decoders survive severe packet corruption or encoding mismatches.
- **Want to test subtle schema bugs (single-character corruption)?**
  Reduce the **Mutation Intensity** to `1`. In many responses, corrupting only 1 character will not break JSON syntax (for example, mutating `"isAdmin": true` to `"isAdmin": trfe` or altering an alphanumeric UUID like `"id": "9a12b"` to `"id": "9x12b"`). This allows you to verify whether your frontend validation schemas (such as Zod, Yup, or JSON Schema) catch invalid data types without crashing.
- **Want to simulate a slow AND corrupted response (compound chaos)?**
  In the **Edit Chaos Rule** dialog, check **Enable Latency** and enter `2000 ms` in addition to enabling **Payload Mutation**. Your application will be forced to wait through a 2-second delay and then receive broken data, testing both timeout handling and parsing recovery in one test run.
- **Want to scope mutation to a single endpoint?**
  In the **Sub-path filter (optional)** field, enter `/users`. Calls to `http://localhost:8080/github/users/octocat` will be mutated, while requests to other paths under the same route (such as `/github/repos/octocat/Hello-World`) will pass through cleanly with their original uncorrupted payloads.

---

## Related Reading

- [Payload Mutation Reference](../02-chaos-engine/06-payload-mutation.md)
- [Sub-Path Filtering & Combining Rules](../02-chaos-engine/05-sub-path-filtering-and-combining-rules.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
- [Inspecting Details & Auto-Mock](../03-inspector/02-inspecting-details-and-auto-mock.md)
- [How-To: Simulate a Slow API](./01-simulate-a-slow-api.md)
- [How-To: Combine Latency and Failure](./04-combine-latency-and-failure.md)
