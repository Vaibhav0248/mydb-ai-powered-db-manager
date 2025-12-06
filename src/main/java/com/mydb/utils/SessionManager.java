package com.mydb.utils;

public class SessionManager {
    private static String currentDatabase;
    private static String currentUser;
    
    public static void setCurrentDatabase(String database) {
        currentDatabase = database;
    }
    
    public static String getCurrentDatabase() {
        return currentDatabase;
    }
    
    public static void setCurrentUser(String user) {
        currentUser = user;
    }
    
    public static String getCurrentUser() {
        return currentUser;
    }
    
    public static void clear() {
        currentDatabase = null;
        currentUser = null;
    }
}
