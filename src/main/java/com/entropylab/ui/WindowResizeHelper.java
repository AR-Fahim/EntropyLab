package com.entropylab.ui;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WindowResizeHelper {

    private final Stage stage;
    private final int borderSize;
    private final double minWidth;
    private final double minHeight;

    private Cursor cursorEvent = Cursor.DEFAULT;
    private double startScreenX, startScreenY;
    private double startStageX, startStageY, startStageW, startStageH;
    private boolean isResizing = false;

    public static void install(Stage stage, int borderSize, double minWidth, double minHeight) {
        new WindowResizeHelper(stage, borderSize, minWidth, minHeight);
    }

    private WindowResizeHelper(Stage stage, int borderSize, double minWidth, double minHeight) {
        this.stage = stage;
        this.borderSize = Math.max(borderSize, 10);
        this.minWidth = minWidth;
        this.minHeight = minHeight;

        Scene scene = stage.getScene();
        if (scene != null) {
            attachListeners(scene);
        } else {
            stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    attachListeners(newScene);
                }
            });
        }
    }

    private void attachListeners(Scene scene) {
        // Use EventFilters to capture before any child component can consume mouse events
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);

        // Keyboard window snapping: Win+Left, Win+Right, Win+Up, Win+Down AND Alt+Left/Right/Up/Down
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    private CustomTitleBar getCustomTitleBar() {
        Object tb = stage.getProperties().get("customTitleBar");
        if (tb instanceof CustomTitleBar) {
            return (CustomTitleBar) tb;
        }
        return null;
    }

    private boolean isWindowMaximized() {
        if (stage.isMaximized()) {
            return true;
        }
        CustomTitleBar tb = getCustomTitleBar();
        if (tb != null) {
            return tb.isWindowMaximized();
        }
        return Boolean.TRUE.equals(stage.getProperties().get("customMaximized"));
    }

    private void handleKeyPressed(KeyEvent event) {
        boolean winOrMeta = event.isMetaDown();
        boolean alt = event.isAltDown();
        boolean ctrl = event.isControlDown();

        // Support Win+Arrow and Alt+Arrow (without Ctrl)
        if (winOrMeta || (alt && !ctrl)) {
            CustomTitleBar tb = getCustomTitleBar();
            if (tb != null) {
                if (event.getCode() == KeyCode.LEFT) {
                    tb.snapLeft();
                    event.consume();
                } else if (event.getCode() == KeyCode.RIGHT) {
                    tb.snapRight();
                    event.consume();
                } else if (event.getCode() == KeyCode.UP) {
                    tb.maximizeWindow();
                    event.consume();
                } else if (event.getCode() == KeyCode.DOWN) {
                    tb.restoreWindow();
                    event.consume();
                }
            }
        }
    }

    private void handleMouseMoved(MouseEvent t) {
        if (isResizing) {
            return;
        }

        if (isWindowMaximized()) {
            if (cursorEvent != Cursor.DEFAULT) {
                cursorEvent = Cursor.DEFAULT;
                Scene scene = stage.getScene();
                if (scene != null) {
                    scene.setCursor(Cursor.DEFAULT);
                }
            }
            return;
        }

        Scene scene = stage.getScene();
        if (scene == null) return;

        double mouseX = t.getSceneX();
        double mouseY = t.getSceneY();
        double sceneW = scene.getWidth();
        double sceneH = scene.getHeight();

        boolean left = mouseX <= borderSize;
        boolean right = mouseX >= sceneW - borderSize;
        boolean top = mouseY <= borderSize;
        boolean bottom = mouseY >= sceneH - borderSize;

        if (top && left) {
            cursorEvent = Cursor.NW_RESIZE;
        } else if (top && right) {
            cursorEvent = Cursor.NE_RESIZE;
        } else if (bottom && left) {
            cursorEvent = Cursor.SW_RESIZE;
        } else if (bottom && right) {
            cursorEvent = Cursor.SE_RESIZE;
        } else if (left) {
            cursorEvent = Cursor.W_RESIZE;
        } else if (right) {
            cursorEvent = Cursor.E_RESIZE;
        } else if (top) {
            cursorEvent = Cursor.N_RESIZE;
        } else if (bottom) {
            cursorEvent = Cursor.S_RESIZE;
        } else {
            cursorEvent = Cursor.DEFAULT;
        }

        scene.setCursor(cursorEvent);
    }

    private void handleMousePressed(MouseEvent t) {
        if (cursorEvent != Cursor.DEFAULT && !isWindowMaximized()) {
            isResizing = true;
            t.consume();
            startScreenX = t.getScreenX();
            startScreenY = t.getScreenY();
            startStageX = stage.getX();
            startStageY = stage.getY();
            startStageW = stage.getWidth();
            startStageH = stage.getHeight();
        }
    }

    private void handleMouseDragged(MouseEvent t) {
        if (!isResizing) {
            return;
        }
        t.consume();

        double deltaX = t.getScreenX() - startScreenX;
        double deltaY = t.getScreenY() - startScreenY;

        // East (Right border)
        if (cursorEvent == Cursor.E_RESIZE || cursorEvent == Cursor.NE_RESIZE || cursorEvent == Cursor.SE_RESIZE) {
            double newW = Math.max(minWidth, startStageW + deltaX);
            stage.setWidth(newW);
        }

        // South (Bottom border)
        if (cursorEvent == Cursor.S_RESIZE || cursorEvent == Cursor.SE_RESIZE || cursorEvent == Cursor.SW_RESIZE) {
            double newH = Math.max(minHeight, startStageH + deltaY);
            stage.setHeight(newH);
        }

        // West (Left border) - keep right edge pinned
        if (cursorEvent == Cursor.W_RESIZE || cursorEvent == Cursor.NW_RESIZE || cursorEvent == Cursor.SW_RESIZE) {
            double newW = Math.max(minWidth, startStageW - deltaX);
            stage.setX(startStageX + (startStageW - newW));
            stage.setWidth(newW);
        }

        // North (Top border) - keep bottom edge pinned
        if (cursorEvent == Cursor.N_RESIZE || cursorEvent == Cursor.NW_RESIZE || cursorEvent == Cursor.NE_RESIZE) {
            double newH = Math.max(minHeight, startStageH - deltaY);
            stage.setY(startStageY + (startStageH - newH));
            stage.setHeight(newH);
        }
    }

    private void handleMouseReleased(MouseEvent t) {
        if (isResizing) {
            isResizing = false;
            t.consume();
            if (stage.getScene() != null) {
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }
}
