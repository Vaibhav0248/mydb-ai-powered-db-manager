package com.mydb.models;

public class Database {
    private String name;
    private int tableCount;
    
    public Database(String name) {
        this.name = name;
        this.tableCount = 0;
    }
    
    public Database(String name, int tableCount) {
        this.name = name;
        this.tableCount = tableCount;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getTableCount() {
        return tableCount;
    }
    
    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
