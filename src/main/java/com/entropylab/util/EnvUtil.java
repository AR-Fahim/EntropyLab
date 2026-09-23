package com.entropylab.util;

import com.entropylab.config.AppPaths;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EnvUtil {

    private static final Map<String, String> ENV_CACHE = new HashMap<>();
    private static boolean loaded = false;

    private EnvUtil() {
    }

    private static synchronized void loadIfNeeded() {
        if (loaded) {
            return;
        }
        loaded = true;

        // 1. Current working directory .env
        loadFile(Paths.get(".env"));

        // 2. AppData directory .env
        try {
            loadFile(AppPaths.getAppDataDir().resolve(".env"));
        } catch (Exception ignored) {
        }
    }

    private static void loadFile(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        if (val.length() >= 2) {
                            val = val.substring(1, val.length() - 1);
                        }
                    }
                    if (!ENV_CACHE.containsKey(key)) {
                        ENV_CACHE.put(key, val);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static String get(String key, String defaultValue) {
        loadIfNeeded();
        if (ENV_CACHE.containsKey(key)) {
            String v = ENV_CACHE.get(key);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        String sysEnv = System.getenv(key);
        if (sysEnv != null && !sysEnv.isBlank()) {
            return sysEnv;
        }
        return defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }
}
