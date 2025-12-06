package com.mydb.controllers;

import com.mydb.utils.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.stage.FileChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CRUDController {

    private TableView<ObservableList<String>> tableView;
    private String currentTable;
    private String currentDatabase;
    private List<String> columnNames;
    private List<String> columnTypes;
    private Label recordCountLabel;

    public CRUDController(String database) {
        this.currentDatabase = database;
        this.columnNames = new ArrayList<>();
        this.columnTypes = new ArrayList<>();
    }

    public VBox createCRUDView(String tableName) {
        this.currentTable = tableName;

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Header with table info
        HBox header = createHeader();

        // Toolbar with buttons
        ToolBar toolbar = createToolbar();

        // Table view
        tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.setPlaceholder(new Label("No data available"));

        // Record count label
        recordCountLabel = new Label("Records: 0");
        recordCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Load data
        loadTableData();

        container.getChildren().addAll(header, toolbar, tableView, recordCountLabel);
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontIcon tableIcon = new FontIcon(FontAwesomeSolid.TABLE);
        tableIcon.setIconSize(24);
        tableIcon.setIconColor(Color.web("#4CAF50"));

        Label tableLabel = new Label(currentTable);
        tableLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        header.getChildren().addAll(tableIcon, tableLabel);

        return header;
    }

    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();

        // Refresh button
        Button refreshBtn = new Button("Refresh");
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC);
        refreshBtn.setGraphic(refreshIcon);
        refreshBtn.setOnAction(e -> loadTableData());

        // Add button
        Button addBtn = new Button("Add Record");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        FontIcon addIcon = new FontIcon(FontAwesomeSolid.PLUS);
        addIcon.setIconColor(Color.WHITE);
        addBtn.setGraphic(addIcon);
        addBtn.setOnAction(e -> handleAddRecord());

        // Edit button
        Button editBtn = new Button("Edit");
        FontIcon editIcon = new FontIcon(FontAwesomeSolid.EDIT);
        editBtn.setGraphic(editIcon);
        editBtn.setOnAction(e -> handleEditRecord());

        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);
        deleteIcon.setIconColor(Color.WHITE);
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setOnAction(e -> handleDeleteRecord());

        // Export button
        Button exportBtn = new Button("Export");
        FontIcon exportIcon = new FontIcon(FontAwesomeSolid.FILE_EXPORT);
        exportBtn.setGraphic(exportIcon);
        exportBtn.setOnAction(e -> handleExport());

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(200);

        toolbar.getItems().addAll(
                refreshBtn,
                new Separator(),
                addBtn,
                editBtn,
                deleteBtn,
                new Separator(),
                exportBtn,
                new Separator(),
                searchField);

        return toolbar;
    }

    private void loadTableData() {
        tableView.getItems().clear();
        tableView.getColumns().clear();
        columnNames.clear();
        columnTypes.clear();

        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);

                // Get column metadata
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet columns = metaData.getColumns(currentDatabase, null, currentTable, null);

                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    columnNames.add(columnName);
                    columnTypes.add(columnType);
                }

                // Create columns
                for (int i = 0; i < columnNames.size(); i++) {
                    final int colIndex = i;
                    String columnName = columnNames.get(i);

                    TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
                    column.setMinWidth(100);
                    column.setCellValueFactory(param -> {
                        if (param.getValue().size() > colIndex) {
                            return new SimpleStringProperty(param.getValue().get(colIndex));
                        }
                        return new SimpleStringProperty("");
                    });

                    Platform.runLater(() -> tableView.getColumns().add(column));
                }

                // Load data
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + currentTable);

                int rowCount = 0;
                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= columnNames.size(); i++) {
                        String value = rs.getString(i);
                        row.add(value == null ? "NULL" : value);
                    }
                    Platform.runLater(() -> tableView.getItems().add(row));
                    rowCount++;
                }

                final int finalRowCount = rowCount;
                Platform.runLater(() -> {
                    recordCountLabel.setText("Records: " + finalRowCount);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error Loading Table", e.getMessage());
                });
            }
        }).start();
    }

    private void handleAddRecord() {
        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Add New Record");
        dialog.setHeaderText("Enter values for new record in " + currentTable);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        List<TextField> fields = new ArrayList<>();

        for (int i = 0; i < columnNames.size(); i++) {
            Label label = new Label(columnNames.get(i) + " (" + columnTypes.get(i) + "):");
            TextField textField = new TextField();
            textField.setPromptText("Enter " + columnNames.get(i));

            grid.add(label, 0, i);
            grid.add(textField, 1, i);

            fields.add(textField);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                ObservableList<String> values = FXCollections.observableArrayList();
                for (TextField field : fields) {
                    values.add(field.getText().trim());
                }
                return values;
            }
            return null;
        });

        Optional<ObservableList<String>> result = dialog.showAndWait();

        result.ifPresent(values -> {
            insertRecord(values);
        });
    }

    private void insertRecord(ObservableList<String> values) {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);

                StringBuilder sql = new StringBuilder("INSERT INTO " + currentTable + " (");
                StringBuilder placeholders = new StringBuilder("VALUES (");

                for (int i = 0; i < columnNames.size(); i++) {
                    sql.append(columnNames.get(i));
                    placeholders.append("?");

                    if (i < columnNames.size() - 1) {
                        sql.append(", ");
                        placeholders.append(", ");
                    }
                }

                sql.append(") ");
                placeholders.append(")");
                sql.append(placeholders);

                PreparedStatement pstmt = conn.prepareStatement(sql.toString());

                for (int i = 0; i < values.size(); i++) {
                    String value = values.get(i);
                    if (value.isEmpty() || value.equalsIgnoreCase("NULL")) {
                        pstmt.setNull(i + 1, Types.VARCHAR);
                    } else {
                        pstmt.setString(i + 1, value);
                    }
                }

                int rowsAffected = pstmt.executeUpdate();

                Platform.runLater(() -> {
                    if (rowsAffected > 0) {
                        showInfo("Success", "Record added successfully!");
                        loadTableData();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Insert Error", e.getMessage());
                });
            }
        }).start();
    }

    private void handleEditRecord() {
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showWarning("No Selection", "Please select a record to edit.");
            return;
        }

        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Edit Record");
        dialog.setHeaderText("Edit record in " + currentTable);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        List<TextField> fields = new ArrayList<>();

        for (int i = 0; i < columnNames.size(); i++) {
            Label label = new Label(columnNames.get(i) + ":");
            TextField textField = new TextField(selectedRow.get(i));

            grid.add(label, 0, i);
            grid.add(textField, 1, i);

            fields.add(textField);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                ObservableList<String> values = FXCollections.observableArrayList();
                for (TextField field : fields) {
                    values.add(field.getText().trim());
                }
                return values;
            }
            return null;
        });

        Optional<ObservableList<String>> result = dialog.showAndWait();

        result.ifPresent(newValues -> {
            updateRecord(selectedRow, newValues);
        });
    }

    private void updateRecord(ObservableList<String> oldValues, ObservableList<String> newValues) {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);

                StringBuilder sql = new StringBuilder("UPDATE " + currentTable + " SET ");

                for (int i = 0; i < columnNames.size(); i++) {
                    sql.append(columnNames.get(i)).append(" = ?");
                    if (i < columnNames.size() - 1) {
                        sql.append(", ");
                    }
                }

                sql.append(" WHERE ");
                for (int i = 0; i < columnNames.size(); i++) {
                    sql.append(columnNames.get(i)).append(" = ?");
                    if (i < columnNames.size() - 1) {
                        sql.append(" AND ");
                    }
                }

                PreparedStatement pstmt = conn.prepareStatement(sql.toString());

                int paramIndex = 1;
                for (String newValue : newValues) {
                    pstmt.setString(paramIndex++, newValue);
                }
                for (String oldValue : oldValues) {
                    pstmt.setString(paramIndex++, oldValue);
                }

                int rowsAffected = pstmt.executeUpdate();

                Platform.runLater(() -> {
                    if (rowsAffected > 0) {
                        showInfo("Success", "Record updated successfully!");
                        loadTableData();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Update Error", e.getMessage());
                });
            }
        }).start();
    }

    private void handleDeleteRecord() {
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showWarning("No Selection", "Please select a record to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Record");
        confirmation.setContentText("Are you sure you want to delete this record?\nThis action cannot be undone!");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteRecord(selectedRow);
        }
    }

    private void deleteRecord(ObservableList<String> rowValues) {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);

                StringBuilder sql = new StringBuilder("DELETE FROM " + currentTable + " WHERE ");

                for (int i = 0; i < columnNames.size(); i++) {
                    sql.append(columnNames.get(i)).append(" = ?");
                    if (i < columnNames.size() - 1) {
                        sql.append(" AND ");
                    }
                }

                PreparedStatement pstmt = conn.prepareStatement(sql.toString());

                for (int i = 0; i < rowValues.size(); i++) {
                    pstmt.setString(i + 1, rowValues.get(i));
                }

                int rowsAffected = pstmt.executeUpdate();

                Platform.runLater(() -> {
                    if (rowsAffected > 0) {
                        showInfo("Success", "Record deleted successfully!");
                        loadTableData();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Delete Error", e.getMessage());
                });
            }
        }).start();
    }

    private void handleExport() {
        if (currentTable == null || currentTable.isEmpty()) {
            showWarning("No Table Selected", "Please select a table to export.");
            return;
        }

        // Ask user which format to use
        List<String> choices = List.of("CSV", "SQL", "JSON");
        ChoiceDialog<String> formatDialog = new ChoiceDialog<>("CSV", choices);
        formatDialog.setTitle("Export Table");
        formatDialog.setHeaderText("Export \"" + currentTable + "\"");
        formatDialog.setContentText("Choose export format:");

        Optional<String> formatResult = formatDialog.showAndWait();
        if (formatResult.isEmpty()) {
            return; // user cancelled
        }
        String chosenFormat = formatResult.get();

        // Configure FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save " + currentTable + " as " + chosenFormat);

        switch (chosenFormat) {
            case "CSV":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                fileChooser.setInitialFileName(currentTable + ".csv");
                break;
            case "SQL":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("SQL Files", "*.sql"));
                fileChooser.setInitialFileName(currentTable + ".sql");
                break;
            case "JSON":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                fileChooser.setInitialFileName(currentTable + ".json");
                break;
        }

        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        // Run export on background thread
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection(currentDatabase)) {
                switch (chosenFormat) {
                    case "CSV":
                        exportAsCSV(conn, file);
                        break;
                    case "SQL":
                        exportAsSQL(conn, file);
                        break;
                    case "JSON":
                        exportAsJSON(conn, file);
                        break;
                }

                Platform.runLater(() -> showInfo("Export Complete",
                        "Table \"" + currentTable + "\" exported successfully as " + chosenFormat + "."));

            } catch (Exception e) {
                Platform.runLater(() -> showError("Export Error", e.getMessage()));
            }
        }).start();
    }

    private void exportAsCSV(Connection conn, File file) throws SQLException, IOException {
        String query = "SELECT * FROM " + currentTable;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            // Header row
            for (int i = 0; i < columnNames.size(); i++) {
                writer.write(escapeCSV(columnNames.get(i)));
                if (i < columnNames.size() - 1)
                    writer.write(",");
            }
            writer.newLine();

            // Data rows
            while (rs.next()) {
                for (int i = 0; i < columnNames.size(); i++) {
                    String value = rs.getString(columnNames.get(i));
                    if (value == null)
                        value = "";
                    writer.write(escapeCSV(value));
                    if (i < columnNames.size() - 1)
                        writer.write(",");
                }
                writer.newLine();
            }
        }
    }

    private String escapeCSV(String value) {
        boolean needQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
                || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        if (needQuotes) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void exportAsSQL(Connection conn, File file) throws SQLException, IOException {
        String selectQuery = "SELECT * FROM " + currentTable;

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectQuery);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write("-- Data export for table `" + currentTable + "`");
            writer.newLine();
            writer.write("USE `" + currentDatabase + "`;");
            writer.newLine();
            writer.newLine();

            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO `").append(currentTable).append("` (");

                // columns
                for (int i = 0; i < columnNames.size(); i++) {
                    sb.append("`").append(columnNames.get(i)).append("`");
                    if (i < columnNames.size() - 1)
                        sb.append(", ");
                }
                sb.append(") VALUES (");

                // values
                for (int i = 0; i < columnNames.size(); i++) {
                    String colName = columnNames.get(i);
                    String value = rs.getString(colName);
                    if (value == null) {
                        sb.append("NULL");
                    } else {
                        sb.append("'").append(escapeSQL(value)).append("'");
                    }
                    if (i < columnNames.size() - 1)
                        sb.append(", ");
                }
                sb.append(");");
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    private String escapeSQL(String value) {
        // Basic escaping of single quotes and backslashes
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private void exportAsJSON(Connection conn, File file) throws SQLException, IOException {
    String query = "SELECT * FROM " + currentTable;
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query);
         BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

        writer.write("[");
        writer.newLine();

        boolean firstRow = true;
        while (rs.next()) {
            if (!firstRow) {
                writer.write(",");
                writer.newLine();
            }
            writer.write("  {");

            for (int i = 0; i < columnNames.size(); i++) {
                String colName = columnNames.get(i);
                String value = rs.getString(colName);
                writer.write("\"" + escapeJSON(colName) + "\": ");

                if (value == null) {
                    writer.write("null");
                } else {
                    writer.write("\"" + escapeJSON(value) + "\"");
                }

                if (i < columnNames.size() - 1) writer.write(", ");
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
