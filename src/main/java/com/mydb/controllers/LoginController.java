package com.mydb.controllers;

import com.mydb.App;
import com.mydb.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.sql.Connection;

public class LoginController {
    
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button connectButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    
    @FXML
    public void initialize() {
        // Set default values
        hostField.setText("localhost");
        portField.setText("3306");
        usernameField.setText("root");
        
        // Hide progress indicator initially
        progressIndicator.setVisible(false);
        
        // Add icon to connect button
        FontIcon icon = new FontIcon(FontAwesomeSolid.SIGN_IN_ALT);
        icon.setIconColor(Color.WHITE);
        connectButton.setGraphic(icon);
    }
    
    @FXML
    private void handleConnect() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (host.isEmpty() || port.isEmpty() || username.isEmpty()) {
            showError("Please fill in all required fields!");
            return;
        }
        
        // Show loading
        connectButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Connecting...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");
        
        // Connect in background thread
        new Thread(() -> {
            try {
                DatabaseConnection.initialize(host, port, username, password);
                Connection conn = DatabaseConnection.getConnection();
                
                if (conn != null && !conn.isClosed()) {
                    // Success - switch to database selector
                    javafx.application.Platform.runLater(() -> {
                        try {
                            App.changeScene("/fxml/database-selector.fxml", "MyDB - Select Database");
                        } catch (Exception e) {
                            showError("Error loading database selector: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Connection failed: " + e.getMessage());
                    connectButton.setDisable(false);
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }
}
