package com.entropylab.util;

import com.entropylab.model.RequestLogEntry;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LogEventBus {

    private static final LogEventBus INSTANCE = new LogEventBus();

    private final List<Consumer<RequestLogEntry>> listeners = new CopyOnWriteArrayList<>();

    private LogEventBus() {
    }

    public static LogEventBus getInstance() {
        return INSTANCE;
    }

    public void registerListener(Consumer<RequestLogEntry> listener) {
        listeners.add(listener);
    }

    public void publish(RequestLogEntry entry) {
        for (Consumer<RequestLogEntry> listener : listeners) {
            try {
                Platform.runLater(() -> listener.accept(entry));
            } catch (IllegalStateException e) {
                // If JavaFX toolkit is not initialized (e.g., in unit or console tests)
                listener.accept(entry);
            }
        }
    }
}
