package com.entropylab.ui;

import com.entropylab.util.AppBrandUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SubWindowHelper {

    private static double xOffset = 0;
    private static double yOffset = 0;

    /**
     * Styles any Dialog or Alert as an undecorated sub-window with:
     * 1. EntropyLab branding app icon on the stage.
     * 2. Custom title bar matching the active light/dark theme.
     * 3. Draggable title bar for window repositioning.
     * 4. Functional close button that cancels/closes the dialog cleanly.
     * 5. Dynamic theme synchronization via ThemeManager.
     */
    public static <R> Dialog<R> styleSubWindow(Dialog<R> dialog) {
        if (dialog == null) {
            return null;
        }

        try {
            dialog.initStyle(StageStyle.UNDECORATED);
        } catch (Exception ignored) {
            // Already initialized style
        }

        Stage stage = null;
        if (dialog.getDialogPane().getScene() != null && dialog.getDialogPane().getScene().getWindow() instanceof Stage) {
            stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().setAll(
                    AppBrandUtil.loadAppLogo(16, 16),
                    AppBrandUtil.loadAppLogo(32, 32),
                    AppBrandUtil.loadAppLogo(48, 48),
                    AppBrandUtil.loadAppLogo(64, 64)
            );
        }

        // Apply theme stylesheets and register scene for dynamic live-updating
        ThemeManager.getInstance().styleDialogPane(dialog.getDialogPane());
        if (dialog.getDialogPane().getScene() != null) {
            ThemeManager.getInstance().registerScene(dialog.getDialogPane().getScene());
        }

        // Build Title Bar
        HBox titleBar = new HBox(8);
        titleBar.getStyleClass().add("custom-title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 0, 0, 10));

        ImageView logoView = new ImageView(AppBrandUtil.loadAppLogo(18, 18));
        logoView.setFitWidth(18);
        logoView.setFitHeight(18);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        String title = dialog.getTitle();
        if (title == null || title.isBlank()) {
            title = "EntropyLab";
        }
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.getStyleClass().addAll("title-bar-btn", "title-bar-close-btn");
        SVGPath closeGlyph = new SVGPath();
        closeGlyph.getStyleClass().add("title-bar-glyph");
        closeGlyph.setContent("M 1.05 0 L 5 3.95 L 8.95 0 L 10 1.05 L 6.05 5 L 10 8.95 L 8.95 10 L 5 6.05 L 1.05 10 L 0 8.95 L 3.95 5 L 0 1.05 Z");
        closeBtn.setGraphic(closeGlyph);

        closeBtn.setOnAction(e -> {
            for (ButtonType bt : dialog.getDialogPane().getButtonTypes()) {
                if (bt == ButtonType.CANCEL || bt == ButtonType.NO || bt == ButtonType.CLOSE) {
                    dialog.setResult((R) bt);
                    dialog.close();
                    return;
                }
            }
            dialog.close();
        });

        titleBar.getChildren().addAll(logoView, titleLabel, spacer, closeBtn);

        // Window drag handlers for repositioning
        final Stage finalStage = stage;
        titleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && finalStage != null) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        titleBar.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && finalStage != null) {
                finalStage.setX(event.getScreenX() - xOffset);
                finalStage.setY(event.getScreenY() - yOffset);
            }
        });

        String headerText = dialog.getHeaderText();
        dialog.setHeaderText(null); // Clear default JavaFX header text

        if (headerText != null && !headerText.isBlank()) {
            VBox headerContainer = new VBox(0);
            headerContainer.getChildren().add(titleBar);

            Label headerLabel = new Label(headerText);
            headerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12px 16px 4px 16px;");
            headerContainer.getChildren().add(headerLabel);
            dialog.getDialogPane().setHeader(headerContainer);
        } else {
            dialog.getDialogPane().setHeader(titleBar);
        }

        dialog.getDialogPane().getStyleClass().add("window-root");
        return dialog;
    }
}
