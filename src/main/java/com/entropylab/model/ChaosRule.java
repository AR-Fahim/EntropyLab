package com.entropylab.model;

public class ChaosRule {

    private int id;
    private int routeMappingId;
    private boolean latencyEnabled;
    private int latencyMs;
    private boolean statusOverrideEnabled;
    private int statusCode;
    private int failurePercentage;
    private boolean connectionResetEnabled;
    private int resetPercentage;
    private String pathPattern;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRouteMappingId() {
        return routeMappingId;
    }

    public void setRouteMappingId(int routeMappingId) {
        this.routeMappingId = routeMappingId;
    }

    public boolean isLatencyEnabled() {
        return latencyEnabled;
    }

    public void setLatencyEnabled(boolean latencyEnabled) {
        this.latencyEnabled = latencyEnabled;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public boolean isStatusOverrideEnabled() {
        return statusOverrideEnabled;
    }

    public void setStatusOverrideEnabled(boolean statusOverrideEnabled) {
        this.statusOverrideEnabled = statusOverrideEnabled;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getFailurePercentage() {
        return failurePercentage;
    }

    public void setFailurePercentage(int failurePercentage) {
        this.failurePercentage = failurePercentage;
    }

    public boolean isConnectionResetEnabled() {
        return connectionResetEnabled;
    }

    public void setConnectionResetEnabled(boolean connectionResetEnabled) {
        this.connectionResetEnabled = connectionResetEnabled;
    }

    public int getResetPercentage() {
        return resetPercentage;
    }

    public void setResetPercentage(int resetPercentage) {
        this.resetPercentage = resetPercentage;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    private boolean mutationEnabled;
    private int mutationIntensity;

    public boolean isMutationEnabled() {
        return mutationEnabled;
    }

    public void setMutationEnabled(boolean mutationEnabled) {
        this.mutationEnabled = mutationEnabled;
    }

    public int getMutationIntensity() {
        return mutationIntensity;
    }

    public void setMutationIntensity(int mutationIntensity) {
        this.mutationIntensity = mutationIntensity;
    }
}
