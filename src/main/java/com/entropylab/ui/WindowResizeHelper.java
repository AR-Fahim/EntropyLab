package com.entropylab.ui;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
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
        this.borderSize = borderSize;
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
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    private void handleMouseMoved(MouseEvent t) {
        if (isResizing || stage.isMaximized()) {
            return;
        }

        Scene scene = stage.getScene();
        double mouseX = t.getSceneX();
        double mouseY = t.getSceneY();
        double sceneW = scene.getWidth();
        double sceneH = scene.getHeight();

        boolean left = mouseX < borderSize;
        boolean right = mouseX > sceneW - borderSize;
        boolean top = mouseY < borderSize;
        boolean bottom = mouseY > sceneH - borderSize;

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
        if (cursorEvent != Cursor.DEFAULT && !stage.isMaximized()) {
            isResizing = true;
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

        double deltaX = t.getScreenX() - startScreenX;
        double deltaY = t.getScreenY() - startScreenY;

        if (cursorEvent == Cursor.E_RESIZE || cursorEvent == Cursor.NE_RESIZE || cursorEvent == Cursor.SE_RESIZE) {
            double newW = Math.max(minWidth, startStageW + deltaX);
            stage.setWidth(newW);
        }
        if (cursorEvent == Cursor.S_RESIZE || cursorEvent == Cursor.SE_RESIZE || cursorEvent == Cursor.SW_RESIZE) {
            double newH = Math.max(minHeight, startStageH + deltaY);
            stage.setHeight(newH);
        }
        if (cursorEvent == Cursor.W_RESIZE || cursorEvent == Cursor.NW_RESIZE || cursorEvent == Cursor.SW_RESIZE) {
            double newW = Math.max(minWidth, startStageW - deltaX);
            if (newW > minWidth) {
                stage.setX(startStageX + deltaX);
                stage.setWidth(newW);
            }
        }
        if (cursorEvent == Cursor.N_RESIZE || cursorEvent == Cursor.NW_RESIZE || cursorEvent == Cursor.NE_RESIZE) {
            double newH = Math.max(minHeight, startStageH - deltaY);
            if (newH > minHeight) {
                stage.setY(startStageY + deltaY);
                stage.setHeight(newH);
            }
        }
    }

    private void handleMouseReleased(MouseEvent t) {
        isResizing = false;
        if (stage.getScene() != null) {
            stage.getScene().setCursor(Cursor.DEFAULT);
        }
    }
}
