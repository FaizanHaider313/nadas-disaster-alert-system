package com.disaster.alert.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Centralizes screen navigation — avoids duplicated FXMLLoader code
 * scattered across every controller.
 */
public class SceneManager {

    /** Switches the current stage to a new FXML screen. */
    public static void switchTo(Stage stage, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                    SceneManager.class.getResource(fxmlPath));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.err.println("[SceneManager] Cannot load " + fxmlPath);
            System.err.println("[SceneManager] Message: " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("[SceneManager] Caused by: " + cause);
                cause = cause.getCause();
            }
        }
    }

    /** Helper overload that takes any scene node to find the stage. */
    public static void switchTo(javafx.scene.Node node, String fxmlPath) {
        switchTo((Stage) node.getScene().getWindow(), fxmlPath);
    }
}