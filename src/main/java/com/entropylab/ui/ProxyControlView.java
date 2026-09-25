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
        setSpacing(16);
        setAlignment(Pos.TOP_LEFT);
        setMaxWidth(Double.MAX_VALUE);
        setFillWidth(true);

        int initialPort = 8080;
        if (ConfigManager.getInstance().getConfig() != null && ConfigManager.getInstance().getConfig().getProxyPort() > 0) {
            initialPort = ConfigManager.getInstance().getConfig().getProxyPort();
        }

        // Card 1: Server Status & Control Card
        VBox serverCard = new VBox(14);
        serverCard.getStyleClass().add("card-pane");
        serverCard.setMaxWidth(Double.MAX_VALUE);

        Label cardTitle = new Label("Proxy Server Control");
        cardTitle.getStyleClass().add("card-title");

        Label cardSubtitle = new Label("Intercept and route HTTP traffic through local port with active chaos simulation");
        cardSubtitle.getStyleClass().add("secondary");

        // Port input
        Label portLabel = new Label("Port:");
        portField = new TextField(String.valueOf(initialPort));
        portField.setId("proxyPortField");
        portField.setPrefWidth(100);
        portField.setMaxWidth(100);

        // Control button
        toggleButton = new Button("Start Proxy");
        toggleButton.setId("proxyToggleButton");
        toggleButton.getStyleClass().addAll("btn", "btn-primary");
        toggleButton.setPrefWidth(120);
        toggleButton.setOnAction(e -> handleToggle());

        // Status
        Label statusTitle = new Label("Status:");
        statusLabel = new Label("Stopped");
        statusLabel.setId("proxyStatusLabel");
        statusLabel.setStyle("-fx-text-fill: #FE0134; -fx-font-weight: bold;");

        HBox controlRow = new HBox(12, portLabel, portField, toggleButton, statusTitle, statusLabel);
        controlRow.setAlignment(Pos.CENTER_LEFT);

        serverCard.getChildren().addAll(cardTitle, cardSubtitle, controlRow);

        // Card 2: Client Routing & Integration Guide
        VBox guideCard = new VBox(12);
        guideCard.getStyleClass().add("card-pane");
        guideCard.setMaxWidth(Double.MAX_VALUE);

        Label guideTitle = new Label("Client Routing & Integration");
        guideTitle.getStyleClass().add("card-title");

        Label guideDesc = new Label("Direct your frontend, microservices, or API testing clients to EntropyLab using any of these methods:");
        guideDesc.getStyleClass().add("secondary");

        VBox examplesBox = new VBox(10);
        examplesBox.getChildren().addAll(
                createGuideRow("Target URL Direct", "http://127.0.0.1:" + initialPort + "/<localPath>", "Send HTTP requests directly to mapped path prefixes"),
                createGuideRow("System / CLI Proxy", "export HTTP_PROXY=http://127.0.0.1:" + initialPort, "Forward global CLI tools, curl, and SDK requests"),
                createGuideRow("Live Inspection", "Switch to the Inspector tab", "View streaming live requests, latencies, and payload mutations in real-time")
        );

        guideCard.getChildren().addAll(guideTitle, guideDesc, examplesBox);

        getChildren().addAll(serverCard, guideCard);

        // Initialize state if already running
        if (proxyServerManager.isRunning()) {
            updateUiState(true, initialPort);
        }
    }

    private HBox createGuideRow(String labelText, String codeText, String descText) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText + ":");
        label.setStyle("-fx-font-weight: bold; -fx-min-width: 140px;");

        Label codeLabel = new Label(codeText);
        codeLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-background-color: rgba(254, 1, 52, 0.08); -fx-padding: 3px 8px; -fx-background-radius: 4px;");

        Label desc = new Label(descText);
        desc.getStyleClass().add("secondary");

        row.getChildren().addAll(label, codeLabel, desc);
        return row;
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
            toggleButton.getStyleClass().remove("btn-primary");
            if (!toggleButton.getStyleClass().contains("btn-danger")) {
                toggleButton.getStyleClass().add("btn-danger");
            }
            portField.setDisable(true);
        } else {
            statusLabel.setText("Stopped");
            statusLabel.setStyle("-fx-text-fill: #FE0134; -fx-font-weight: bold;");
            toggleButton.setText("Start Proxy");
            toggleButton.getStyleClass().remove("btn-danger");
            if (!toggleButton.getStyleClass().contains("btn-primary")) {
                toggleButton.getStyleClass().add("btn-primary");
            }
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
