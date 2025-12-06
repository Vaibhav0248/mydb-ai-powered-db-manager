package com.mydb.controllers;

import com.mydb.services.BackupService;
import com.mydb.utils.DatabaseConnection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BackupController {
    
    private String currentDatabase;
    private TextArea logArea;
    private Label statusLabel;
    private ComboBox<String> tableSelector;
    private TextField backupPathField;
    
    public BackupController(String database) {
        this.currentDatabase = database;
    }
    
    public VBox createBackupView() {
    // Main container
    VBox mainContainer = new VBox();
    mainContainer.setStyle("-fx-background-color: white;");
    
    // Scrollable content
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
    
    VBox container = new VBox(20);
    container.setPadding(new Insets(20));
    container.setStyle("-fx-background-color: white;");
    
    // Header
    HBox header = createHeader();
    
    // Backup path selector
    VBox pathSection = createPathSection();
    
    // Database backup section
    VBox databaseBackupSection = createDatabaseBackupSection();
    
    // Table export section
    VBox tableExportSection = createTableExportSection();
    
    // Log area
    VBox logSection = createLogSection();
    
    // Status
    statusLabel = new Label("Ready to backup/export");
    statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
    
    container.getChildren().addAll(
        header, 
        pathSection, 
        new Separator(),
        databaseBackupSection, 
        new Separator(),
        tableExportSection,
        new Separator(),
        logSection,
        statusLabel
    );
    
    // Add content to scroll pane
    scrollPane.setContent(container);
    
    // Add scroll pane to main container
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    mainContainer.getChildren().add(scrollPane);
    
    // Load tables
    loadTables();
    
    return mainContainer;
}

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        FontIcon icon = new FontIcon(FontAwesomeSolid.DOWNLOAD);
        icon.setIconSize(24);
        icon.setIconColor(Color.web("#4CAF50"));
        
        Label title = new Label("Backup & Recovery");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(icon, title);
        
        return header;
    }
    
    private VBox createPathSection() {
        VBox section = new VBox(10);
        
        Label label = new Label("Backup Location:");
        label.setStyle("-fx-font-weight: bold;");
        
        HBox pathBox = new HBox(10);
        pathBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        backupPathField = new TextField(System.getProperty("user.home") + "/MyDB_Backups");
        backupPathField.setPrefWidth(400);
        HBox.setHgrow(backupPathField, Priority.ALWAYS);
        
        Button browseBtn = new Button("Browse...");
        FontIcon folderIcon = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
        browseBtn.setGraphic(folderIcon);
        browseBtn.setOnAction(e -> browseDirectory());
        
        pathBox.getChildren().addAll(backupPathField, browseBtn);
        section.getChildren().addAll(label, pathBox);
        
        return section;
    }
    
    private VBox createDatabaseBackupSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 20; -fx-background-radius: 5;");
        
        Label title = new Label("Full Database Backup");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label desc = new Label("Export entire database as SQL file (structure + data)");
        desc.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        
        HBox buttonBox = new HBox(10);
        
        Button backupBtn = new Button("Backup Database to SQL");
        backupBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        FontIcon downloadIcon = new FontIcon(FontAwesomeSolid.DATABASE);
        downloadIcon.setIconColor(Color.WHITE);
        backupBtn.setGraphic(downloadIcon);
        backupBtn.setOnAction(e -> backupDatabase());
        
        buttonBox.getChildren().add(backupBtn);
        
        section.getChildren().addAll(title, desc, buttonBox);
        
        return section;
    }
    
    private VBox createTableExportSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 20; -fx-background-radius: 5;");
        
        Label title = new Label("Table Export");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label desc = new Label("Export individual tables to different formats");
        desc.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        
        HBox selectorBox = new HBox(10);
        selectorBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label tableLabel = new Label("Select Table:");
        tableLabel.setStyle("-fx-font-weight: bold;");
        
        tableSelector = new ComboBox<>();
        tableSelector.setPrefWidth(250);
        
        selectorBox.getChildren().addAll(tableLabel, tableSelector);
        
        HBox buttonBox = new HBox(10);
        
        Button csvBtn = createExportButton("Export to CSV", FontAwesomeSolid.FILE_CSV, "#2196F3", () -> exportTable("CSV"));
        Button jsonBtn = createExportButton("Export to JSON", FontAwesomeSolid.FILE_CODE, "#FF9800", () -> exportTable("JSON"));
        Button sqlBtn = createExportButton("Export to SQL", FontAwesomeSolid.FILE_ALT, "#9C27B0", () -> exportTable("SQL"));
        
        buttonBox.getChildren().addAll(csvBtn, jsonBtn, sqlBtn);
        
        section.getChildren().addAll(title, desc, selectorBox, buttonBox);
        
        return section;
    }
    
    private Button createExportButton(String text, FontAwesomeSolid icon, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        FontIcon btnIcon = new FontIcon(icon);
        btnIcon.setIconColor(Color.WHITE);
        btn.setGraphic(btnIcon);
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    private VBox createLogSection() {
        VBox section = new VBox(10);
        
        Label label = new Label("Operation Log:");
        label.setStyle("-fx-font-weight: bold;");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff00; -fx-font-family: 'Courier New';");
        
        section.getChildren().addAll(label, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        return section;
    }
    
    private void loadTables() {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet rs = metaData.getTables(currentDatabase, null, "%", new String[]{"TABLE"});
                
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
                
                Platform.runLater(() -> {
                    tableSelector.getItems().addAll(tables);
                    if (!tables.isEmpty()) {
                        tableSelector.getSelectionModel().selectFirst();
                    }
                });
                
            } catch (SQLException e) {
                log("Error loading tables: " + e.getMessage());
            }
        }).start();
    }
    
    private void browseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Directory");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        File selectedDir = chooser.showDialog(null);
        if (selectedDir != null) {
            backupPathField.setText(selectedDir.getAbsolutePath());
        }
    }
    
    private void backupDatabase() {
        String outputPath = backupPathField.getText();
        File dir = new File(outputPath);
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        log("Starting full database backup...");
        log("Database: " + currentDatabase);
        log("Output path: " + outputPath);
        statusLabel.setText("Backing up database...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");
        
        new Thread(() -> {
            try {
                String filename = BackupService.exportDatabaseToSQL(currentDatabase, outputPath);
                
                Platform.runLater(() -> {
                    log("✓ Backup completed successfully!");
                    log("File: " + filename);
                    statusLabel.setText("✓ Backup completed: " + new File(filename).getName());
                    statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    showInfo("Backup Successful", "Database backed up to:\n" + filename);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    log("✗ Backup failed: " + e.getMessage());
                    statusLabel.setText("✗ Backup failed");
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                    showError("Backup Failed", e.getMessage());
                });
            }
        }).start();
    }
    
    private void exportTable(String format) {
        String tableName = tableSelector.getSelectionModel().getSelectedItem();
        
        if (tableName == null) {
            showWarning("No Table Selected", "Please select a table to export");
            return;
        }
        
        String outputPath = backupPathField.getText();
        File dir = new File(outputPath);
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        log("Exporting table: " + tableName + " to " + format);
        statusLabel.setText("Exporting to " + format + "...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");
        
        new Thread(() -> {
            try {
                String filename = null;
                
                switch (format) {
                    case "CSV":
                        filename = BackupService.exportTableToCSV(currentDatabase, tableName, outputPath);
                        break;
                    case "JSON":
                        filename = BackupService.exportTableToJSON(currentDatabase, tableName, outputPath);
                        break;
                    case "SQL":
                        filename = BackupService.exportDatabaseToSQL(currentDatabase, outputPath);
                        break;
                }
                
                String finalFilename = filename;
                Platform.runLater(() -> {
                    log("✓ Export completed!");
                    log("File: " + finalFilename);
                    statusLabel.setText("✓ Exported to " + format + ": " + new File(finalFilename).getName());
                    statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    showInfo("Export Successful", "Table exported to:\n" + finalFilename);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    log("✗ Export failed: " + e.getMessage());
                    statusLabel.setText("✗ Export failed");
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                    showError("Export Failed", e.getMessage());
                });
            }
        }).start();
    }
    
    private void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
        });
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
