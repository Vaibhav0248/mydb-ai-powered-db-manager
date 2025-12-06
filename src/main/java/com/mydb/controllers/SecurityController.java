package com.mydb.controllers;

import com.mydb.security.SecurityMonitor;
import com.mydb.security.SecurityMonitor.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SECURITY NOTE:
 * - This security panel uses simple pattern-based keyword checks only!
 * - It does NOT block threats, decode advanced injections, or secure your DB.
 * - Remote MySQL logins are allowed (no code blocks it).
 * - For true security, use strong DB permissions, proper input sanitation, and parameterized queries in your actual app code!
 */
public class SecurityController {

    private String currentDatabase;
    private Label totalQueriesLabel;
    private Label criticalThreatsLabel;
    private Label highRiskLabel;
    private Label alertsLabel;
    private TableView<QueryLog> queryLogTable;
    private TableView<SecurityAlert> alertsTable;
    private PieChart threatChart;

    public SecurityController(String database) {
        this.currentDatabase = database;
    }

    public VBox createSecurityView() {
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: white;");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white;");

        // Header
        HBox header = createHeader();
        // Query Testing Section
        VBox queryTestSection = createQueryTestSection();

        // Statistics cards
        HBox statsCards = createStatisticsCards();
        VBox chartSection = createChartSection();
        VBox alertsSection = createAlertsSection();
        VBox queryLogSection = createQueryLogSection();

        // Refresh button
        HBox refreshBox = new HBox();
        refreshBox.setAlignment(javafx.geometry.Pos.CENTER);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC);
        refreshIcon.setIconColor(Color.WHITE);
        refreshBtn.setGraphic(refreshIcon);
        refreshBtn.setOnAction(e -> refreshData());
        refreshBox.getChildren().add(refreshBtn);

        container.getChildren().addAll(
                header,
                createInfoAlert(),
                queryTestSection,
                new Separator(),
                statsCards,
                new Separator(),
                chartSection,
                new Separator(),
                alertsSection,
                new Separator(),
                queryLogSection,
                refreshBox
        );

        scrollPane.setContent(container);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainContainer.getChildren().add(scrollPane);

        refreshData();
        return mainContainer;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(FontAwesomeSolid.SHIELD_ALT);
        icon.setIconSize(24);
        icon.setIconColor(Color.web("#f44336"));

        Label title = new Label("Security Monitor");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Pattern-based SQL query checks for hints of threats");
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        VBox titleBox = new VBox(5, title, subtitle);
        header.getChildren().addAll(icon, titleBox);
        return header;
    }

    private HBox createInfoAlert() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: #ffc10788; -fx-background-radius: 7;");
        Label warning = new Label("This security analysis uses basic pattern-matching for common SQL risks. "
            + "It is NOT a substitute for proper database security (strong users/passwords, strict permissions, "
            + "input sanitation, parameterized queries).");
        warning.setWrapText(true);
        box.getChildren().addAll(new Label("⚠️"), warning);
        return box;
    }

    private HBox createStatisticsCards() {
        HBox cards = new HBox(15);
        cards.setAlignment(javafx.geometry.Pos.CENTER);

        VBox card1 = createStatCard("Tested Queries", "0", "#2196F3", FontAwesomeSolid.DATABASE);
        VBox card2 = createStatCard("Critical Alerts", "0", "#f44336", FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        VBox card3 = createStatCard("High Risk", "0", "#FF9800", FontAwesomeSolid.EXCLAMATION_CIRCLE);
        VBox card4 = createStatCard("Info", "0", "#9C27B0", FontAwesomeSolid.BELL);

        totalQueriesLabel = (Label) ((VBox) card1.getChildren().get(1)).getChildren().get(0);
        criticalThreatsLabel = (Label) ((VBox) card2.getChildren().get(1)).getChildren().get(0);
        highRiskLabel = (Label) ((VBox) card3.getChildren().get(1)).getChildren().get(0);
        alertsLabel = (Label) ((VBox) card4.getChildren().get(1)).getChildren().get(0);

        cards.getChildren().addAll(card1, card2, card3, card4);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        return cards;
    }

    private VBox createStatCard(String title, String value, String color, FontAwesomeSolid icon) {
        VBox card = new VBox(10);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-padding: 20;");

        FontIcon cardIcon = new FontIcon(icon);
        cardIcon.setIconSize(32);
        cardIcon.setIconColor(Color.WHITE);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        VBox textBox = new VBox(5, valueLabel, titleLabel);
        textBox.setAlignment(javafx.geometry.Pos.CENTER);

        card.getChildren().addAll(cardIcon, textBox);
        return card;
    }

    private VBox createChartSection() {
        VBox section = new VBox(15);
        Label title = new Label("Detected Patterns (last 1000 tests)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        threatChart = new PieChart();
        threatChart.setTitle("Query Risk Levels");
        threatChart.setLegendVisible(true);
        threatChart.setPrefHeight(300);
        section.getChildren().addAll(title, threatChart);
        return section;
    }

    private VBox createAlertsSection() {
        VBox section = new VBox(15);
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("Security Alerts");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button clearBtn = new Button("Clear All");
        clearBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        clearBtn.setOnAction(e -> {
            SecurityMonitor.clearAlerts();
            refreshData();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, clearBtn);

        alertsTable = new TableView<>();
        alertsTable.setPrefHeight(200);

        TableColumn<SecurityAlert, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        TableColumn<SecurityAlert, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().severity));
        TableColumn<SecurityAlert, String> titleCol = new TableColumn<>("Alert");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().title));
        TableColumn<SecurityAlert, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().description));
        descCol.setPrefWidth(300);
        alertsTable.getColumns().addAll(timeCol, severityCol, titleCol, descCol);
        section.getChildren().addAll(header, alertsTable);
        return section;
    }

    private VBox createQueryLogSection() {
        VBox section = new VBox(15);
        Label title = new Label("Check Log (pattern-matching only; NOT runtime DB queries)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        queryLogTable = new TableView<>();
        queryLogTable.setPrefHeight(250);

        TableColumn<QueryLog, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        TableColumn<QueryLog, String> riskCol = new TableColumn<>("Risk");
        riskCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().riskLevel));
        riskCol.setCellFactory(column -> new TableCell<QueryLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("CRITICAL")) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                    } else if (item.equals("HIGH")) {
                        setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");
                    }
                }
            }
        });

        TableColumn<QueryLog, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().username));
        TableColumn<QueryLog, String> dbCol = new TableColumn<>("Database");
        dbCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().database));
        TableColumn<QueryLog, String> queryCol = new TableColumn<>("Tested Query");
        queryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().query));
        queryCol.setPrefWidth(400);

        queryLogTable.getColumns().addAll(timeCol, riskCol, userCol, dbCol, queryCol);
        section.getChildren().addAll(title, queryLogTable);
        return section;
    }

    private void refreshData() {
        SecurityStatistics stats = SecurityMonitor.getStatistics();
        totalQueriesLabel.setText(String.valueOf(stats.totalQueries));
        criticalThreatsLabel.setText(String.valueOf(stats.criticalThreats));
        highRiskLabel.setText(String.valueOf(stats.highRiskQueries));
        alertsLabel.setText(String.valueOf(stats.unacknowledgedAlerts));
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList(
                new PieChart.Data("Safe (" + stats.safeQueries + ")", stats.safeQueries),
                new PieChart.Data("High Risk (" + stats.highRiskQueries + ")", stats.highRiskQueries),
                new PieChart.Data("Critical (" + stats.criticalThreats + ")", stats.criticalThreats));
        threatChart.setData(chartData);
        List<SecurityAlert> alerts = SecurityMonitor.getAlerts();
        alertsTable.setItems(FXCollections.observableArrayList(alerts));
        List<QueryLog> queryLogs = SecurityMonitor.getQueryHistory();
        queryLogTable.setItems(FXCollections.observableArrayList(queryLogs));
    }

    // Query tester (unchanged structure, but description is honest!)
    private VBox createQueryTestSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: #fff3cd; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #ffc107; -fx-border-width: 2; -fx-border-radius: 10;");
        // Header
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        FontIcon testIcon = new FontIcon(FontAwesomeSolid.SHIELD_ALT);
        testIcon.setIconSize(24);
        testIcon.setIconColor(Color.web("#ff9800"));
        Label title = new Label("Query Pattern Checker (not a DB Firewall!)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label subtitle = new Label("Scan for common risky SQL patterns. Result is for reference only.");
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        VBox titleBox = new VBox(5, title, subtitle);
        header.getChildren().addAll(testIcon, titleBox);

        // Query input
        Label inputLabel = new Label("Enter SQL Query to Scan:");
        inputLabel.setStyle("-fx-font-weight: bold;");
        TextArea queryInput = new TextArea();
        queryInput.setPromptText("Example: SELECT * FROM users WHERE id = 1 OR 1=1");
        queryInput.setPrefRowCount(4);
        queryInput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
        // Test button
        Button testBtn = new Button("Pattern Check");
        testBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 30;");
        FontIcon analyzeIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        analyzeIcon.setIconColor(Color.WHITE);
        testBtn.setGraphic(analyzeIcon);
        // Results area
        VBox resultsBox = new VBox(10);
        resultsBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        resultsBox.setVisible(false);
        Label resultsTitle = new Label("Static Pattern Analysis Results:");
        resultsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label riskLevelLabel = new Label();
        riskLevelLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox threatsBox = new VBox(8);
        resultsBox.getChildren().addAll(resultsTitle, riskLevelLabel, new Separator(), threatsBox);

        // Sample queries
        HBox samplesBox = new HBox(10);
        samplesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label samplesLabel = new Label("Try Sample Patterns:");
        samplesLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
        Button safeBtn = createSampleButton("Safe Query", "#4CAF50", () -> {
            queryInput.setText("SELECT * FROM users WHERE id = 123;");
        });
        Button injectionBtn = createSampleButton("SQL Injection", "#f44336", () -> {
            queryInput.setText("SELECT * FROM users WHERE username = 'admin' OR '1'='1' --");
        });
        Button dropBtn = createSampleButton("Drop Table", "#d32f2f", () -> {
            queryInput.setText("DROP TABLE users; --");
        });
        Button unionBtn = createSampleButton("UNION", "#e91e63", () -> {
            queryInput.setText("SELECT id, username FROM users UNION SELECT null, password FROM passwords");
        });
        samplesBox.getChildren().addAll(samplesLabel, safeBtn, injectionBtn, dropBtn, unionBtn);

        testBtn.setOnAction(e -> {
            String query = queryInput.getText().trim();
            if (query.isEmpty()) {
                showWarning("Empty Query", "Please enter a query to test");
                return;
            }
            // Analyze pattern match only
            SecurityMonitor.SecurityAnalysis analysis = SecurityMonitor.analyzeQuery(
                    query, "TestUser", currentDatabase);
            // Show results (as before, but honest: this is only simple pattern/dictionary matching)
            resultsBox.setVisible(true);
            String riskText = "Detected Risk (pattern-based): " + analysis.riskLevel;
            riskLevelLabel.setText(riskText);
            switch (analysis.riskLevel) {
                case "CRITICAL":
                    riskLevelLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-background-color: #ffebee; -fx-padding: 10; -fx-background-radius: 5;");
                    break;
                case "HIGH":
                    riskLevelLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f57c00; -fx-background-color: #fff3e0; -fx-padding: 10; -fx-background-radius: 5;");
                    break;
                case "SAFE":
                    riskLevelLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #388e3c; -fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");
                    break;
            }
            threatsBox.getChildren().clear();
            for (SecurityMonitor.SecurityThreat threat : analysis.threats) {
                VBox threatBox = new VBox(5);
                String bgColor = "";
                String textColor = "";
                String iconName = "";
                switch (threat.severity) {
                    case "CRITICAL": bgColor = "#ffebee"; textColor = "#c62828"; iconName = "⛔"; break;
                    case "WARNING": bgColor = "#fff3e0"; textColor = "#e65100"; iconName = "⚠️"; break;
                    case "INFO": bgColor = "#e8f5e9"; textColor = "#2e7d32"; iconName = "✓"; break;
                }
                threatBox.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 10; -fx-background-radius: 5;");
                Label threatTitle = new Label(iconName + " " + threat.type + " - " + threat.severity);
                threatTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
                Label threatMsg = new Label(threat.message);
                threatMsg.setStyle("-fx-text-fill: #333;");
                threatMsg.setWrapText(true);
                Label threatDetails = new Label(threat.details);
                threatDetails.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-font-style: italic;");
                threatDetails.setWrapText(true);
                threatBox.getChildren().addAll(threatTitle, threatMsg, threatDetails);
                threatsBox.getChildren().add(threatBox);
            }
            refreshData();
        });

        section.getChildren().addAll(
                header, inputLabel, queryInput, testBtn, samplesBox, resultsBox
        );
        return section;
    }

    private Button createSampleButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 3;");
        btn.setOnAction(e -> action.run());
        return btn;
    }
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
