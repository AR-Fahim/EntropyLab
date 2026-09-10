package com.entropylab.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppPaths {

    private AppPaths() {
    }

    public static Path getAppDataDir() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            System.err.println("WARNING: APPDATA environment variable is missing or blank. Falling back to ~/.entropylab");
            return Paths.get(System.getProperty("user.home"), ".entropylab");
        }
        return Paths.get(appData, "EntropyLab");
    }

    public static Path getConfigFilePath() {
        return getAppDataDir().resolve("config.json");
    }

    public static Path getDatabaseFilePath() {
        return getAppDataDir().resolve("entropylab.db");
    }

    public static Path getMocksDir() {
        return getAppDataDir().resolve("mocks");
    }

    public static void ensureDirectoriesExist() {
        try {
            Files.createDirectories(getAppDataDir());
            Files.createDirectories(getMocksDir());
        } catch (IOException e) {
            System.err.println("Failed to create application directories: " + e.getMessage());
            throw new RuntimeException("Failed to initialize application directories", e);
        }
    }
}
