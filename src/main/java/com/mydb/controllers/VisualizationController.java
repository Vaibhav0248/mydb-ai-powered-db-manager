package com.mydb.controllers;

import com.mydb.utils.ChartGenerator;
import com.mydb.utils.DatabaseConnection;
import com.mydb.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisualizationController {

    private String currentDatabase;
    private VBox chartContainer;
    private TextArea queryInput;
    private Label statusLabel;
    private List<String> currentColumnNames;
    private ObservableList<ObservableList<String>> currentData;

    public VisualizationController(String database) {
        this.currentDatabase = database;
        this.currentColumnNames = new ArrayList<>();
        this.currentData = FXCollections.observableArrayList();
    }

    public VBox createVisualizationView() {
        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: white;");

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white;");

        // Header
        HBox header = createHeader();

        // Query input section
        VBox querySection = createQuerySection();

        // Chart type selector
        HBox chartSelector = createChartSelector();

        // Chart container
        chartContainer = new VBox();
        chartContainer.setStyle(
                "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 20; -fx-background-color: #fafafa;");
        chartContainer.setMinHeight(400);
        VBox.setVgrow(chartContainer, Priority.ALWAYS);

        // Status label
        statusLabel = new Label("Enter a query that returns 2 columns: labels and values");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        container.getChildren().addAll(header, querySection, chartSelector, chartContainer, statusLabel);

        // Add content to scroll pane
        scrollPane.setContent(container);

        // Add scroll pane to main container
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainContainer.getChildren().add(scrollPane);

        return mainContainer;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontIcon chartIcon = new FontIcon(FontAwesomeSolid.CHART_BAR);
        chartIcon.setIconSize(24);
        chartIcon.setIconColor(Color.web("#667eea"));

        Label title = new Label("Data Visualization");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        header.getChildren().addAll(chartIcon, title);

        return header;
    }

    private VBox createQuerySection() {
        VBox section = new VBox(10);

        Label label = new Label("SQL Query (must return 2 columns):");
        label.setStyle("-fx-font-weight: bold;");

        queryInput = new TextArea();
        queryInput.setPromptText("Example: SELECT category, COUNT(*) FROM products GROUP BY category");
        queryInput.setPrefRowCount(4);
        queryInput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px;");

        Button executeBtn = new Button("Load Data");
        executeBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold;");
        FontIcon playIcon = new FontIcon(FontAwesomeSolid.PLAY);
        playIcon.setIconColor(Color.WHITE);
        executeBtn.setGraphic(playIcon);
        executeBtn.setOnAction(e -> executeQuery());

        section.getChildren().addAll(label, queryInput, executeBtn);

        return section;
    }

    private HBox createChartSelector() {
        HBox selector = new HBox(10);
        selector.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        selector.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        Label label = new Label("Chart Type:");
        label.setStyle("-fx-font-weight: bold;");

        Button barBtn = createChartButton("Bar Chart", FontAwesomeSolid.CHART_BAR, () -> generateChart("BAR"));
        Button pieBtn = createChartButton("Pie Chart", FontAwesomeSolid.CHART_PIE, () -> generateChart("PIE"));
        Button lineBtn = createChartButton("Line Chart", FontAwesomeSolid.CHART_LINE, () -> generateChart("LINE"));
        Button areaBtn = createChartButton("Area Chart", FontAwesomeSolid.CHART_AREA, () -> generateChart("AREA"));
        Button scatterBtn = createChartButton("Scatter Plot", FontAwesomeSolid.BRAILLE, () -> generateChart("SCATTER"));

        selector.getChildren().addAll(label, barBtn, pieBtn, lineBtn, areaBtn, scatterBtn);

        return selector;
    }

    private Button createChartButton(String text, FontAwesomeSolid icon, Runnable action) {
        Button btn = new Button(text);
        FontIcon btnIcon = new FontIcon(icon);
        btn.setGraphic(btnIcon);
        btn.setStyle(
                "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 3; -fx-background-radius: 3;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void executeQuery() {
        String query = queryInput.getText().trim();

        if (query.isEmpty()) {
            showError("Empty Query", "Please enter a SQL query");
            return;
        }

        statusLabel.setText("Executing query...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");

        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection(currentDatabase);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount < 2) {
                    Platform.runLater(() -> {
                        showError("Invalid Query", "Query must return at least 2 columns (labels and values)");
                        statusLabel.setText("Error: Need 2 columns minimum");
                        statusLabel.setStyle("-fx-text-fill: #f44336;");
                    });
                    return;
                }

                currentColumnNames.clear();
                for (int i = 1; i <= columnCount; i++) {
                    currentColumnNames.add(metaData.getColumnName(i));
                }

                currentData.clear();
                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        row.add(value == null ? "NULL" : value);
                    }
                    currentData.add(row);
                }

                Platform.runLater(() -> {
                    statusLabel.setText(
                            "✓ Data loaded successfully! " + currentData.size() + " rows. Select a chart type.");
                    statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    generateChart("BAR"); // Default to bar chart
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showError("Query Error", e.getMessage());
                    statusLabel.setText("✗ Query failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                });
            }
        }).start();
    }

    private void generateChart(String chartType) {
        if (currentData.isEmpty()) {
            showWarning("No Data", "Please execute a query first to load data");
            return;
        }

        chartContainer.getChildren().clear();

        String title = "Data Visualization - " + currentDatabase;
        VBox chart = null;

        switch (chartType) {
            case "BAR":
                chart = ChartGenerator.generateBarChart(title, currentColumnNames, currentData);
                break;
            case "PIE":
                chart = ChartGenerator.generatePieChart(title, currentColumnNames, currentData);
                break;
            case "LINE":
                chart = ChartGenerator.generateLineChart(title, currentColumnNames, currentData);
                break;
            case "AREA":
                chart = ChartGenerator.generateAreaChart(title, currentColumnNames, currentData);
                break;
            case "SCATTER":
                chart = ChartGenerator.generateScatterChart(title, currentColumnNames, currentData);
                break;
        }

        if (chart != null) {
            chartContainer.getChildren().add(chart);
            VBox.setVgrow(chart, Priority.ALWAYS);
            statusLabel.setText("✓ " + chartType + " chart generated!");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
