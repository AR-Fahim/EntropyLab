package com.entropylab.ui;

import com.entropylab.config.ConfigManager;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    public enum Theme {
        LIGHT,
        DARK
    }

    @FunctionalInterface
    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    private static final ThemeManager INSTANCE = new ThemeManager();

    private Theme currentTheme = Theme.LIGHT;
    private final List<Scene> trackedScenes = new ArrayList<>();
    private final List<ThemeChangeListener> themeListeners = new ArrayList<>();

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setThemeWithoutSaving(Theme theme) {
        if (theme != null) {
            this.currentTheme = theme;
            notifyListeners();
        }
    }

    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !themeListeners.contains(listener)) {
            themeListeners.add(listener);
        }
    }

    public void removeThemeChangeListener(ThemeChangeListener listener) {
        themeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (ThemeChangeListener listener : new ArrayList<>(themeListeners)) {
            try {
                listener.onThemeChanged(currentTheme);
            } catch (Exception ignored) {
            }
        }
    }

    public void registerScene(Scene scene) {
        if (scene != null && !trackedScenes.contains(scene)) {
            trackedScenes.add(scene);
        }
        if (scene != null) {
            applyTheme(scene);
        }
    }

    public void applyTheme(Scene scene) {
        if (scene != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                getClass().getResource("/com/entropylab/styles/" +
                (currentTheme == Theme.LIGHT ? "light-theme.css" : "dark-theme.css"))
                .toExternalForm()
            );
        }
    }

    public void toggleTheme() {
        currentTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
        for (Scene scene : trackedScenes) {
            applyTheme(scene);
        }
        notifyListeners();
        if (ConfigManager.getInstance().getConfig() != null) {
            ConfigManager.getInstance().getConfig().setThemeMode(currentTheme.name());
            ConfigManager.getInstance().save();
        }
    }

    public void styleDialogPane(DialogPane pane) {
        if (pane != null) {
            pane.getStylesheets().clear();
            pane.getStylesheets().add(
                getClass().getResource("/com/entropylab/styles/" +
                (currentTheme == Theme.LIGHT ? "light-theme.css" : "dark-theme.css"))
                .toExternalForm()
            );
        }
    }

    public <R> Dialog<R> styleDialog(Dialog<R> dialog) {
        return SubWindowHelper.styleSubWindow(dialog);
    }
}
