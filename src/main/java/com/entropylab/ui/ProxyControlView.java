package com.entropylab.ui;

import com.entropylab.config.ConfigManager;
import com.entropylab.proxy.ProxyServerManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ProxyControlView extends VBox {

    private final ProxyServerManager proxyServerManager;
    private final TextField portField;
    private final Button toggleButton;
    private final Label statusLabel;

    public ProxyControlView() {
        this(ProxyServerManager.getInstance());
    }

    public ProxyControlView(ProxyServerManager proxyServerManager) {
        this.proxyServerManager = proxyServerManager;

        setPadding(new Insets(16, 20, 16, 20));
        setSpacing(12);
        setAlignment(Pos.TOP_LEFT);

        // Port input row
        Label portLabel = new Label("Port:");
        int initialPort = 8080;
        if (ConfigManager.getInstance().getConfig() != null && ConfigManager.getInstance().getConfig().getProxyPort() > 0) {
            initialPort = ConfigManager.getInstance().getConfig().getProxyPort();
        }
        portField = new TextField(String.valueOf(initialPort));
        portField.setId("proxyPortField");
        portField.setPrefWidth(100);
        portField.setMaxWidth(100);

        HBox portBox = new HBox(10, portLabel, portField);
        portBox.setAlignment(Pos.CENTER_LEFT);

        // Control button
        toggleButton = new Button("Start Proxy");
        toggleButton.setId("proxyToggleButton");
        toggleButton.getStyleClass().addAll("btn", "btn-primary");
        toggleButton.setPrefWidth(120);
        toggleButton.setOnAction(e -> handleToggle());

        // Status row
        Label statusTitle = new Label("Status:");
        statusLabel = new Label("Stopped");
        statusLabel.setId("proxyStatusLabel");
        statusLabel.setStyle("-fx-text-fill: #FE0134; -fx-font-weight: bold;");

        HBox statusBox = new HBox(8, statusTitle, statusLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(portBox, toggleButton, statusBox);

        // Initialize state if already running
        if (proxyServerManager.isRunning()) {
            updateUiState(true, initialPort);
        }
    }

    public void handleToggle() {
        if (proxyServerManager.isRunning()) {
            proxyServerManager.stop();
            updateUiState(false, 0);
        } else {
            int port;
            try {
                port = Integer.parseInt(portField.getText().trim());
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("Port out of range");
                }
            } catch (NumberFormatException ex) {
                showThemedAlert(Alert.AlertType.ERROR, "Invalid Port", "Invalid port number");
                return;
            }

            try {
                proxyServerManager.start(port);
                if (ConfigManager.getInstance().getConfig() != null) {
                    ConfigManager.getInstance().getConfig().setProxyPort(port);
                    ConfigManager.getInstance().save();
                }
                updateUiState(true, port);
            } catch (IOException ex) {
                showThemedAlert(Alert.AlertType.ERROR, "Proxy Error", ex.getMessage() != null ? ex.getMessage() : ex.toString());
                updateUiState(false, 0);
            }
        }
    }

    public void updateUiState(boolean running, int port) {
        if (running) {
            statusLabel.setText("Running on port " + port);
            statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            toggleButton.setText("Stop Proxy");
            portField.setDisable(true);
        } else {
            statusLabel.setText("Stopped");
            statusLabel.setStyle("-fx-text-fill: #FE0134; -fx-font-weight: bold;");
            toggleButton.setText("Start Proxy");
            portField.setDisable(false);
        }
    }

    protected void showThemedAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ThemeManager.getInstance().styleDialog(alert);
        alert.showAndWait();
    }

    public TextField getPortField() {
        return portField;
    }

    public Button getToggleButton() {
        return toggleButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}
