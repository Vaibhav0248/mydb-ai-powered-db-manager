package com.mydb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Load Login Screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/main-theme.css").toExternalForm());
        
        primaryStage.setTitle("MyDB - AI-Powered Database Manager");
        primaryStage.setScene(scene);
        
        // Make window maximized and allow resizing
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        
        // Optional: Set minimum window size
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        
        // Add logo icon (we'll add this later)
        // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
        
        primaryStage.show();
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void changeScene(String fxmlFile, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("/css/main-theme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        
        // Keep maximized when changing scenes
        primaryStage.setMaximized(true);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
