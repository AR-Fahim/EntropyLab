package com.entropylab.config;

import com.entropylab.model.ChaosRule;
import com.entropylab.model.MockMapping;
import com.entropylab.model.RouteMapping;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AppConfig config;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public synchronized AppConfig load() {
        Path configFile = AppPaths.getConfigFilePath();
        if (!Files.exists(configFile)) {
            this.config = new AppConfig();
            save();
            return this.config;
        }

        try {
            AppConfig loaded = objectMapper.readValue(configFile.toFile(), AppConfig.class);
            loaded.setRouteMappings(new CopyOnWriteArrayList<>(loaded.getRouteMappings() != null ? loaded.getRouteMappings() : List.of()));
            loaded.setChaosRules(new CopyOnWriteArrayList<>(loaded.getChaosRules() != null ? loaded.getChaosRules() : List.of()));
            loaded.setMockMappings(new CopyOnWriteArrayList<>(loaded.getMockMappings() != null ? loaded.getMockMappings() : List.of()));
            this.config = loaded;
            return this.config;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            this.config = new AppConfig();
            save();
            return this.config;
        }
    }

    public synchronized void save() {
        try {
            if (config != null) {
                Path configFile = AppPaths.getConfigFilePath();
                if (configFile.getParent() != null) {
                    Files.createDirectories(configFile.getParent());
                }
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile.toFile(), config);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public synchronized AppConfig getConfig() {
        return config;
    }

    public synchronized int nextRouteMappingId() {
        return getConfig().getRouteMappings().stream()
            .mapToInt(RouteMapping::getId).max().orElse(0) + 1;
    }

    public synchronized int nextChaosRuleId() {
        return getConfig().getChaosRules().stream()
            .mapToInt(ChaosRule::getId).max().orElse(0) + 1;
    }

    public synchronized int nextMockMappingId() {
        return getConfig().getMockMappings().stream()
            .mapToInt(MockMapping::getId).max().orElse(0) + 1;
    }
}
