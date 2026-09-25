# Payload Mutation: Testing Malformed JSON & Corrupted Data
*For: all developers and QA engineers testing malformed JSON resilience, parser fault-tolerance, and data integrity.*

## Overview

**Payload Mutation** is a chaos engineering feature that intentionally corrupts response data returned by upstream services. Instead of simulating network-level problems (such as slow connections or dropped packets) or protocol-level failures (such as HTTP 500 error codes), payload mutation simulates **application-level data corruption**. 

When payload mutation is active on a route, EntropyLab waits for a real API response to return successfully, randomly scrambles a user-defined number of characters within the response body, and delivers the corrupted data to your client application.

This feature reveals critical vulnerabilities that traditional error-code testing overlooks:
- Unhandled `JSON.parse` or deserializer syntax exceptions that crash client applications or trigger white-screen freezes.
- Fragile schema assumptions where missing quotes, unexpected symbols, or truncated fields cascade into unhandled runtime panics.
- Missing fallback error boundaries, validation layers, or data sanitization routines.

> [!WARNING]
> **Payload Mutation applies ONLY to successfully forwarded (`FORWARDED`) responses.** It never applies to **Mocked** responses, forced **Status Code Overrides**, or **Connection Resets** because those failure modes intercept traffic before contacting the target API and do not involve a real upstream payload.

## The Chaos Rules Tab & Payload Mutation Controls at a Glance

Payload mutation settings are managed alongside other chaos features in the per-route **Edit Chaos Rule** dialog.

![Screenshot: Edit Chaos Rule dialog for route /github, showing the bottom Payload Mutation section with Enable Payload Mutation checked and Mutation Intensity spinner set to 5](./images/chaos-payload-mutation-dialog.png)

When configuring payload mutation:
1. Navigate to the **Chaos Rules** tab to inspect all configured route mappings.
2. Select a route and click **Edit Chaos Rule** (or double-click the table row).
3. The bottom section of the dialog contains the **Payload Mutation** controls:
   - The **Enable Payload Mutation** checkbox (activates or deactivates body scrambling).
   - The **Mutation Intensity** spinner (configures how many characters are randomly corrupted).
4. When saved, the **Chaos Summary** column for that route updates to reflect the active mutation intensity (for example, `Mutation: 5 chars`).

## Step-by-Step: Enabling Payload Mutation

Follow these steps to corrupt responses on any active route:

1. Click the **Chaos Rules** tab in the top navigation bar.
2. Click to select the route you want to target (for example, `/github`).
3. Click **Edit Chaos Rule** in the toolbar (or double-click the selected row).
4. In the dialog, scroll to the bottom section and check the **Enable Payload Mutation** checkbox.
5. In the **Mutation Intensity** field, choose how many characters to corrupt (default is `5`). You can type a number or use the up/down spinner arrows.
6. Click **OK** to save the rule.
7. Confirm that the **Chaos Rules** table displays your updated rule in the **Chaos Summary** column:
   ```text
   Latency: OFF | Status: OFF | Reset: OFF | Mutation: 5 chars
   ```

The rule takes effect immediately for all subsequent forwarded traffic. You do not need to restart the proxy server.

## Verifying Mutated Responses with the Inspector

To observe mutated traffic and confirm your client's error-handling behavior:

1. Send an HTTP request to your proxied endpoint through your browser, curl, or Postman:
   ```bash
   curl http://localhost:8080/github/users/octocat
   ```
2. Inspect the raw text returned to your client. You will notice scattered random ASCII characters replacing parts of the original JSON (for example, breaking keys, values, or bracket structures):
   ```json
   {"login": "octocat", "id": 583231, "n%de_id": "MDQ6VXNlcjU4MzIzMQ==", "av#tar_url": "https://..."}
   ```
3. Open EntropyLab and click the **Inspector** tab.
4. Locate the request in the traffic history table:
   - The **Status** column displays the real HTTP status code returned by the upstream server (e.g. `200`).
   - The **Type** column displays **`FORWARDED_MUTATED`** (instead of standard `FORWARDED`).
5. Click the row to inspect the detail drawer:
   - The **Response Body** tab displays the raw mutated text.
   - If the corruption altered structural JSON tokens (such as quotes, colons, or commas), the Inspector's JSON pretty-printer will gracefully fall back to displaying the raw payload text rather than formatted JSON. This is normal and expected — it confirms that the syntax was successfully broken.

## Sub-path Filter Interaction (Tested Behavior)

> [!IMPORTANT]
> **Sub-path Filtering gates Payload Mutation.**
> During live testing against the running proxy engine, we verified that when an optional **Sub-path filter** (such as `/users`) is defined on the chaos rule, **only forwarded requests matching that sub-path are mutated**. Requests to non-matching endpoints under the same route (such as `/repos`) pass through completely uncorrupted with their original payload and are logged with type `FORWARDED`.

## Field-by-Field Reference

| Field / Control | What it does | Valid values / range | Default |
|---|---|---|---|
| **Enable Payload Mutation** | Master checkbox that enables or disables random response body corruption for the selected route. | Checked (`true`) or Unchecked (`false`). | Unchecked (`false`) |
| **Mutation Intensity** | The exact number of characters randomly replaced with printable ASCII symbols in the response body. | Whole integer between `1` and `1000`. Step size: `1`. | `5` |

### Understanding Mutation Intensity with Worked Examples

Mutation intensity represents the **exact character count** targeted for random corruption per response. Unlike percentage-based chaos rules, intensity operates on absolute character positions:

- **Example 1: Intensity 1 (Subtle Glitch)**
  - Response length: 500 characters.
  - Impact: Exactly 1 character is altered (0.2% corruption).
  - Effect: May silently corrupt a single digit in an ID, flip a character in a name, or occasionally break a JSON quotation mark. Ideal for testing schema validations and subtle parser resilience.
- **Example 2: Intensity 5 (Moderate Malformed JSON — Default)**
  - Response length: 200 characters.
  - Impact: Exactly 5 characters are altered (~2.5% corruption).
  - Effect: Almost guaranteed to hit a structural JSON token (`{`, `}`, `"`, `:`, `,`), turning standard payloads into invalid JSON strings. Ideal for testing parser `try/catch` error boundaries.
- **Example 3: Intensity 50 (Heavy Scramble)**
  - Response length: 400 characters.
  - Impact: 50 characters are scrambled (~12.5% corruption).
  - Effect: Severely garbles text and structural tokens alike. Useful for stress-testing data sanitizers, buffer parsers, and stream decoders.

> [!NOTE]
> **🎯 For Experts: The Exact Mutation Algorithm**
> 1. When an upstream HTTP response completes, EntropyLab checks if `isMutationEnabled()` is `true` and verifies that the incoming path satisfies the `pathPattern` (if any).
> 2. If a non-zero **Failure %** is configured on the route, it evaluates the probability roll (`Math.random() * 100 < failurePercentage`); otherwise, it mutates on every eligible request.
> 3. The raw response bytes are decoded into a UTF-8 character array `chars[]`.
> 4. A cryptographically sound pseudorandom generator selects an index `idx = rnd.nextInt(chars.length)` for each iteration up to `mutationIntensity`.
> 5. The original character is replaced with a randomly selected printable ASCII character within the range `['!', '~']` (character codes 33 to 122), explicitly ensuring `corrupt != orig`.
> 6. The modified character array is re-encoded to UTF-8 bytes, the HTTP `Content-Length` header is updated to match the re-encoded length, and the response is transmitted to the client.
> 7. The request history entry is tagged with type `FORWARDED_MUTATED`.

## Common Mistakes & Troubleshooting

- **Expecting Mutations on Mocked Endpoints**: If an exact path matches an active Mock, EntropyLab serves the static mock file immediately without forwarding the request. Payload mutation runs only on real upstream responses, so mocks are never mutated.
- **Expecting Mutations on Status Override or Connection Reset**: If a route rule triggers a forced status code (like 500) or drops the socket, no request was ever forwarded to the upstream server, so mutation never occurs.
- **Setting Intensity Greater Than Body Length**: If the response body is shorter than the configured intensity, EntropyLab safely clamps character selections within `[0, chars.length - 1]`. The entire short payload will be scrambled without throwing an `IndexOutOfBoundsException`.
- **Assuming Sub-path Filter is Ignored**: If your client requests an endpoint that does not match the configured sub-path filter, it will receive the unmodified upstream response. Always verify the sub-path pattern if you notice requests passing through uncorrupted.

## Related Reading

- [What Is Chaos Engineering?](./01-what-is-chaos-engineering.md)
- [Latency Injection Reference](./02-latency-injection.md)
- [Status Code Override Reference](./03-status-code-override.md)
- [Connection Reset Reference](./04-connection-reset.md)
- [Sub-Path Filtering & Combining Rules](./05-sub-path-filtering-and-combining-rules.md)
- [How-To Recipe: Test Malformed JSON Resilience](../06-how-to-recipes/09-test-malformed-json-resilience.md)
- [Reading & Using Traffic History](../03-inspector/01-reading-and-using-traffic-history.md)
