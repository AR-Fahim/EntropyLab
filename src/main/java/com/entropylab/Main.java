package com.entropylab;

import com.entropylab.config.AppConfig;
import com.entropylab.config.AppPaths;
import com.entropylab.config.ConfigManager;
import com.entropylab.ui.CustomTitleBar;
import com.entropylab.ui.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppPaths.ensureDirectoriesExist();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1000, 700);
        ThemeManager.getInstance().registerScene(scene);

        AppConfig config = ConfigManager.getInstance().load();
        if (config != null && config.getThemeMode() != null) {
            try {
                ThemeManager.Theme theme = ThemeManager.Theme.valueOf(config.getThemeMode().toUpperCase());
                ThemeManager.getInstance().setThemeWithoutSaving(theme);
                ThemeManager.getInstance().applyTheme(scene);
            } catch (IllegalArgumentException e) {
                // Keep default theme if invalid
            }
        }

        // Main Shell: BorderPane
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("window-root");

        // Custom Title Bar with branding logo, app title, day-night toggle icon, and window controls
        CustomTitleBar customTitleBar = new CustomTitleBar(primaryStage);
        borderPane.setTop(customTitleBar);

        // Center region: TabPane with tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab proxyControlTab = new Tab("Proxy Control");
        proxyControlTab.setContent(new com.entropylab.ui.ProxyControlView());
        tabPane.getTabs().add(proxyControlTab);

        Tab routesTab = new Tab("Routes");
        routesTab.setContent(new com.entropylab.ui.RouteMappingView());
        tabPane.getTabs().add(routesTab);

        Tab chaosTab = new Tab("Chaos Rules");
        chaosTab.setContent(new com.entropylab.ui.ChaosRulesView());
        tabPane.getTabs().add(chaosTab);

        com.entropylab.ui.MockManagementView mockManagementView = new com.entropylab.ui.MockManagementView();
        com.entropylab.ui.InspectorView inspectorView = new com.entropylab.ui.InspectorView(mockManagementView);

        Tab inspectorTab = new Tab("Inspector");
        inspectorTab.setContent(inspectorView);
        tabPane.getTabs().add(inspectorTab);

        Tab mocksTab = new Tab("Mocks");
        mocksTab.setContent(mockManagementView);
        tabPane.getTabs().add(mocksTab);

        Tab analyticsTab = new Tab("Analytics");
        analyticsTab.setContent(new com.entropylab.ui.AnalyticsView());
        tabPane.getTabs().add(analyticsTab);

        Tab aboutTab = new Tab("About");
        aboutTab.setContent(new com.entropylab.ui.AboutView());
        tabPane.getTabs().add(aboutTab);

        borderPane.setCenter(tabPane);

        // REUSE exact same Scene from M1
        scene.setRoot(borderPane);
        ThemeManager.getInstance().applyTheme(scene);

        // Set branding app logo for OS taskbar across multiple resolutions
        primaryStage.getIcons().setAll(
                com.entropylab.util.AppBrandUtil.loadAppLogo(16, 16),
                com.entropylab.util.AppBrandUtil.loadAppLogo(32, 32),
                com.entropylab.util.AppBrandUtil.loadAppLogo(48, 48),
                com.entropylab.util.AppBrandUtil.loadAppLogo(64, 64),
                com.entropylab.util.AppBrandUtil.loadAppLogo(128, 128),
                com.entropylab.util.AppBrandUtil.loadAppLogo(256, 256)
        );

        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        primaryStage.setTitle("EntropyLab");
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(550);
        primaryStage.setScene(scene);

        // Enable edge resizing for undecorated window
        com.entropylab.ui.WindowResizeHelper.install(primaryStage, 10, 850, 550);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
