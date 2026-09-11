package com.entropylab.proxy;

import com.entropylab.config.ConfigManager;
import com.entropylab.db.RequestLogDAO;
import com.entropylab.model.ChaosRule;
import com.entropylab.model.MockMapping;
import com.entropylab.model.RequestLogEntry;
import com.entropylab.model.RouteMapping;
import com.entropylab.util.LogEventBus;
import com.entropylab.util.PathUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProxyRequestHandler implements HttpHandler {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestLogDAO logDao = new RequestLogDAO();

    private static final Set<String> DISALLOWED_HEADERS = Set.of(
            "host", "content-length", "connection", "transfer-encoding", "expect", "upgrade", "accept-encoding"
    );

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();
        String incomingPath = PathUtil.normalize(exchange.getRequestURI().getPath());

        // 1. Manual/Auto Mock exact-path check (NEW, now truly first)
        List<MockMapping> mocks = ConfigManager.getInstance().getConfig().getMockMappings();
        MockMapping matchedMock = null;
        if (mocks != null) {
            for (MockMapping mock : mocks) {
                if (mock.isEnabled() && mock.getLocalPath() != null) {
                    if (incomingPath.equals(PathUtil.normalize(mock.getLocalPath()))) {
                        matchedMock = mock;
                        break;
                    }
                }
            }
        }

        if (matchedMock != null) {
            String method = exchange.getRequestMethod();
            byte[] bodyBytes = new byte[0];
            try (InputStream is = exchange.getRequestBody()) {
                bodyBytes = is.readAllBytes();
            } catch (Exception ignored) {
            }

            String fileContent;
            try {
                fileContent = Files.readString(Path.of(matchedMock.getFilePath()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                String errorMsg = "Internal Server Error: Failed to read mock file: " + e.getMessage();
                byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(500, errorBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorBytes);
                }
                exchange.close();
                return;
            }

            byte[] responseBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            exchange.close();

            try {
                long durationMs = System.currentTimeMillis() - startTime;
                RequestLogEntry entry = new RequestLogEntry();
                entry.setTimestamp(startTime);
                entry.setDurationMs(durationMs);
                entry.setMethod(method);
                entry.setPath(incomingPath);
                entry.setTargetUrl(null);
                entry.setStatusCode(200);
                entry.setRequestHeaders(objectMapper.writeValueAsString(exchange.getRequestHeaders()));
                entry.setResponseHeaders("{\"Content-Type\":[\"application/json\"]}");
                entry.setRequestBody(new String(bodyBytes, StandardCharsets.UTF_8));
                entry.setResponseBody(fileContent);
                entry.setType("MOCKED");

                logDao.insertAsync(entry, saved -> LogEventBus.getInstance().publish(saved));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return;
        }

        // 2. Route mapping match
        List<RouteMapping> routes = ConfigManager.getInstance().getConfig().getRouteMappings();
        RouteMapping bestMatch = null;
        int longestLength = -1;

        if (routes != null) {
            for (RouteMapping route : routes) {
                if (route.isEnabled() && route.getLocalPath() != null) {
                    String normalizedLocalPath = PathUtil.normalize(route.getLocalPath());
                    if (incomingPath.equals(normalizedLocalPath) || incomingPath.startsWith(normalizedLocalPath + "/")) {
                        if (normalizedLocalPath.length() > longestLength) {
                            longestLength = normalizedLocalPath.length();
                            bestMatch = route;
                        }
                    }
                }
            }
        }

        if (bestMatch == null) {
            byte[] response = "No route configured for this path".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
            return;
        }

        try {
            // Look up ChaosRule for matched route
            ChaosRule chaosRule = null;
            List<ChaosRule> chaosRules = ConfigManager.getInstance().getConfig().getChaosRules();
            if (chaosRules != null) {
                for (ChaosRule r : chaosRules) {
                    if (r.getRouteMappingId() == bestMatch.getId()) {
                        chaosRule = r;
                        break;
                    }
                }
            }
            // 3. Latency injection (M22) — first
            if (matchesSubPathFilter(chaosRule, bestMatch, incomingPath)) {
                ChaosEngine.applyLatencyIfNeeded(chaosRule);
            }

            // 4. Connection reset check (NEW) — second
            if (chaosRule != null && chaosRule.isConnectionResetEnabled()
                    && matchesSubPathFilter(chaosRule, bestMatch, incomingPath)
                    && (Math.random() * 100 < chaosRule.getResetPercentage())) {
                String method = exchange.getRequestMethod();
                String reqHeadersJson = "{}";
                try {
                    reqHeadersJson = objectMapper.writeValueAsString(exchange.getRequestHeaders());
                } catch (Exception ignored) {
                }

                // com.sun.net.httpserver has no raw socket RST control, this is the closest achievable simulation
                exchange.close();

                // Best-effort log AFTER closing: type="CHAOS_RESET", statusCode=0, responseBody="", responseHeaders="{}"
                try {
                    long durationMs = System.currentTimeMillis() - startTime;
                    RequestLogEntry entry = new RequestLogEntry();
                    entry.setTimestamp(startTime);
                    entry.setDurationMs(durationMs);
                    entry.setMethod(method);
                    entry.setPath(incomingPath);
                    entry.setTargetUrl(null);
                    entry.setStatusCode(0);
                    entry.setRequestHeaders(reqHeadersJson);
                    entry.setResponseHeaders("{}");
                    entry.setRequestBody("");
                    entry.setResponseBody("");
                    entry.setType("CHAOS_RESET");

                    logDao.insertAsync(entry, saved -> LogEventBus.getInstance().publish(saved));
                } catch (Exception ignored) {
                }
                return;
            }

            // 5. Status override check (M24) — third, only if step 4 didn't return
            if (chaosRule != null && chaosRule.isStatusOverrideEnabled()
                    && matchesSubPathFilter(chaosRule, bestMatch, incomingPath)
                    && (Math.random() * 100 < chaosRule.getFailurePercentage())) {
                String method = exchange.getRequestMethod();
                byte[] bodyBytes;
                try (InputStream is = exchange.getRequestBody()) {
                    bodyBytes = is.readAllBytes();
                }

                int statusCode = chaosRule.getStatusCode();
                String responseJson = "{\"error\": \"Chaos Engine forced status " + statusCode + "\"}";
                byte[] responseBytes = responseJson.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(statusCode, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                exchange.close();

                try {
                    long durationMs = System.currentTimeMillis() - startTime;
                    RequestLogEntry entry = new RequestLogEntry();
                    entry.setTimestamp(startTime);
                    entry.setDurationMs(durationMs);
                    entry.setMethod(method);
                    entry.setPath(incomingPath);
                    entry.setTargetUrl(null);
                    entry.setStatusCode(statusCode);
                    entry.setRequestHeaders(objectMapper.writeValueAsString(exchange.getRequestHeaders()));
                    entry.setResponseHeaders("{\"Content-Type\":[\"application/json\"]}");
                    entry.setRequestBody(new String(bodyBytes, StandardCharsets.UTF_8));
                    entry.setResponseBody(responseJson);
                    entry.setType("CHAOS_STATUS");

                    logDao.insertAsync(entry, saved -> LogEventBus.getInstance().publish(saved));
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                return;
            }

            // 1. Remainder path computation
            String normalizedLocal = PathUtil.normalize(bestMatch.getLocalPath());
            String remainderPath = incomingPath.substring(normalizedLocal.length());
            if (remainderPath.isEmpty()) {
                remainderPath = "/";
            } else if (!remainderPath.startsWith("/")) {
                remainderPath = "/" + remainderPath;
            }

            // 2. Target URI construction
            String targetBase = bestMatch.getTargetBaseUrl() != null ? bestMatch.getTargetBaseUrl() : "";
            while (targetBase.length() > 1 && targetBase.endsWith("/")) {
                targetBase = targetBase.substring(0, targetBase.length() - 1);
            }
            String rawQuery = exchange.getRequestURI().getRawQuery();
            String targetUri = targetBase + remainderPath + (rawQuery != null && !rawQuery.isEmpty() ? "?" + rawQuery : "");

            // 3. Read method and full request body
            String method = exchange.getRequestMethod();
            byte[] bodyBytes;
            try (InputStream is = exchange.getRequestBody()) {
                bodyBytes = is.readAllBytes();
            }

            // 4. Build outgoing headers
            // The accept-encoding exclusion keeps responses uncompressed/readable for logging, Inspector, and Auto-Mock
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(targetUri))
                    .method(method, bodyBytes.length == 0
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofByteArray(bodyBytes));

            for (Map.Entry<String, List<String>> headerEntry : exchange.getRequestHeaders().entrySet()) {
                String key = headerEntry.getKey();
                if (key != null && !DISALLOWED_HEADERS.contains(key.toLowerCase())) {
                    for (String val : headerEntry.getValue()) {
                        requestBuilder.header(key, val);
                    }
                }
            }

            // 5. Send request via HttpClient
            HttpRequest request = requestBuilder.build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] responseBytes = response.body() != null ? response.body() : new byte[0];

            // 6. Dumb Payload Mutation check (M37)
            boolean mutated = false;
            if (chaosRule != null && chaosRule.isMutationEnabled()
                    && matchesSubPathFilter(chaosRule, bestMatch, incomingPath)
                    && (chaosRule.getFailurePercentage() > 0 ? (Math.random() * 100 < chaosRule.getFailurePercentage()) : true)) {
                int intensity = chaosRule.getMutationIntensity() > 0 ? chaosRule.getMutationIntensity() : 5;
                if (responseBytes.length > 0) {
                    String bodyStr = new String(responseBytes, StandardCharsets.UTF_8);
                    if (!bodyStr.isEmpty()) {
                        char[] chars = bodyStr.toCharArray();
                        java.util.Random rnd = new java.util.Random();
                        for (int i = 0; i < intensity; i++) {
                            int idx = rnd.nextInt(chars.length);
                            char orig = chars[idx];
                            char corrupt;
                            do {
                                corrupt = (char) ('!' + rnd.nextInt(90));
                            } while (corrupt == orig);
                            chars[idx] = corrupt;
                        }
                        String mutatedBody = new String(chars);
                        responseBytes = mutatedBody.getBytes(StandardCharsets.UTF_8);
                        mutated = true;
                    }
                }
            }

            // Copy response back
            for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
                String key = entry.getKey();
                if (key != null && !key.startsWith(":")) {
                    String lowerKey = key.toLowerCase();
                    if (!lowerKey.equals("transfer-encoding") && !lowerKey.equals("content-length")) {
                        for (String val : entry.getValue()) {
                            exchange.getResponseHeaders().add(key, val);
                        }
                    }
                }
            }

            exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            exchange.close();

            // Asynchronously log forwarded traffic without delaying client response
            try {
                long durationMs = System.currentTimeMillis() - startTime;
                RequestLogEntry entry = new RequestLogEntry();
                entry.setTimestamp(startTime);
                entry.setDurationMs(durationMs);
                entry.setMethod(method);
                entry.setPath(incomingPath);
                entry.setTargetUrl(targetUri);
                entry.setStatusCode(response.statusCode());
                entry.setRequestHeaders(objectMapper.writeValueAsString(exchange.getRequestHeaders()));
                entry.setResponseHeaders(objectMapper.writeValueAsString(response.headers().map()));
                entry.setRequestBody(new String(bodyBytes, StandardCharsets.UTF_8));
                entry.setResponseBody(new String(responseBytes, StandardCharsets.UTF_8));
                entry.setType(mutated ? "FORWARDED_MUTATED" : "FORWARDED");

                logDao.insertAsync(entry, saved -> LogEventBus.getInstance().publish(saved));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        } catch (Exception e) {
            try {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
                byte[] errorResponse = ("Bad Gateway: " + errorMsg).getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(502, errorResponse.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorResponse);
                }
            } catch (IOException ignored) {
            } finally {
                exchange.close();
            }
        }
    }

    private static boolean matchesSubPathFilter(ChaosRule rule, RouteMapping mapping, String incomingPath) {
        if (rule == null || rule.getPathPattern() == null || rule.getPathPattern().isBlank()) {
            return true;
        }
        if (mapping == null || mapping.getLocalPath() == null || incomingPath == null) {
            return false;
        }
        String normalizedLocal = PathUtil.normalize(mapping.getLocalPath());
        String pattern = rule.getPathPattern().trim();
        String expectedPrefix;
        if (normalizedLocal.equals("/")) {
            expectedPrefix = pattern.startsWith("/") ? pattern : "/" + pattern;
        } else {
            expectedPrefix = pattern.startsWith("/") ? normalizedLocal + pattern : normalizedLocal + "/" + pattern;
        }
        return incomingPath.startsWith(expectedPrefix) || incomingPath.startsWith(normalizedLocal + pattern);
    }
}
