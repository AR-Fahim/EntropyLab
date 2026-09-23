package com.entropylab.proxy;

import com.entropylab.model.ChaosRule;

public class ChaosEngine {

    public static void applyLatencyIfNeeded(ChaosRule rule) {
        if (rule != null && rule.isLatencyEnabled()) {
            try {
                Thread.sleep(rule.getLatencyMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
