package com.mydb.controllers;

import com.mydb.App;
import com.mydb.models.Database;
import com.mydb.utils.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseSelectorController {
    
    @FXML private TilePane databaseContainer;
    @FXML private Label welcomeLabel;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private ProgressIndicator loadingIndicator;
    
    private ObservableList<Database> databases;
    
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + DatabaseConnection.getUsername());
        databases = FXCollections.observableArrayList();
        
        // Add icons to buttons
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC);
        refreshIcon.setIconColor(Color.WHITE);
        refreshButton.setGraphic(refreshIcon);
        
        FontIcon logoutIcon = new FontIcon(FontAwesomeSolid.SIGN_OUT_ALT);
        logoutButton.setGraphic(logoutIcon);
        
        loadDatabases();
    }
    
    private void loadDatabases() {
        loadingIndicator.setVisible(true);
        databaseContainer.getChildren().clear();
        
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW DATABASES");
                
                List<Database> dbList = new ArrayList<>();
                while (rs.next()) {
                    String dbName = rs.getString(1);
                    // Skip system databases
                    if (!dbName.equals("information_schema") && 
                        !dbName.equals("mysql") && 
                        !dbName.equals("performance_schema") && 
                        !dbName.equals("sys")) {
                        dbList.add(new Database(dbName));
                    }
                }
                
                Platform.runLater(() -> {
                    databases.setAll(dbList);
                    displayDatabases();
                    loadingIndicator.setVisible(false);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error loading databases: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void displayDatabases() {
        if (databases.isEmpty()) {
            Label emptyLabel = new Label("No databases found. Create one in MySQL first.");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16px;");
            databaseContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Database db : databases) {
            VBox card = createDatabaseCard(db);
            databaseContainer.getChildren().add(card);
        }
    }
    
    private VBox createDatabaseCard(Database database) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("database-card");
        card.setPrefWidth(280);
        card.setPrefHeight(150);
        
        // Database icon and name
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon dbIcon = new FontIcon(FontAwesomeSolid.DATABASE);
        dbIcon.setIconSize(40);
        dbIcon.setIconColor(Color.web("#667eea"));
        
        Label nameLabel = new Label(database.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(dbIcon, nameLabel);
        
        // Connect button
        Button connectBtn = new Button("Open Database");
        connectBtn.getStyleClass().add("secondary-button");
        FontIcon arrowIcon = new FontIcon(FontAwesomeSolid.ARROW_RIGHT);
        connectBtn.setGraphic(arrowIcon);
        
        connectBtn.setOnAction(e -> openDatabase(database.getName()));
        
        card.getChildren().addAll(header, connectBtn);
        
        return card;
    }
    
    private void openDatabase(String databaseName) {
        try {
            // Store selected database (we'll create this class next)
            com.mydb.utils.SessionManager.setCurrentDatabase(databaseName);
            
            // Switch to main workspace
            App.changeScene("/fxml/main-workspace.fxml", "MyDB - " + databaseName);
        } catch (Exception e) {
            showError("Error opening database: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadDatabases();
    }
    
    @FXML
    private void handleLogout() {
        try {
            DatabaseConnection.closeConnection();
            App.changeScene("/fxml/login.fxml", "MyDB - Login");
        } catch (Exception e) {
            showError("Error logging out: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCreateDatabase() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Database");
        dialog.setHeaderText("Create a new MySQL database");
        dialog.setContentText("Database Name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String dbName = result.get().trim();
            if (dbName.isEmpty()) {
                showError("Database name cannot be empty.");
                return;
            }
            
            // Validate database name (letters, numbers, underscores)
            if (!dbName.matches("^[a-zA-Z0-9_]+$")) {
                showError("Invalid database name. Only letters, numbers, and underscores are allowed.");
                return;
            }
            
            try {
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE DATABASE `" + dbName + "`");
                showInfo("Database '" + dbName + "' created successfully!");
                loadDatabases(); // Reload list
            } catch (Exception e) {
                showError("Failed to create database: " + e.getMessage());
            }
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
