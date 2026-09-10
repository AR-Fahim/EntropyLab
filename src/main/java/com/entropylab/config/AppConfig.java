package com.entropylab.config;

import com.entropylab.model.ChaosRule;
import com.entropylab.model.MockMapping;
import com.entropylab.model.RouteMapping;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppConfig {

    private List<RouteMapping> routeMappings = new CopyOnWriteArrayList<>();
    private List<ChaosRule> chaosRules = new CopyOnWriteArrayList<>();
    private List<MockMapping> mockMappings = new CopyOnWriteArrayList<>();
    private int proxyPort = 8080;
    private String themeMode = "LIGHT";

    public AppConfig() {
    }

    public List<RouteMapping> getRouteMappings() {
        return routeMappings;
    }

    public void setRouteMappings(List<RouteMapping> routeMappings) {
        this.routeMappings = routeMappings;
    }

    public List<ChaosRule> getChaosRules() {
        return chaosRules;
    }

    public void setChaosRules(List<ChaosRule> chaosRules) {
        this.chaosRules = chaosRules;
    }

    public List<MockMapping> getMockMappings() {
        return mockMappings;
    }

    public void setMockMappings(List<MockMapping> mockMappings) {
        this.mockMappings = mockMappings;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(String themeMode) {
        this.themeMode = themeMode;
    }
}
