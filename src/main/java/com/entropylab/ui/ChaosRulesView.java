package com.entropylab.ui;

import com.entropylab.config.ConfigManager;
import com.entropylab.model.ChaosRule;
import com.entropylab.model.RouteMapping;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChaosRulesView extends VBox {

    private final TableView<RouteMapping> tableView;
    private final ObservableList<RouteMapping> routeList;
    private final Button editRuleButton;

    public ChaosRulesView() {
        this(null);
    }

    public ChaosRulesView(ObservableList<RouteMapping> sharedRouteList) {
        setPadding(new Insets(14, 16, 14, 16));
        setSpacing(10);

        // Toolbar with Edit Chaos Rule Button
        editRuleButton = new Button("Edit Chaos Rule");
        editRuleButton.setId("editChaosRuleButton");
        editRuleButton.getStyleClass().addAll("btn", "btn-secondary");

        HBox toolbar = new HBox(editRuleButton);
        toolbar.getStyleClass().add("view-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        if (sharedRouteList != null) {
            this.routeList = sharedRouteList;
        } else {
            List<RouteMapping> routes = ConfigManager.getInstance().getConfig() != null
                    && ConfigManager.getInstance().getConfig().getRouteMappings() != null
                    ? ConfigManager.getInstance().getConfig().getRouteMappings()
                    : List.of();
            this.routeList = FXCollections.observableArrayList(routes);
        }
        tableView = new TableView<>(routeList);
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        editRuleButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        editRuleButton.setOnAction(e -> {
            RouteMapping selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditChaosRuleDialog(selected);
            }
        });

        TableColumn<RouteMapping, String> localPathCol = new TableColumn<>("Local Path");
        localPathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocalPath()));
        localPathCol.setPrefWidth(250);

        TableColumn<RouteMapping, String> summaryCol = new TableColumn<>("Chaos Summary");
        summaryCol.setCellValueFactory(data -> {
            RouteMapping route = data.getValue();
            List<ChaosRule> rules = ConfigManager.getInstance().getConfig() != null
                    ? ConfigManager.getInstance().getConfig().getChaosRules()
                    : null;
            ChaosRule matched = null;
            if (rules != null && route != null) {
                for (ChaosRule rule : rules) {
                    if (rule.getRouteMappingId() == route.getId()) {
                        matched = rule;
                        break;
                    }
                }
            }

            // Summary text for latency, status override, and connection reset
            String latencySummary = (matched != null && matched.isLatencyEnabled())
                    ? "Latency: " + matched.getLatencyMs() + "ms"
                    : "Latency: OFF";

            String statusSummary = (matched != null && matched.isStatusOverrideEnabled())
                    ? "Status: " + matched.getStatusCode() + " (" + matched.getFailurePercentage() + "%)"
                    : "Status: OFF";

            String resetSummary = (matched != null && matched.isConnectionResetEnabled())
                    ? "Reset: " + matched.getResetPercentage() + "%"
                    : "Reset: OFF";

            String mutationSummary = (matched != null && matched.isMutationEnabled())
                    ? "Mutation: " + matched.getMutationIntensity() + " chars"
                    : "Mutation: OFF";

            String summary = latencySummary + " | " + statusSummary + " | " + resetSummary + " | " + mutationSummary;
            return new SimpleStringProperty(summary);
        });
        summaryCol.setPrefWidth(520);

        tableView.getColumns().addAll(localPathCol, summaryCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        tableView.setRowFactory(tv -> {
            TableRow<RouteMapping> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    showEditChaosRuleDialog(row.getItem());
                }
            });
            return row;
        });

        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                RouteMapping selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEditChaosRuleDialog(selected);
                }
            }
        });

        getChildren().addAll(toolbar, tableView);
    }

    public void showEditChaosRuleDialog(RouteMapping route) {
        if (route == null) {
            return;
        }
        Dialog<ButtonType> dialog = buildChaosRuleDialog(route);
        showDialog(dialog);
    }

    public Dialog<ButtonType> buildChaosRuleDialog(RouteMapping route) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Chaos Rule - " + route.getLocalPath());
        dialog.setHeaderText("Chaos Engineering Configuration for " + route.getLocalPath());

        ThemeManager.getInstance().styleDialog(dialog);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Look up existing rule
        ChaosRule existingRule = null;
        if (ConfigManager.getInstance().getConfig() != null && ConfigManager.getInstance().getConfig().getChaosRules() != null) {
            for (ChaosRule r : ConfigManager.getInstance().getConfig().getChaosRules()) {
                if (r.getRouteMappingId() == route.getId()) {
                    existingRule = r;
                    break;
                }
            }
        }

        // --- Latency Section ---
        CheckBox latencyCheckBox = new CheckBox("Enable Latency");
        latencyCheckBox.setId("chaosLatencyCheckBox");
        latencyCheckBox.setSelected(existingRule != null && existingRule.isLatencyEnabled());

        int initialLatency = (existingRule != null) ? existingRule.getLatencyMs() : 0;
        Spinner<Integer> latencySpinner = new Spinner<>(0, 60000, initialLatency, 100);
        latencySpinner.setId("chaosLatencySpinner");
        latencySpinner.setEditable(true);

        latencySpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (!nv) {
                try {
                    latencySpinner.increment(0);
                } catch (Exception ignored) {
                }
            }
        });

        grid.add(latencyCheckBox, 0, 0, 2, 1);
        grid.add(new Label("Latency (ms):"), 0, 1);
        grid.add(latencySpinner, 1, 1);

        // --- Status Override Section ---
        CheckBox statusOverrideCheckBox = new CheckBox("Enable Status Override");
        statusOverrideCheckBox.setId("chaosStatusOverrideCheckBox");
        statusOverrideCheckBox.setSelected(existingRule != null && existingRule.isStatusOverrideEnabled());

        ComboBox<Integer> statusCodeComboBox = new ComboBox<>();
        statusCodeComboBox.setId("chaosStatusCodeComboBox");
        statusCodeComboBox.getItems().addAll(500, 503, 504);
        int initialStatusCode = (existingRule != null && existingRule.getStatusCode() > 0)
                ? existingRule.getStatusCode()
                : 500;
        statusCodeComboBox.setValue(initialStatusCode);

        int initialFailurePercentage = (existingRule != null) ? existingRule.getFailurePercentage() : 0;
        Spinner<Integer> failurePercentageSpinner = new Spinner<>(0, 100, initialFailurePercentage, 5);
        failurePercentageSpinner.setId("chaosFailurePercentageSpinner");
        failurePercentageSpinner.setEditable(true);

        failurePercentageSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (!nv) {
                try {
                    failurePercentageSpinner.increment(0);
                } catch (Exception ignored) {
                }
            }
        });

        grid.add(statusOverrideCheckBox, 0, 2, 2, 1);
        grid.add(new Label("Status Code:"), 0, 3);
        grid.add(statusCodeComboBox, 1, 3);
        grid.add(new Label("Failure %:"), 0, 4);
        grid.add(failurePercentageSpinner, 1, 4);

        // --- Connection Reset Section ---
        CheckBox connectionResetCheckBox = new CheckBox("Enable Connection Reset");
        connectionResetCheckBox.setId("chaosConnectionResetCheckBox");
        connectionResetCheckBox.setSelected(existingRule != null && existingRule.isConnectionResetEnabled());

        int initialResetPercentage = (existingRule != null) ? existingRule.getResetPercentage() : 0;
        Spinner<Integer> resetPercentageSpinner = new Spinner<>(0, 100, initialResetPercentage, 5);
        resetPercentageSpinner.setId("chaosResetPercentageSpinner");
        resetPercentageSpinner.setEditable(true);

        resetPercentageSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (!nv) {
                try {
                    resetPercentageSpinner.increment(0);
                } catch (Exception ignored) {
                }
            }
        });

        grid.add(connectionResetCheckBox, 0, 5, 2, 1);
        grid.add(new Label("Reset %:"), 0, 6);
        grid.add(resetPercentageSpinner, 1, 6);

        // --- Sub-path filter Section ---
        TextField pathPatternField = new TextField();
        pathPatternField.setId("chaosPathPatternField");
        pathPatternField.setPromptText("/checkout");
        pathPatternField.setText(existingRule != null && existingRule.getPathPattern() != null
                ? existingRule.getPathPattern()
                : "");

        grid.add(new Label("Sub-path filter (optional):"), 0, 7);
        grid.add(pathPatternField, 1, 7);

        // --- Payload Mutation Section ---
        CheckBox mutationCheckBox = new CheckBox("Enable Payload Mutation");
        mutationCheckBox.setId("chaosMutationCheckBox");
        mutationCheckBox.setSelected(existingRule != null && existingRule.isMutationEnabled());

        int initialIntensity = (existingRule != null && existingRule.getMutationIntensity() > 0)
                ? existingRule.getMutationIntensity()
                : 5;
        Spinner<Integer> mutationIntensitySpinner = new Spinner<>(1, 1000, initialIntensity, 1);
        mutationIntensitySpinner.setId("chaosMutationIntensitySpinner");
        mutationIntensitySpinner.setEditable(true);

        mutationIntensitySpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (!nv) {
                try {
                    mutationIntensitySpinner.increment(0);
                } catch (Exception ignored) {
                }
            }
        });

        grid.add(mutationCheckBox, 0, 8, 2, 1);
        grid.add(new Label("Mutation Intensity:"), 0, 9);
        grid.add(mutationIntensitySpinner, 1, 9);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                latencySpinner.increment(0);
            } catch (Exception ignored) {
            }
            try {
                failurePercentageSpinner.increment(0);
            } catch (Exception ignored) {
            }
            try {
                resetPercentageSpinner.increment(0);
            } catch (Exception ignored) {
            }
            try {
                mutationIntensitySpinner.increment(0);
            } catch (Exception ignored) {
            }

            boolean latencyEnabled = latencyCheckBox.isSelected();
            int latencyMs = latencySpinner.getValue() != null ? latencySpinner.getValue() : 0;

            boolean statusOverrideEnabled = statusOverrideCheckBox.isSelected();
            int statusCode = statusCodeComboBox.getValue() != null ? statusCodeComboBox.getValue() : 500;
            int failurePercentage = failurePercentageSpinner.getValue() != null ? failurePercentageSpinner.getValue() : 0;

            boolean connectionResetEnabled = connectionResetCheckBox.isSelected();
            int resetPercentage = resetPercentageSpinner.getValue() != null ? resetPercentageSpinner.getValue() : 0;

            String rawFilter = pathPatternField.getText();
            String pathPattern = (rawFilter != null && !rawFilter.trim().isEmpty())
                    ? rawFilter.trim()
                    : null;

            boolean mutationEnabled = mutationCheckBox.isSelected();
            int mutationIntensity = mutationIntensitySpinner.getValue() != null ? mutationIntensitySpinner.getValue() : 5;

            saveChaosRule(route, latencyEnabled, latencyMs,
                    statusOverrideEnabled, statusCode, failurePercentage,
                    connectionResetEnabled, resetPercentage,
                    pathPattern,
                    mutationEnabled, mutationIntensity);
        });

        return dialog;
    }

    public void saveChaosRule(RouteMapping route, boolean latencyEnabled, int latencyMs) {
        saveChaosRule(route, latencyEnabled, latencyMs, false, 500, 0, false, 0, null, false, 0);
    }

    public void saveChaosRule(RouteMapping route,
                              boolean latencyEnabled, int latencyMs,
                              boolean statusOverrideEnabled, int statusCode, int failurePercentage) {
        saveChaosRule(route, latencyEnabled, latencyMs, statusOverrideEnabled, statusCode, failurePercentage, false, 0, null, false, 0);
    }

    public void saveChaosRule(RouteMapping route,
                              boolean latencyEnabled, int latencyMs,
                              boolean statusOverrideEnabled, int statusCode, int failurePercentage,
                              boolean connectionResetEnabled, int resetPercentage) {
        saveChaosRule(route, latencyEnabled, latencyMs, statusOverrideEnabled, statusCode, failurePercentage, connectionResetEnabled, resetPercentage, null, false, 0);
    }

    public void saveChaosRule(RouteMapping route,
                              boolean latencyEnabled, int latencyMs,
                              boolean statusOverrideEnabled, int statusCode, int failurePercentage,
                              boolean connectionResetEnabled, int resetPercentage,
                              String pathPattern) {
        saveChaosRule(route, latencyEnabled, latencyMs, statusOverrideEnabled, statusCode, failurePercentage, connectionResetEnabled, resetPercentage, pathPattern, false, 0);
    }

    public void saveChaosRule(RouteMapping route,
                              boolean latencyEnabled, int latencyMs,
                              boolean statusOverrideEnabled, int statusCode, int failurePercentage,
                              boolean connectionResetEnabled, int resetPercentage,
                              String pathPattern,
                              boolean mutationEnabled, int mutationIntensity) {
        if (route == null) {
            return;
        }

        List<ChaosRule> rules = ConfigManager.getInstance().getConfig().getChaosRules();
        ChaosRule existing = null;
        for (ChaosRule r : rules) {
            if (r.getRouteMappingId() == route.getId()) {
                existing = r;
                break;
            }
        }

        String normalizedPattern = (pathPattern != null && !pathPattern.trim().isEmpty()) ? pathPattern.trim() : null;

        if (existing != null) {
            existing.setLatencyEnabled(latencyEnabled);
            existing.setLatencyMs(latencyMs);
            existing.setStatusOverrideEnabled(statusOverrideEnabled);
            existing.setStatusCode(statusCode);
            existing.setFailurePercentage(failurePercentage);
            existing.setConnectionResetEnabled(connectionResetEnabled);
            existing.setResetPercentage(resetPercentage);
            existing.setPathPattern(normalizedPattern);
            existing.setMutationEnabled(mutationEnabled);
            existing.setMutationIntensity(mutationIntensity);
        } else if (latencyEnabled || statusOverrideEnabled || connectionResetEnabled || normalizedPattern != null || mutationEnabled) {
            ChaosRule newRule = new ChaosRule();
            newRule.setId(ConfigManager.getInstance().nextChaosRuleId());
            newRule.setRouteMappingId(route.getId());
            newRule.setLatencyEnabled(latencyEnabled);
            newRule.setLatencyMs(latencyMs);
            newRule.setStatusOverrideEnabled(statusOverrideEnabled);
            newRule.setStatusCode(statusCode);
            newRule.setFailurePercentage(failurePercentage);
            newRule.setConnectionResetEnabled(connectionResetEnabled);
            newRule.setResetPercentage(resetPercentage);
            newRule.setPathPattern(normalizedPattern);
            newRule.setMutationEnabled(mutationEnabled);
            newRule.setMutationIntensity(mutationIntensity);

            rules.add(newRule);
        }

        ConfigManager.getInstance().save();
        tableView.refresh();
    }

    public void refresh() {
        if (ConfigManager.getInstance().getConfig() != null) {
            List<RouteMapping> currentConfigRoutes = ConfigManager.getInstance().getConfig().getRouteMappings();
            if (currentConfigRoutes != null && routeList != currentConfigRoutes) {
                for (RouteMapping r : currentConfigRoutes) {
                    if (!routeList.contains(r)) {
                        routeList.add(r);
                    }
                }
                routeList.removeIf(r -> !currentConfigRoutes.contains(r));
            }
        }
        if (tableView != null) {
            tableView.refresh();
        }
    }

    protected void showDialog(Dialog<?> dialog) {
        dialog.showAndWait();
    }

    public TableView<RouteMapping> getTableView() {
        return tableView;
    }

    public ObservableList<RouteMapping> getRouteList() {
        return routeList;
    }

    public Button getEditRuleButton() {
        return editRuleButton;
    }
}
