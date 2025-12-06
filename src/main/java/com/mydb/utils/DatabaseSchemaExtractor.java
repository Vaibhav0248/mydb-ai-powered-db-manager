package com.mydb.utils;

import java.sql.*;

public class DatabaseSchemaExtractor {
    
    public static String extractSchema(String databaseName) {
        StringBuilder schema = new StringBuilder();
        
        try {
            Connection conn = DatabaseConnection.getConnection(databaseName);
            DatabaseMetaData metaData = conn.getMetaData();
            
            schema.append("Database: ").append(databaseName).append("\n\n");
            
            // Get all tables
            ResultSet tables = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"});
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                schema.append("Table: ").append(tableName).append("\n");
                
                // Get columns for this table
                ResultSet columns = metaData.getColumns(databaseName, null, tableName, null);
                
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    String nullable = columns.getString("IS_NULLABLE");
                    
                    schema.append("  - ").append(columnName)
                          .append(" (").append(columnType).append(")")
                          .append(nullable.equals("NO") ? " NOT NULL" : "")
                          .append("\n");
                }
                
                // Get primary keys
                ResultSet primaryKeys = metaData.getPrimaryKeys(databaseName, null, tableName);
                StringBuilder pkList = new StringBuilder();
                
                while (primaryKeys.next()) {
                    if (pkList.length() > 0) pkList.append(", ");
                    pkList.append(primaryKeys.getString("COLUMN_NAME"));
                }
                
                if (pkList.length() > 0) {
                    schema.append("  PRIMARY KEY: ").append(pkList).append("\n");
                }
                
                schema.append("\n");
            }
            
        } catch (SQLException e) {
            schema.append("Error extracting schema: ").append(e.getMessage());
        }
        
        return schema.toString();
    }
}
