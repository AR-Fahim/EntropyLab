package com.entropylab.ui;

import com.entropylab.config.ConfigManager;
import com.entropylab.model.RouteMapping;
import com.entropylab.util.PathUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class RouteMappingView extends VBox {

    private final TableView<RouteMapping> tableView;
    private final ObservableList<RouteMapping> routeList;
    private final Button addRouteButton;
    private final Button editRouteButton;
    private final Button deleteRouteButton;

    public RouteMappingView() {
        setPadding(new Insets(16));
        setSpacing(12);

        List<RouteMapping> existingRoutes = ConfigManager.getInstance().getConfig() != null
                && ConfigManager.getInstance().getConfig().getRouteMappings() != null
                ? ConfigManager.getInstance().getConfig().getRouteMappings()
                : List.of();

        routeList = FXCollections.observableArrayList(existingRoutes);
        tableView = new TableView<>(routeList);
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Toolbar with Add, Edit, Delete Buttons
        addRouteButton = new Button("Add Route");
        addRouteButton.setId("addRouteButton");
        addRouteButton.setOnAction(e -> showAddRouteDialog());

        editRouteButton = new Button("Edit Route");
        editRouteButton.setId("editRouteButton");
        editRouteButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        editRouteButton.setOnAction(e -> {
            RouteMapping selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditRouteDialog(selected);
            }
        });

        deleteRouteButton = new Button("Delete Route");
        deleteRouteButton.setId("deleteRouteButton");
        deleteRouteButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        deleteRouteButton.setOnAction(e -> {
            RouteMapping selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                confirmAndDeleteRoute(selected);
            }
        });

        HBox toolbar = new HBox(8, addRouteButton, editRouteButton, deleteRouteButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TableColumn<RouteMapping, String> localPathCol = new TableColumn<>("Local Path");
        localPathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocalPath()));
        localPathCol.setEditable(false);

        TableColumn<RouteMapping, String> targetCol = new TableColumn<>("Target Base URL");
        targetCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTargetBaseUrl()));
        targetCol.setEditable(false);

        TableColumn<RouteMapping, Boolean> enabledCol = new TableColumn<>("Enabled");
        enabledCol.setCellValueFactory(param -> {
            RouteMapping mapping = param.getValue();
            SimpleBooleanProperty prop = new SimpleBooleanProperty(mapping.isEnabled());
            prop.addListener((obs, oldVal, newVal) -> {
                mapping.setEnabled(newVal);
                ConfigManager.getInstance().save();
            });
            return prop;
        });
        enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
        enabledCol.setEditable(true);
        enabledCol.setPrefWidth(80);
        enabledCol.setMaxWidth(100);

        tableView.getColumns().addAll(localPathCol, targetCol, enabledCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Row factory for ContextMenu (Edit, Delete)
        tableView.setRowFactory(tv -> {
            TableRow<RouteMapping> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(e -> {
                RouteMapping item = row.getItem();
                if (item != null) {
                    showEditRouteDialog(item);
                }
            });
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> {
                RouteMapping item = row.getItem();
                if (item != null) {
                    confirmAndDeleteRoute(item);
                }
            });
            contextMenu.getItems().addAll(editItem, deleteItem);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        getChildren().addAll(toolbar, tableView);
    }

    public void showAddRouteDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Route");
        dialog.setHeaderText("Add New Route Mapping");

        ThemeManager.getInstance().styleDialog(dialog);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField localPathField = new TextField();
        localPathField.setId("dialogLocalPathField");
        localPathField.setPromptText("/api/example");

        TextField targetUrlField = new TextField();
        targetUrlField.setId("dialogTargetUrlField");
        targetUrlField.setPromptText("https://api.example.com");

        grid.add(new Label("Local Path:"), 0, 0);
        grid.add(localPathField, 1, 0);
        grid.add(new Label("Target Base URL:"), 0, 1);
        grid.add(targetUrlField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            String localPath = localPathField.getText();
            String targetBaseUrl = targetUrlField.getText();

            boolean success = addRoute(localPath, targetBaseUrl);
            if (!success) {
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    public boolean addRoute(String localPath, String targetBaseUrl) {
        if (localPath == null || localPath.trim().isEmpty() || targetBaseUrl == null || targetBaseUrl.trim().isEmpty()) {
            showThemedAlert(Alert.AlertType.WARNING, "Validation Error", "Both fields are required");
            return false;
        }

        String normalizedLocalPath = PathUtil.normalize(localPath.trim());
        boolean duplicate = ConfigManager.getInstance().getConfig().getRouteMappings().stream()
                .anyMatch(r -> r.getLocalPath() != null && PathUtil.normalize(r.getLocalPath()).equals(normalizedLocalPath));

        if (duplicate) {
            showThemedAlert(Alert.AlertType.WARNING, "Duplicate Route", "A route for this path already exists");
            return false;
        }

        RouteMapping mapping = new RouteMapping();
        mapping.setId(ConfigManager.getInstance().nextRouteMappingId());
        mapping.setLocalPath(normalizedLocalPath);
        mapping.setTargetBaseUrl(targetBaseUrl.trim());
        mapping.setEnabled(true);

        ConfigManager.getInstance().getConfig().getRouteMappings().add(mapping);
        routeList.add(mapping);
        ConfigManager.getInstance().save();
        return true;
    }

    public void showEditRouteDialog(RouteMapping mapping) {
        if (mapping == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Route");
        dialog.setHeaderText("Edit Route Mapping");

        ThemeManager.getInstance().styleDialog(dialog);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField localPathField = new TextField(mapping.getLocalPath());
        localPathField.setId("dialogEditLocalPathField");

        TextField targetUrlField = new TextField(mapping.getTargetBaseUrl());
        targetUrlField.setId("dialogEditTargetUrlField");

        grid.add(new Label("Local Path:"), 0, 0);
        grid.add(localPathField, 1, 0);
        grid.add(new Label("Target Base URL:"), 0, 1);
        grid.add(targetUrlField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            boolean success = updateRoute(mapping, localPathField.getText(), targetUrlField.getText());
            if (!success) {
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    public boolean updateRoute(RouteMapping mapping, String localPath, String targetBaseUrl) {
        if (mapping == null) {
            return false;
        }
        if (localPath == null || localPath.trim().isEmpty() || targetBaseUrl == null || targetBaseUrl.trim().isEmpty()) {
            showThemedAlert(Alert.AlertType.WARNING, "Validation Error", "Both fields are required");
            return false;
        }

        String normalizedLocalPath = PathUtil.normalize(localPath.trim());
        boolean duplicate = ConfigManager.getInstance().getConfig().getRouteMappings().stream()
                .anyMatch(r -> r.getId() != mapping.getId() && r.getLocalPath() != null
                        && PathUtil.normalize(r.getLocalPath()).equals(normalizedLocalPath));

        if (duplicate) {
            showThemedAlert(Alert.AlertType.WARNING, "Duplicate Route", "A route for this path already exists");
            return false;
        }

        mapping.setLocalPath(normalizedLocalPath);
        mapping.setTargetBaseUrl(targetBaseUrl.trim());
        tableView.refresh();
        ConfigManager.getInstance().save();
        return true;
    }

    public boolean confirmAndDeleteRoute(RouteMapping mapping) {
        if (mapping == null) {
            return false;
        }
        boolean confirmed = showThemedConfirmation("Confirm Deletion", "Delete route '" + mapping.getLocalPath() + "'?");
        if (confirmed) {
            deleteRoute(mapping);
            return true;
        }
        return false;
    }

    public void deleteRoute(RouteMapping mapping) {
        if (mapping == null) {
            return;
        }
        ConfigManager.getInstance().getConfig().getRouteMappings().remove(mapping);
        routeList.remove(mapping);
        ConfigManager.getInstance().save();
    }

    protected void showThemedAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ThemeManager.getInstance().styleDialog(alert);
        alert.showAndWait();
    }

    protected boolean showThemedConfirmation(String title, String content) {
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, yesButton, noButton);
        alert.setTitle(title);
        alert.setHeaderText(null);
        ThemeManager.getInstance().styleDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    public TableView<RouteMapping> getTableView() {
        return tableView;
    }

    public ObservableList<RouteMapping> getRouteList() {
        return routeList;
    }

    public Button getAddRouteButton() {
        return addRouteButton;
    }

    public Button getEditRouteButton() {
        return editRouteButton;
    }

    public Button getDeleteRouteButton() {
        return deleteRouteButton;
    }
}
