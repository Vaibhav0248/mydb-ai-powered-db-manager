package com.mydb.controllers;

import com.mydb.utils.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class HelpController {

    public BorderPane createHelpView() {

        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));

        // Title Section
        HBox titleBox = new HBox(10);
        FontIcon helpIcon = new FontIcon(FontAwesomeSolid.INFO_CIRCLE);
        helpIcon.setIconSize(28);
        helpIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#667eea"));
        Label title = new Label("MyDB Help & About");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333;");
        titleBox.getChildren().addAll(helpIcon, title);
        titleBox.setPadding(new Insets(0, 0, 15, 0));

        // Scrollable content area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        content.setMaxWidth(700);

        // Welcome Text
        Label welcomeLabel = new Label("Welcome to MyDB - Your AI-Powered Database Manager.");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Description Text
        TextFlow description = new TextFlow(
                new javafx.scene.text.Text(
                        "MyDB is a modern, professional desktop application designed to make database management intuitive and powerful. "
                                + "Leverage AI-driven query generation, rich visualization, and comprehensive backup solutions all in one place.\n\n"
                                + "This Help section guides you through MyDB's features and provides useful tips and shortcuts to enhance your productivity.\n\n"
                                + "For more information, updates, or support, visit our website or contact our team at support@mydbapp.com.\n\n")
        );
        description.setStyle("-fx-font-size: 14px;");
        description.setMaxWidth(700);

        // MySQL Version section
        String mysqlVersion = getMySQLVersion();
        Label mysqlVersionLabel = new Label("Connected MySQL Version: " + mysqlVersion);
        mysqlVersionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 10 0 0 0;");

        // Features Section
        VBox featuresBox = new VBox(8);
        Label featuresLabel = new Label("Key Features");
        featuresLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        Label featureList = new Label(
                "• AI-driven Natural Language to SQL query generation\n"
                        + "• Intuitive drag-and-drop database navigation\n"
                        + "• Powerful CRUD operations with smart dialogs\n"
                        + "• Data visualization with multiple chart types\n"
                        + "• Security pattern scanning for risky SQL\n"
                        + "• Full database backup & granular table exports\n"
                        + "• Real-time terminal logs and status feedback\n"
                        + "• Customizable themes and layouts\n"
        );
        featureList.setWrapText(true);
        featureList.setStyle("-fx-font-size: 14px;");

        featuresBox.getChildren().addAll(featuresLabel, featureList);

        // Tips Section
        VBox tipsBox = new VBox(8);
        Label tipsLabel = new Label("Quick Tips");
        tipsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        Label tipsList = new Label(
                "• Double-click tables in the tree view to open CRUD tab.\n"
                        + "• Use AI Mode in the Query Editor for natural language querying.\n"
                        + "• Backup your data regularly using the Backup & Recovery tab.\n"
                        + "• Use the Security Monitor to scan SQL queries for risky patterns.\n"
                        + "• Customize query execution with MySQL Mode toggle.\n"
        );
        tipsList.setWrapText(true);
        tipsList.setStyle("-fx-font-size: 14px;");
        tipsBox.getChildren().addAll(tipsLabel, tipsList);

        // Contact & Resources Section
        VBox contactBox = new VBox(8);
        Label contactLabel = new Label("Contact & Resources");
        contactLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        Label contactInfo = new Label("Email: support@mydbapp.com\n"
                + "Website: www.mydbapp.com\n"
                + "GitHub: github.com/mydbapp");
        contactInfo.setWrapText(true);
        contactInfo.setStyle("-fx-font-size: 14px;");

        HBox socialBox = new HBox(15);
        FontIcon githubIcon = new FontIcon(FontAwesomeBrands.GITHUB);
        githubIcon.setIconSize(24);
        githubIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#333"));
        FontIcon twitterIcon = new FontIcon(FontAwesomeBrands.TWITTER);
        twitterIcon.setIconSize(24);
        twitterIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#1DA1F2"));
        FontIcon linkedinIcon = new FontIcon(FontAwesomeBrands.LINKEDIN);
        linkedinIcon.setIconSize(24);
        linkedinIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#0077B5"));
        socialBox.getChildren().addAll(githubIcon, twitterIcon, linkedinIcon);

        contactBox.getChildren().addAll(contactLabel, contactInfo, socialBox);

        content.getChildren().addAll(welcomeLabel, description, mysqlVersionLabel, featuresBox, tipsBox, contactBox);

        scrollPane.setContent(content);

        mainPane.setTop(titleBox);
        mainPane.setCenter(scrollPane);
        return mainPane;
    }

    private String getMySQLVersion() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if(conn == null) return "Not connected";
            DatabaseMetaData metaData = conn.getMetaData();
            return metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            return "Error fetching version";
        }
    }
}
