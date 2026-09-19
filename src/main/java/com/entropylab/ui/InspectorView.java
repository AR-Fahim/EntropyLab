package com.entropylab.ui;

import com.entropylab.config.AppPaths;
import com.entropylab.config.ConfigManager;
import com.entropylab.db.RequestLogDAO;
import com.entropylab.model.MockMapping;
import com.entropylab.model.RequestLogEntry;
import com.entropylab.util.LogEventBus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class InspectorView extends VBox {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TableView<RequestLogEntry> tableView;
    private final ObservableList<RequestLogEntry> logList;
    private final RequestLogDAO logDao;
    private final Button saveAsMockButton;
    private MockManagementView mockManagementView;

    public InspectorView() {
        this(new RequestLogDAO(), null);
    }

    public InspectorView(MockManagementView mockManagementView) {
        this(new RequestLogDAO(), mockManagementView);
    }

    public InspectorView(RequestLogDAO logDao) {
        this(logDao, null);
    }

    public InspectorView(RequestLogDAO logDao, MockManagementView mockManagementView) {
        this.logDao = logDao;
        this.mockManagementView = mockManagementView;

        setPadding(new Insets(16));
        setSpacing(12);

        saveAsMockButton = new Button("Save as Mock");
        saveAsMockButton.setId("saveAsMockButton");

        List<RequestLogEntry> recentLogs = logDao.getRecent(100);
        logList = FXCollections.observableArrayList(recentLogs);
        tableView = new TableView<>(logList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        saveAsMockButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            RequestLogEntry selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return true;
            }
            if ("CHAOS_RESET".equals(selected.getType())) {
                return true;
            }
            if (selected.getResponseBody() == null || selected.getResponseBody().trim().isEmpty()) {
                return true;
            }
            return false;
        }, tableView.getSelectionModel().selectedItemProperty()));

        saveAsMockButton.setOnAction(e -> {
            RequestLogEntry selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                saveAsMock(selected);
            }
        });

        HBox toolbar = new HBox(saveAsMockButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TableColumn<RequestLogEntry, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(param -> {
            long timestamp = param.getValue().getTimestamp();
            String formatted = TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
            return new SimpleStringProperty(formatted);
        });
        timeCol.setPrefWidth(85);

        TableColumn<RequestLogEntry, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMethod()));
        methodCol.setPrefWidth(80);

        TableColumn<RequestLogEntry, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath()));
        pathCol.setPrefWidth(250);

        TableColumn<RequestLogEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(param -> {
            RequestLogEntry entry = param.getValue();
            if ("CHAOS_RESET".equals(entry.getType())) {
                return new SimpleStringProperty("N/A (Reset)");
            }
            return new SimpleStringProperty(String.valueOf(entry.getStatusCode()));
        });
        statusCol.setPrefWidth(100);

        TableColumn<RequestLogEntry, String> durationCol = new TableColumn<>("Duration (ms)");
        durationCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDurationMs())));
        durationCol.setPrefWidth(110);

        TableColumn<RequestLogEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(120);

        tableView.getColumns().addAll(timeCol, methodCol, pathCol, statusCol, durationCol, typeCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Row click to open detail view
        tableView.setRowFactory(tv -> {
            TableRow<RequestLogEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    showDetailDialog(row.getItem());
                }
            });
            return row;
        });

        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                RequestLogEntry selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showDetailDialog(selected);
                }
            }
        });

        getChildren().addAll(toolbar, tableView);

        // Register listener for live updates and enforce 500-row display cap
        LogEventBus.getInstance().registerListener(entry -> {
            logList.add(0, entry);
            while (logList.size() > 500) {
                logList.remove(logList.size() - 1);
            }
        });
    }

    public void saveAsMock(RequestLogEntry entry) {
        if (entry == null || "CHAOS_RESET".equals(entry.getType())
                || entry.getResponseBody() == null || entry.getResponseBody().trim().isEmpty()) {
            return;
        }

        // 1. Sanitize entry.getPath() into a filename: replace every "/" and any non letter/digit/"-"/"_" character with "-", append ".json"
        String rawPath = entry.getPath() != null ? entry.getPath() : "";
        String filename = rawPath.replaceAll("[^a-zA-Z0-9_-]", "-") + ".json";

        // 2. Search mockMappings for an existing entry with localPath exactly equal to entry.getPath()
        List<MockMapping> mockMappings = ConfigManager.getInstance().getConfig().getMockMappings();
        MockMapping existingMock = null;
        if (mockMappings != null) {
            for (MockMapping m : mockMappings) {
                if (rawPath.equals(m.getLocalPath())) {
                    existingMock = m;
                    break;
                }
            }
        }

        if (existingMock != null && !existingMock.isAutoGenerated()) {
            boolean confirm = showThemedConfirmation("Confirm Overwrite",
                    "This path already has a manual mock. Overwrite with the captured response?");
            if (!confirm) {
                return;
            }
        }

        // 3. Write entry.getResponseBody() to AppPaths.getMocksDir().resolve(filename)
        Path mockFilePath = AppPaths.getMocksDir().resolve(filename);
        try {
            Files.createDirectories(AppPaths.getMocksDir());
            Files.writeString(mockFilePath, entry.getResponseBody(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }

        // 4. If updating an existing MockMapping, reuse its id, update filePath. If new, id = ConfigManager.getInstance().nextMockMappingId().
        if (existingMock != null) {
            existingMock.setFilePath(mockFilePath.toAbsolutePath().toString());
            existingMock.setEnabled(true);
            existingMock.setAutoGenerated(true);
        } else {
            MockMapping newMapping = new MockMapping();
            newMapping.setId(ConfigManager.getInstance().nextMockMappingId());
            newMapping.setLocalPath(rawPath);
            newMapping.setFilePath(mockFilePath.toAbsolutePath().toString());
            newMapping.setEnabled(true);
            newMapping.setAutoGenerated(true);
            if (mockMappings != null) {
                mockMappings.add(newMapping);
            }
        }

        ConfigManager.getInstance().save();

        if (mockManagementView != null) {
            mockManagementView.refresh();
        }

        showThemedAlert(Alert.AlertType.INFORMATION, "Mock Saved", "Mock saved and activated for " + rawPath);
    }

    public void setMockManagementView(MockManagementView mockManagementView) {
        this.mockManagementView = mockManagementView;
    }

    public MockManagementView getMockManagementView() {
        return mockManagementView;
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

    protected void showThemedAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ThemeManager.getInstance().styleDialog(alert);
        alert.showAndWait();
    }

    public Button getSaveAsMockButton() {
        return saveAsMockButton;
    }

    public void showDetailDialog(RequestLogEntry entry) {
        if (entry == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Request Details - " + entry.getMethod() + " " + entry.getPath());
        dialog.setHeaderText("HTTP " + ("CHAOS_RESET".equals(entry.getType()) ? "N/A (Reset)" : entry.getStatusCode())
                + " | " + entry.getMethod() + " " + entry.getPath()
                + " | " + entry.getDurationMs() + " ms (" + entry.getType() + ")");
        dialog.setResizable(true);

        ThemeManager.getInstance().styleDialog(dialog);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(800, 600);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(12));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        row1.setVgrow(Priority.ALWAYS);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        row2.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(row1, row2);

        VBox reqHeadersBox = createSection("Request Headers", entry.getRequestHeaders());
        VBox respHeadersBox = createSection("Response Headers", entry.getResponseHeaders());
        VBox reqBodyBox = createSection("Request Body", entry.getRequestBody());
        VBox respBodyBox = createSection("Response Body", entry.getResponseBody());

        grid.add(reqHeadersBox, 0, 0);
        grid.add(respHeadersBox, 1, 0);
        grid.add(reqBodyBox, 0, 1);
        grid.add(respBodyBox, 1, 1);

        dialog.getDialogPane().setContent(grid);
        showDialog(dialog);
    }

    public static String formatJsonIfPossible(String rawString) {
        if (rawString == null || rawString.trim().isEmpty()) {
            return "";
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(rawString);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return rawString;
        }
    }

    private VBox createSection(String title, String rawContent) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");

        TextArea textArea = new TextArea(formatJsonIfPossible(rawContent));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        VBox.setVgrow(textArea, Priority.ALWAYS);

        VBox box = new VBox(6, label, textArea);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    protected void showDialog(Dialog<?> dialog) {
        dialog.showAndWait();
    }

    public TableView<RequestLogEntry> getTableView() {
        return tableView;
    }

    public ObservableList<RequestLogEntry> getLogList() {
        return logList;
    }
}
