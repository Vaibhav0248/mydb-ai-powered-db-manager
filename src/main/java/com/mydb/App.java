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
        
        // Load Login Screen with a consistent default size
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/css/main-theme.css").toExternalForm());
        
        primaryStage.setTitle("MyDB - AI-Powered Database Manager");
        primaryStage.setScene(scene);
        
        // Optional: Set minimum window size to prevent collapsing
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
        
        // Show stage first, then maximize it. This prevents startup sizing bugs on Windows
        primaryStage.show();
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void changeScene(String fxmlFile, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFile));
        Parent root = loader.load();
        
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(App.class.getResource("/css/main-theme.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        
        primaryStage.setTitle(title);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
