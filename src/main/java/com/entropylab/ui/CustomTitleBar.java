package com.entropylab.ui;

import com.entropylab.util.AppBrandUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class CustomTitleBar extends HBox {

    private static final String SUN_SVG =
            "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm0-5a1 1 0 0 1 1 1v2a1 1 0 0 1-2 0V3a1 1 0 0 1 1-1zm0 18a1 1 0 0 1 1 1v2a1 1 0 0 1-2 0v-2a1 1 0 0 1 1-1zM4.22 4.22a1 1 0 0 1 1.42 0l1.41 1.42a1 1 0 0 1-1.41 1.41L4.22 5.64a1 1 0 0 1 0-1.42zm14.14 14.14a1 1 0 0 1 1.42 0l1.41 1.42a1 1 0 0 1-1.41 1.41l-1.42-1.41a1 1 0 0 1 0-1.42zM2 12a1 1 0 0 1 1-1h2a1 1 0 0 1 0 2H3a1 1 0 0 1-1-1zm18 0a1 1 0 0 1 1-1h2a1 1 0 0 1 0 2h-2a1 1 0 0 1-1-1zM5.64 18.36a1 1 0 0 1 0 1.42l-1.42 1.41a1 1 0 0 1-1.41-1.41l1.41-1.42a1 1 0 0 1 1.42 0zm14.14-14.14a1 1 0 0 1 0 1.42l-1.42 1.41a1 1 0 0 1-1.41-1.41l1.41-1.42a1 1 0 0 1 1.42 0z";

    private static final String MOON_SVG =
            "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";

    private static final String MIN_SVG =
            "M 0 0 H 10 V 1.5 H 0 Z";

    private static final String MAX_SVG =
            "M 0 0 H 10 V 10 H 0 Z M 1.2 1.2 V 8.8 H 8.8 V 1.2 Z";

    private static final String RESTORE_SVG =
            "M 2 0 H 10 V 8 H 8 V 10 H 0 V 2 H 2 Z M 3.2 1.2 V 2 H 8 V 6.8 H 8.8 V 1.2 Z M 1.2 3.2 V 8.8 H 6.8 V 3.2 Z";

    private static final String CLOSE_SVG =
            "M 1.05 0 L 5 3.95 L 8.95 0 L 10 1.05 L 6.05 5 L 10 8.95 L 8.95 10 L 5 6.05 L 1.05 10 L 0 8.95 L 3.95 5 L 0 1.05 Z";

    private final Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private boolean isSnapped = false;
    private double prevX, prevY, prevW, prevH;

    private final SVGPath themeSvg = new SVGPath();
    private final Button themeToggleBtn = new Button();
    private final SVGPath maxGlyph = new SVGPath();
    private final Button maxBtn = new Button();

    public CustomTitleBar(Stage stage) {
        this.stage = stage;
        getStyleClass().add("custom-title-bar");
        setAlignment(Pos.CENTER_LEFT);

        // 1. Branding: Logo + Title
        HBox brandBox = new HBox(8);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 0, 0, 10));

        Image logoImg = AppBrandUtil.loadAppLogo(18, 18);
        if (logoImg != null) {
            ImageView logoView = new ImageView(logoImg);
            logoView.setFitWidth(18);
            logoView.setFitHeight(18);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            brandBox.getChildren().add(logoView);
        }

        Label titleLabel = new Label("EntropyLab");
        titleLabel.getStyleClass().add("title-bar-title");
        brandBox.getChildren().add(titleLabel);

        // 2. Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. Day-Night Toggle Icon Button
        themeToggleBtn.getStyleClass().add("title-bar-theme-btn");
        themeSvg.getStyleClass().add("title-bar-theme-icon");
        themeSvg.setScaleX(0.68);
        themeSvg.setScaleY(0.68);
        themeToggleBtn.setGraphic(themeSvg);
        updateThemeDisplay(ThemeManager.getInstance().getCurrentTheme());

        themeToggleBtn.setOnAction(e -> ThemeManager.getInstance().toggleTheme());
        ThemeManager.getInstance().addThemeChangeListener(this::updateThemeDisplay);

        // 4. Window Controls: Minimize, Maximize/Restore, Close
        Button minBtn = new Button();
        minBtn.getStyleClass().addAll("title-bar-btn", "title-bar-min-btn");
        SVGPath minGlyph = new SVGPath();
        minGlyph.getStyleClass().add("title-bar-glyph");
        minGlyph.setContent(MIN_SVG);
        minBtn.setGraphic(minGlyph);
        minBtn.setTooltip(new Tooltip("Minimize"));
        minBtn.setOnAction(e -> stage.setIconified(true));

        maxBtn.getStyleClass().addAll("title-bar-btn", "title-bar-max-btn");
        maxGlyph.getStyleClass().add("title-bar-glyph");
        updateMaxBtnGraphic(false);
        maxBtn.setGraphic(maxGlyph);
        maxBtn.setOnAction(e -> toggleMaximize());

        Button closeBtn = new Button();
        closeBtn.getStyleClass().addAll("title-bar-btn", "title-bar-close-btn");
        SVGPath closeGlyph = new SVGPath();
        closeGlyph.getStyleClass().add("title-bar-glyph");
        closeGlyph.setContent(CLOSE_SVG);
        closeBtn.setGraphic(closeGlyph);
        closeBtn.setTooltip(new Tooltip("Close"));
        closeBtn.setOnAction(e -> {
            stage.close();
            System.exit(0);
        });

        HBox captionBox = new HBox(0, minBtn, maxBtn, closeBtn);
        captionBox.setAlignment(Pos.CENTER_RIGHT);

        HBox rightBox = new HBox(6, themeToggleBtn, captionBox);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(brandBox, spacer, rightBox);

        // Register title bar in stage properties for WindowResizeHelper coordination
        stage.getProperties().put("customTitleBar", this);

        // Window dragging support with drag-to-restore
        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (isMaximized || isSnapped) {
                    // Restore smoothly on drag like native Windows apps
                    double currentWidth = stage.getWidth();
                    double mouseX = event.getScreenX();
                    double ratio = currentWidth > 0 ? (event.getSceneX() / currentWidth) : 0.5;

                    double restoreW = prevW > 0 ? prevW : 1000;
                    double restoreH = prevH > 0 ? prevH : 700;

                    stage.setWidth(restoreW);
                    stage.setHeight(restoreH);

                    double newX = mouseX - (restoreW * ratio);
                    double newY = event.getScreenY() - yOffset;
                    stage.setX(newX);
                    stage.setY(newY);

                    xOffset = restoreW * ratio;
                    isMaximized = false;
                    isSnapped = false;
                    stage.getProperties().put("customMaximized", Boolean.FALSE);
                    updateMaxBtnGraphic(false);
                } else {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            }
        });

        // Snap to edges when released near screen borders
        setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double screenX = event.getScreenX();
                double screenY = event.getScreenY();
                Screen screen = getCurrentScreen();
                Rectangle2D bounds = screen.getVisualBounds();

                // Drag to top edge (within 8px) -> Maximize
                if (screenY <= bounds.getMinY() + 8) {
                    maximizeWindow();
                }
                // Drag to left edge (within 8px) -> Snap Left
                else if (screenX <= bounds.getMinX() + 8) {
                    snapLeft();
                }
                // Drag to right edge (within 8px) -> Snap Right
                else if (screenX >= bounds.getMaxX() - 8) {
                    snapRight();
                }
            }
        });

        // Double click anywhere on title bar (except buttons) to maximize/restore
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
                boolean isButton = false;
                while (target != null && target != this) {
                    if (target instanceof Button) {
                        isButton = true;
                        break;
                    }
                    target = target.getParent();
                }
                if (!isButton) {
                    toggleMaximize();
                }
            }
        });

        // Sync maximize / restore icon when OS resizes or snaps the stage (e.g. Win+Left, Win+Right, Win+Up)
        javafx.beans.value.ChangeListener<Number> boundsListener = (obs, oldVal, newVal) -> {
            Screen screen = getCurrentScreen();
            if (screen != null) {
                Rectangle2D vb = screen.getVisualBounds();
                boolean matchesMax = Math.abs(stage.getX() - vb.getMinX()) < 10
                        && Math.abs(stage.getY() - vb.getMinY()) < 10
                        && Math.abs(stage.getWidth() - vb.getWidth()) < 10
                        && Math.abs(stage.getHeight() - vb.getHeight()) < 10;
                if (matchesMax && !isMaximized) {
                    isMaximized = true;
                    isSnapped = false;
                    stage.getProperties().put("customMaximized", Boolean.TRUE);
                    updateMaxBtnGraphic(true);
                } else if (!matchesMax && isMaximized) {
                    isMaximized = false;
                    stage.getProperties().put("customMaximized", Boolean.FALSE);
                    updateMaxBtnGraphic(false);
                }
            }
        };
        stage.widthProperty().addListener(boundsListener);
        stage.heightProperty().addListener(boundsListener);
        stage.xProperty().addListener(boundsListener);
        stage.yProperty().addListener(boundsListener);

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            isMaximized = newVal;
            stage.getProperties().put("customMaximized", newVal);
            updateMaxBtnGraphic(newVal);
        });
    }

    private void updateThemeDisplay(ThemeManager.Theme theme) {
        if (theme == ThemeManager.Theme.DARK) {
            themeSvg.setContent(SUN_SVG);
            themeToggleBtn.setTooltip(new Tooltip("Switch to Light Mode"));
        } else {
            themeSvg.setContent(MOON_SVG);
            themeToggleBtn.setTooltip(new Tooltip("Switch to Dark Mode"));
        }
    }

    public void toggleMaximize() {
        if (isMaximized) {
            restoreWindow();
        } else {
            maximizeWindow();
        }
    }

    public void maximizeWindow() {
        if (!isMaximized) {
            saveFloatingBoundsIfNeeded();
            Screen screen = getCurrentScreen();
            Rectangle2D bounds = screen.getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            isMaximized = true;
            isSnapped = false;
            stage.getProperties().put("customMaximized", Boolean.TRUE);
            updateMaxBtnGraphic(true);
        }
    }

    public void restoreWindow() {
        if (isMaximized || isSnapped) {
            stage.setX(prevX > 0 ? prevX : 100);
            stage.setY(prevY > 0 ? prevY : 100);
            stage.setWidth(prevW > 0 ? prevW : 1000);
            stage.setHeight(prevH > 0 ? prevH : 700);
            isMaximized = false;
            isSnapped = false;
            stage.getProperties().put("customMaximized", Boolean.FALSE);
            updateMaxBtnGraphic(false);
        }
    }

    public void snapLeft() {
        saveFloatingBoundsIfNeeded();
        Screen screen = getCurrentScreen();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth() / 2.0);
        stage.setHeight(bounds.getHeight());
        isMaximized = false;
        isSnapped = true;
        stage.getProperties().put("customMaximized", Boolean.FALSE);
        updateMaxBtnGraphic(false);
    }

    public void snapRight() {
        saveFloatingBoundsIfNeeded();
        Screen screen = getCurrentScreen();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX() + (bounds.getWidth() / 2.0));
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth() / 2.0);
        stage.setHeight(bounds.getHeight());
        isMaximized = false;
        isSnapped = true;
        stage.getProperties().put("customMaximized", Boolean.FALSE);
        updateMaxBtnGraphic(false);
    }

    private void saveFloatingBoundsIfNeeded() {
        if (!isMaximized && !isSnapped) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevW = stage.getWidth();
            prevH = stage.getHeight();
            if (prevW < 850) prevW = 1000;
            if (prevH < 550) prevH = 700;
        }
    }

    private Screen getCurrentScreen() {
        return Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream().findFirst().orElse(Screen.getPrimary());
    }

    private void updateMaxBtnGraphic(boolean maximized) {
        if (maximized) {
            maxGlyph.setContent(RESTORE_SVG);
            maxBtn.setTooltip(new Tooltip("Restore Down"));
        } else {
            maxGlyph.setContent(MAX_SVG);
            maxBtn.setTooltip(new Tooltip("Maximize"));
        }
    }

    public boolean isWindowMaximized() {
        return isMaximized;
    }

    public boolean isWindowSnapped() {
        return isSnapped;
    }
}
