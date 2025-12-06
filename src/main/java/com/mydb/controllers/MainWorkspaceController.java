package com.mydb.controllers;

// import com.mydb.controllers.MySQLDocsController;
import javafx.stage.FileChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.mydb.App;
import com.mydb.ai.GeminiAPIClient;
import com.mydb.controllers.BackupController;
import com.mydb.controllers.CRUDController;
import com.mydb.controllers.HelpController;
import com.mydb.controllers.SecurityController;
import com.mydb.controllers.VisualizationController;
import com.mydb.models.Table;
import com.mydb.security.SecurityMonitor;
import com.mydb.services.BackupService;
import com.mydb.utils.DatabaseConnection;
import com.mydb.utils.DatabaseSchemaExtractor;
import com.mydb.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainWorkspaceController {

    @FXML private Label databaseNameLabel;
    @FXML private TreeView<String> databaseTreeView;
    @FXML private TabPane mainTabPane;
    @FXML private TextArea terminalOutput;
    @FXML private Button backButton;
    @FXML private ToggleGroup modeGroup;
    @FXML private RadioButton mysqlModeRadio;
    @FXML private RadioButton aiModeRadio;

    private String currentDatabase;
    private ObservableList<Table> tables;

    @FXML
    public void initialize() {
        currentDatabase = SessionManager.getCurrentDatabase();
        databaseNameLabel.setText(currentDatabase);
        tables = FXCollections.observableArrayList();

        // Setup back button icon
        FontIcon backIcon = new FontIcon(FontAwesomeSolid.ARROW_LEFT);
        backIcon.setIconColor(Color.WHITE);
        backButton.setGraphic(backIcon);

        // Setup mode toggle
        modeGroup = new ToggleGroup();
        mysqlModeRadio.setToggleGroup(modeGroup);
        aiModeRadio.setToggleGroup(modeGroup);
        mysqlModeRadio.setSelected(true);

        // Load database structure
        loadDatabaseStructure();

        // Setup default tabs (includes Help tab)
        createDefaultTabs();

        // Add tree view double-click handler to open CRUD tab
        databaseTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = databaseTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getParent() != null
                        && selectedItem.getParent().getValue().equals(currentDatabase)) {
                    String tableName = selectedItem.getValue();
                    openTableInCRUD(tableName);
                }
            }
        });

        // Welcome messages
        logToTerminal("Connected to database: " + currentDatabase);
        logToTerminal("Ready to execute queries!");
        logToTerminal("TIP: Double-click a table in the tree to view/edit records");
    }

    private void loadDatabaseStructure() {
        TreeItem<String> rootItem = new TreeItem<>(currentDatabase);
        rootItem.setExpanded(true);

        FontIcon dbIcon = new FontIcon(FontAwesomeSolid.DATABASE);
        dbIcon.setIconColor(Color.web("#667eea"));
        rootItem.setGraphic(dbIcon);

        databaseTreeView.setRoot(rootItem);

        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet rs = metaData.getTables(currentDatabase, null, "%", new String[] { "TABLE" });

                List<TreeItem<String>> tableItems = new ArrayList<>();

                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    TreeItem<String> tableItem = new TreeItem<>(tableName);

                    FontIcon tableIcon = new FontIcon(FontAwesomeSolid.TABLE);
                    tableIcon.setIconColor(Color.web("#4CAF50"));
                    tableItem.setGraphic(tableIcon);

                    loadTableColumns(tableItem, tableName);

                    tableItems.add(tableItem);
                }

                Platform.runLater(() -> {
                    rootItem.getChildren().addAll(tableItems);
                    logToTerminal("Loaded " + tableItems.size() + " tables from database '" + currentDatabase + "'");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logToTerminal("ERROR: " + e.getMessage());
                });
            }
        }).start();
    }

    private void loadTableColumns(TreeItem<String> tableItem, String tableName) {
        try {
            Connection conn = DatabaseConnection.getConnection(currentDatabase);
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(currentDatabase, null, tableName, null);

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");

                TreeItem<String> columnItem = new TreeItem<>(columnName + " (" + columnType + ")");

                FontIcon columnIcon = new FontIcon(FontAwesomeSolid.COLUMNS);
                columnIcon.setIconColor(Color.web("#FF9800"));
                columnItem.setGraphic(columnIcon);

                tableItem.getChildren().add(columnItem);
            }
        } catch (Exception e) {
            System.err.println("Error loading columns for " + tableName + ": " + e.getMessage());
        }
    }

    private void createDefaultTabs() {
        Tab crudTab = new Tab("Table View (CRUD)");
        crudTab.setClosable(false);
        FontIcon tableIcon = new FontIcon(FontAwesomeSolid.TABLE);
        crudTab.setGraphic(tableIcon);
        Label crudPlaceholder = new Label("Select a table from the left panel to view and edit data");
        crudPlaceholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        crudTab.setContent(new BorderPane(crudPlaceholder));

        Tab queryTab = new Tab("Query Editor");
        queryTab.setClosable(false);
        FontIcon codeIcon = new FontIcon(FontAwesomeSolid.CODE);
        queryTab.setGraphic(codeIcon);
        queryTab.setContent(createQueryEditor());

        Tab visualTab = new Tab("Visualize");
        visualTab.setClosable(false);
        FontIcon chartIcon = new FontIcon(FontAwesomeSolid.CHART_BAR);
        visualTab.setGraphic(chartIcon);
        VisualizationController vizController = new VisualizationController(currentDatabase);
        visualTab.setContent(vizController.createVisualizationView());

        Tab backupTab = new Tab("Backup & Recovery");
        backupTab.setClosable(false);
        FontIcon backupIcon = new FontIcon(FontAwesomeSolid.DOWNLOAD);
        backupTab.setGraphic(backupIcon);
        BackupController backupController = new BackupController(currentDatabase);
        backupTab.setContent(backupController.createBackupView());

        Tab securityTab = new Tab("Security Monitor");
        securityTab.setClosable(false);
        FontIcon securityIcon = new FontIcon(FontAwesomeSolid.SHIELD_ALT);
        securityTab.setGraphic(securityIcon);
        SecurityController securityController = new SecurityController(currentDatabase);
        securityTab.setContent(securityController.createSecurityView());

        Tab helpTab = new Tab("Help & About");
        helpTab.setClosable(false);
        FontIcon helpIcon = new FontIcon(FontAwesomeSolid.QUESTION_CIRCLE);
        helpIcon.setIconColor(Color.web("#667eea"));
        helpTab.setGraphic(helpIcon);
        HelpController helpController = new HelpController();
        helpTab.setContent(helpController.createHelpView());

        mainTabPane.getTabs().addAll(crudTab, queryTab, visualTab, backupTab, securityTab, helpTab);
    }

    private void handleExportResults(TableView<ObservableList<String>> resultsTable) {
        if (resultsTable.getItems().isEmpty() || resultsTable.getColumns().isEmpty()) {
            showWarning("No Data", "There are no query results to export.\nExecute a SELECT query first.");
            return;
        }

        List<String> formats = List.of("CSV", "JSON");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("CSV", formats);
        dialog.setTitle("Export Query Results");
        dialog.setHeaderText("Export current query results");
        dialog.setContentText("Choose format:");

        Optional<String> choice = dialog.showAndWait();
        if (choice.isEmpty()) return;
        String format = choice.get();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Query Results");

        String defaultName = "query_results";
        switch (format) {
            case "CSV":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                fileChooser.setInitialFileName(defaultName + ".csv");
                break;
            case "JSON":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                fileChooser.setInitialFileName(defaultName + ".json");
                break;
        }

        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file == null) return;

        new Thread(() -> {
            try {
                switch (format) {
                    case "CSV":
                        exportResultsAsCSV(resultsTable, file);
                        break;
                    case "JSON":
                        exportResultsAsJSON(resultsTable, file);
                        break;
                }

                Platform.runLater(() -> {
                    showInfo("Export Complete",
                            "Query results exported successfully as " + format + ".");
                    logToTerminal("Exported query results to: " + file.getAbsolutePath());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Export Error", ex.getMessage()));
            }
        }).start();
    }

    private void exportResultsAsCSV(TableView<ObservableList<String>> table, File file)
            throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            // Header row
            for (int i = 0; i < table.getColumns().size(); i++) {
                String colName = table.getColumns().get(i).getText();
                writer.write(escapeCSV(colName));
                if (i < table.getColumns().size() - 1)
                    writer.write(",");
            }
            writer.newLine();

            // Data rows
            for (ObservableList<String> row : table.getItems()) {
                for (int i = 0; i < row.size(); i++) {
                    String value = row.get(i);
                    if (value == null) value = "";
                    writer.write(escapeCSV(value));
                    if (i < row.size() - 1)
                        writer.write(",");
                }
                writer.newLine();
            }
        }
    }

    private String escapeCSV(String value) {
        boolean needQuotes = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        if (needQuotes) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void exportResultsAsJSON(TableView<ObservableList<String>> table, File file)
            throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[");
            writer.newLine();

            boolean firstRow = true;
            for (ObservableList<String> row : table.getItems()) {
                if (!firstRow) {
                    writer.write(",");
                    writer.newLine();
                }
                writer.write("  {");

                for (int i = 0; i < table.getColumns().size(); i++) {
                    String colName = table.getColumns().get(i).getText();
                    String value = row.get(i);

                    writer.write("\"" + escapeJSON(colName) + "\": ");
                    if (value == null || "NULL".equalsIgnoreCase(value)) {
                        writer.write("null");
                    } else {
                        writer.write("\"" + escapeJSON(value) + "\"");
                    }

                    if (i < table.getColumns().size() - 1)
                        writer.write(", ");
                }

                writer.write("}");
                firstRow = false;
            }

            writer.newLine();
            writer.write("]");
        }
    }

    private String escapeJSON(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private VBox createQueryEditor() {
        VBox container = new VBox(15);
        container.setStyle("-fx-padding: 20;");

        ToolBar modeToolbar = new ToolBar();
        Label modeLabel = new Label("Query Mode:");
        modeLabel.setStyle("-fx-font-weight: bold;");
        modeToolbar.getItems().addAll(modeLabel, mysqlModeRadio, aiModeRadio);

        TextArea queryInput = new TextArea();
        queryInput.setPromptText("Enter your SQL query here or use natural language in AI mode...");
        queryInput.setPrefRowCount(10);
        queryInput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");

        ToolBar buttonBar = new ToolBar();
        Button executeBtn = new Button("Execute Query");
        executeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        FontIcon playIcon = new FontIcon(FontAwesomeSolid.PLAY);
        playIcon.setIconColor(Color.WHITE);
        executeBtn.setGraphic(playIcon);

        Button clearBtn = new Button("Clear");
        FontIcon clearIcon = new FontIcon(FontAwesomeSolid.ERASER);
        clearBtn.setGraphic(clearIcon);

        Button copyBtn = new Button("Copy");
        FontIcon copyIcon = new FontIcon(FontAwesomeSolid.COPY);
        copyBtn.setGraphic(copyIcon);

        Button exportBtn = new Button("Export Results");
        FontIcon exportIcon = new FontIcon(FontAwesomeSolid.FILE_EXPORT);
        exportBtn.setGraphic(exportIcon);

        // Results table must be declared BEFORE using in handler
        TableView<ObservableList<String>> resultsTable = new TableView<>();
        resultsTable.setPlaceholder(new Label("Query results will appear here"));

        exportBtn.setOnAction(e -> handleExportResults(resultsTable));

        buttonBar.getItems().addAll(executeBtn, clearBtn, copyBtn, exportBtn);

        executeBtn.setOnAction(e -> {
            String query = queryInput.getText().trim();
            if (!query.isEmpty()) {
                if (aiModeRadio.isSelected()) {
                    executeAIQuery(query, resultsTable);
                } else {
                    executeQuery(query, resultsTable);
                }
            }
        });

        clearBtn.setOnAction(e -> {
            queryInput.clear();
            resultsTable.getItems().clear();
            resultsTable.getColumns().clear();
        });

        copyBtn.setOnAction(e -> {
            String text = queryInput.getText();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
            logToTerminal("Query copied to clipboard!");
        });

        container.getChildren().addAll(modeToolbar, queryInput, buttonBar, resultsTable);
        return container;
    }

    private void executeQuery(String query, TableView<ObservableList<String>> resultsTable) {
        logToTerminal("Executing query: " + query);

        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);
                Statement stmt = conn.createStatement();

                boolean hasResults = stmt.execute(query);

                if (hasResults) {
                    ResultSet rs = stmt.getResultSet();
                    displayResults(rs, resultsTable);
                } else {
                    int rowsAffected = stmt.getUpdateCount();
                    Platform.runLater(() -> {
                        logToTerminal("Query executed successfully. " + rowsAffected + " row(s) affected.");
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logToTerminal("ERROR: " + e.getMessage());
                    showError("Query Error", e.getMessage());
                });
            }
        }).start();
    }

    private void executeAIQuery(String naturalLanguage, TableView<ObservableList<String>> resultsTable) {
        if (!GeminiAPIClient.isConfigured()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Gemini API Key Required");
            dialog.setHeaderText("AI Mode requires Google Gemini API Key");
            dialog.setContentText("Enter your API Key:");
            dialog.getDialogPane().setPrefWidth(500);

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent() && !result.get().trim().isEmpty()) {
                GeminiAPIClient.setApiKey(result.get().trim());
                logToTerminal("API Key configured successfully!");
            } else {
                showWarning("API Key Required",
                        "Please provide a valid Gemini API key to use AI mode.\n\nGet your free API key at: https://makersuite.google.com/app/apikey");
                return;
            }
        }

        logToTerminal("AI Mode: Converting natural language to SQL...");
        logToTerminal("Input: " + naturalLanguage);

        new Thread(() -> {
            try {
                String schema = DatabaseSchemaExtractor.extractSchema(currentDatabase);

                Platform.runLater(() -> {
                    logToTerminal("Analyzing database schema...");
                    logToTerminal("Generating SQL query with AI...");
                });

                GeminiAPIClient aiClient = new GeminiAPIClient();
                String generatedSQL = aiClient.generateSQLQuery(naturalLanguage, schema);

                Platform.runLater(() -> {
                    logToTerminal("Generated SQL: " + generatedSQL);
                    logToTerminal("Executing AI-generated query...");
                });

                Connection conn = DatabaseConnection.getConnection(currentDatabase);
                Statement stmt = conn.createStatement();

                boolean hasResults = stmt.execute(generatedSQL);

                if (hasResults) {
                    ResultSet rs = stmt.getResultSet();
                    displayResults(rs, resultsTable);
                    Platform.runLater(() -> {
                        logToTerminal("✓ AI Query executed successfully!");
                    });
                } else {
                    int rowsAffected = stmt.getUpdateCount();
                    Platform.runLater(() -> {
                        logToTerminal("✓ Query executed successfully. " + rowsAffected + " row(s) affected.");
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logToTerminal("✗ ERROR: " + e.getMessage());
                    showError("AI Query Error", "Failed to generate or execute query:\n\n" + e.getMessage() +
                            "\n\nTip: Try rephrasing your question or be more specific.");
                });
            }
        }).start();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void displayResults(ResultSet rs, TableView<ObservableList<String>> table) throws Exception {
        Platform.runLater(() -> {
            table.getItems().clear();
            table.getColumns().clear();
        });

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            final int colIndex = i - 1;
            String columnName = metaData.getColumnName(i);
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(
                    param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(colIndex)));
            Platform.runLater(() -> table.getColumns().add(column));
        }

        int rowCount = 0;
        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                row.add(value == null ? "NULL" : value);
            }
            ObservableList<String> finalRow = row;
            Platform.runLater(() -> table.getItems().add(finalRow));
            rowCount++;
        }

        final int finalRowCount = rowCount;
        Platform.runLater(() -> {
            logToTerminal("Query successful! " + finalRowCount + " row(s) returned.");
        });
    }

    private void logToTerminal(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        terminalOutput.appendText("[" + timestamp + "] " + message + "\n");
    }

    @FXML
    private void handleBack() {
        try {
            SessionManager.clear();
            App.changeScene("/fxml/database-selector.fxml", "MyDB - Select Database");
        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadDatabaseStructure();
        logToTerminal("Database structure refreshed!");
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

    private void openTableInCRUD(String tableName) {
        logToTerminal("Opening table: " + tableName);

        mainTabPane.getSelectionModel().select(0);

        CRUDController crudController = new CRUDController(currentDatabase);
        VBox crudView = crudController.createCRUDView(tableName);

        Tab crudTab = mainTabPane.getTabs().get(0);
        crudTab.setContent(crudView);
        crudTab.setText("Table: " + tableName);
    }

    @FXML
    private void handleAISettings() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("AI Settings");
        dialog.setHeaderText("Configure Google Gemini API");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label infoLabel = new Label("Get your free API key at:");
        Hyperlink link = new Hyperlink("https://makersuite.google.com/app/apikey");
        link.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://makersuite.google.com/app/apikey"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        TextField apiKeyField = new TextField(GeminiAPIClient.getApiKey());
        apiKeyField.setPromptText("Enter your Gemini API Key");
        apiKeyField.setPrefWidth(400);

        Label statusLabel = new Label();
        if (GeminiAPIClient.isConfigured()) {
            statusLabel.setText("✓ API Key is configured");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("⚠ API Key not configured");
            statusLabel.setStyle("-fx-text-fill: orange;");
        }

        content.getChildren().addAll(infoLabel, link, new Label("API Key:"), apiKeyField, statusLabel);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return apiKeyField.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(apiKey -> {
            if (!apiKey.isEmpty()) {
                GeminiAPIClient.setApiKey(apiKey);
                logToTerminal("API Key updated successfully!");
                showInfo("Success", "Gemini API Key has been configured.\nYou can now use AI Mode!");
            }
        });
    }

    @FXML
    private void handleSecurity() {
        // Check if security tab exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals("Security Monitor")) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        // Create new security tab on demand
        Tab securityTab = new Tab("Security Monitor");
        securityTab.setClosable(true);

        FontIcon secIcon = new FontIcon(FontAwesomeSolid.SHIELD_ALT);
        securityTab.setGraphic(secIcon);

        SecurityController secController = new SecurityController(currentDatabase);
        VBox secContent = secController.createSecurityView();
        securityTab.setContent(secContent);

        mainTabPane.getTabs().add(securityTab);
        mainTabPane.getSelectionModel().select(securityTab);

        logToTerminal("Security Monitor opened");
    }
}
