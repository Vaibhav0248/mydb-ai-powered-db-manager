package com.mydb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    private static String host;
    private static String port;
    private static String username;
    private static String password;
    private static Connection connection;
    
    public static void initialize(String host, String port, String username, String password) {
        DatabaseConnection.host = host;
        DatabaseConnection.port = port;
        DatabaseConnection.username = username;
        DatabaseConnection.password = password;
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:mysql://" + host + ":" + port + "/";
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
    
    public static Connection getConnection(String database) throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(url, username, password);
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getUsername() { return username; }
}
