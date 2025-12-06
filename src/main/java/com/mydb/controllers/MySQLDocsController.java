package com.mydb.controllers;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MySQLDocsController {

    public BorderPane createDocsView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("MySQL Documentation, Tips & Patterns");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 15 0;");
        root.setTop(title);

        // WebView to show docs
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Load official MySQL documentation homepage or specific URL
        // This can be local HTML files or official online docs (needs internet)
        // For demo, we use official MySQL doc URL:
        webEngine.load("https://dev.mysql.com/doc/");

        // Optional: Add tips/patterns below webview inside a VBox with scrollbar
        VBox tipsBox = new VBox(12);
        tipsBox.setStyle("-fx-padding: 10;");
        Label tipsHeader = new Label("MySQL Tricks & Common Patterns");
        tipsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        Label tipsContent = new Label(
            "• Use `EXPLAIN` to analyze query execution plans.\n" +
            "• Use `INDEX` to improve SELECT performance.\n" +
            "• Avoid SELECT *; specify needed columns.\n" +
            "• Use prepared statements to prevent SQL injection.\n" +
            "• Optimize joins with key indexes.\n" +
            "• Use transactions (START TRANSACTION, COMMIT, ROLLBACK) for critical operations.\n" +
            "• Use `INFORMATION_SCHEMA` for metadata queries.\n" +
            "• Use LIMIT/OFFSET for pagination.\n" +
            "• Avoid large result sets; use filtering and aggregation.\n" +
            "• Use stored procedures and functions for reusable logic."
        );
        tipsContent.setWrapText(true);
        tipsBox.getChildren().addAll(tipsHeader, tipsContent);

        ScrollPane tipsScroll = new ScrollPane(tipsBox);
        tipsScroll.setPrefHeight(280);
        tipsScroll.setFitToWidth(true);

        VBox centerBox = new VBox(10, webView, tipsScroll);

        root.setCenter(centerBox);

        return root;
    }
}
